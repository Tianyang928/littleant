package net.tianyang928.littleant.entity;

import com.google.gson.JsonParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.network.PacketDistributor;
import net.tianyang928.littleant.LittleAnt;
import net.tianyang928.littleant.client.overlay.debug.AntDebugClientState;
import net.tianyang928.littleant.entity.ai.brain.*;
import net.tianyang928.littleant.entity.ai.goal.*;
import net.tianyang928.littleant.entity.ai.sense.*;
import net.tianyang928.littleant.gui.AntInventoryMenu;
import net.tianyang928.littleant.gui.AntBrainProgramMenu;
import net.tianyang928.littleant.entity.ai.debug.TaskDebugCodec;
import net.tianyang928.littleant.network.SyncAntTaskDebugPayload;

import javax.annotation.Nullable;
import java.util.*;

public class AntEntity extends PathfinderMob implements InventoryCarrier {

    public static final int INVENTORY_SIZE = 9;
    private static final int INVENTORY_SLOT_OFFSET = 300;
    private static final EntityDimensions CROUCHING_DIMENSIONS =
            EntityDimensions.scalable(0.6F, 1.5F).withEyeHeight(1.27F);
    private static final EntityDimensions SWIMMING_DIMENSIONS =
            EntityDimensions.scalable(0.6F, 0.6F).withEyeHeight(0.4F);

    private AntEntityGlobalData antEntityGlobalData;
    AntScriptInterpreter antScriptInterpreter = new AntScriptInterpreter(this);

    @Nullable
    private BreakBlockGoal breakBlockGoal;
    @Nullable
    private SetBlockGoal setBlockGoal;
    @Nullable
    private UseCraftingTableGoal useCraftingTableGoal;
    @Nullable
    private UseInventoryCraftingGoal useInventoryCraftingGoal;
    @Nullable
    private UseContainerGoal useContainerGoal;
    @Nullable
    private EntityMeleeAttackGoal meleeAttackGoal;

    private final FindNearestBlockEntity findNearestBlockEntity = new FindNearestBlockEntity(this);
    private final FindNearestEntity findNearestEntity = new FindNearestEntity(this);
    private final FindNearestBlock findNearestBlock = new FindNearestBlock(this, null);
    private final FindNearestDrop findNearestDrop = new FindNearestDrop(this);
    private final FindNearestPheromone findNearestPheromone = new FindNearestPheromone(this);
    private final FindBlockList findBlockList = new FindBlockList(this);
    private final FindEntityList findEntityList = new FindEntityList(this);
    private final FindDropList findDropList = new FindDropList(this);
    private final FindBlockEntityList findBlockEntityList = new FindBlockEntityList(this);
    private final FindPheromoneList findPheromoneList = new FindPheromoneList(this);
    private final GetSurroundingPheromoneType getSurroundingPheromoneTypes = new GetSurroundingPheromoneType(this);


    private final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);
    /** Container positions currently held open by any active UseContainerGoal instance. */
    private final Map<BlockPos, Integer> openedContainerPositions = new HashMap<>();
    private final FoodData foodData = new FoodData();
    private int selectedSlot = 0;
    private BlockPos lastTimePos = null;
    private final LinkedHashMap<UUID, BrainBlock> brainBlocks = new LinkedHashMap<>();
    private boolean isProgrammingBrain = false;
    private Player programmingPlayer = null;
    public boolean needAiRestart = false;
    private int lastTaskDebugHash;
    private int lastTaskDebugSyncTick = Integer.MIN_VALUE;

    public boolean tryGettingDownWater = false;
    public double speedModifier = 1.0;
    public boolean isCrouching = false;

    public LivingEntity lastHurtBy = null;
    public long lastHurtTime = -1;

    private double doubleFoodLevel = 20.0D;

    private static final EntityDataAccessor<String> skinNameAccessor =
            SynchedEntityData.defineId(
                    // The class of the entity.
                    AntEntity.class,
                    // The entity data accessor type.
                    EntityDataSerializers.STRING
            );
    private static final EntityDataAccessor<ItemStack> selectedItemAccessor =
            SynchedEntityData.defineId(AntEntity.class, EntityDataSerializers.ITEM_STACK);

    public AntEntity(EntityType<? extends AntEntity> type, Level level) {
        super(type, level);
        this.setCanPickUpLoot(true);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && !this.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
            // Shift deliberately selects the programming surface; normal use remains the inventory.
            serverPlayer.openMenu(player.isShiftKeyDown()
                    ? new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            return Component.translatable("menu.littleant.ant_brain_program");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int id, Inventory inventory, Player ignored) {
                            return new AntBrainProgramMenu(id, inventory, AntEntity.this);
                        }

                        @Override
                        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
                            AntEntity.this.writeBrainProgramClientData(buf);
                        }
                    }
                    : new MenuProvider() {
                        @Override
                        public Component getDisplayName() {
                            // 如果有自定义名称，就显示自定义名称
                            return Component.translatable("container.littleant.ant_inventory");
                        }

                        @Override
                        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                            return new AntInventoryMenu(containerId, playerInventory, AntEntity.this);
                        }

                        @Override
                        public void writeClientSideData(AbstractContainerMenu menu, RegistryFriendlyByteBuf buf) {
                            AntEntity.this.writeInventoryClientData(buf);
                        }
                    }
            );
        }
        return InteractionResult.SUCCESS;
    }

    /** Sends the program snapshot when the dedicated brain menu is opened. */
    public void writeBrainProgramClientData(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(AntDebugClientState.enabled());
        buf.writeVarInt(this.getId());
        buf.writeVarInt(this.brainBlocks.size());
        for (BrainBlock block : this.brainBlocks.values()) {
            buf.writeUtf(block.opcode(), 64);
            buf.writeVarInt(block.x());
            buf.writeVarInt(block.y());
            buf.writeUUID(block.id());
            buf.writeVarInt(block.inputs().size());
            for (InputSlot slot : block.inputs()) {
                buf.writeUtf(slot.name(), 32); buf.writeUtf(slot.type().name(), 16);
                String value = slot.value() == null ? "" : slot.value();
                buf.writeVarInt(value.length());
                buf.writeUtf(value, Math.max(1, value.length()));
                writeUuid(buf, slot.blockId());
            }
            writeUuid(buf, block.next());
            writeUuid(buf, block.parent());
        }
    }

    public void writeInventoryClientData(RegistryFriendlyByteBuf buf) {
        buf.writeInt(this.getId());
    }

    private static void writeUuid(RegistryFriendlyByteBuf buf, UUID id) {
        buf.writeBoolean(id != null);
        if (id != null) { buf.writeUUID(id); }
    }

    public LinkedHashMap<UUID, BrainBlock> getBrainBlocks() {
        return (LinkedHashMap<UUID, BrainBlock>) this.brainBlocks.clone();
    }

    public void addBrainBlock(String opcode, int x, int y, UUID id) {
        if (this.brainBlocks.size() < 1024 && ModuleRegistry.contains(opcode)) {
            this.brainBlocks.put(id, new BrainBlock(opcode, x, y, id,
                    ModuleRegistry.createDefaultInputs(opcode), null, null));
        }
    }

    public void removeBrainBlock(UUID id) {
        this.brainBlocks.remove(id);
        LittleAnt.LOGGER.info("[AntEntity] removeBrainBlock, id: {}", id);
    }

    public void replaceBrainBlocks(Map<UUID, BrainBlock> blocks) {
        this.brainBlocks.clear();
        this.brainBlocks.putAll(blocks);
    }



    public void clearActiveGoals() {
        if (breakBlockGoal != null) breakBlockGoal.clearTarget();
        if (setBlockGoal != null) setBlockGoal.clearTarget();
    }

    public void runScript(String source) {
        this.brainBlocks.clear();
        String trimmed = source == null ? "" : source.stripLeading();
        if (trimmed.startsWith("{")) {
            this.brainBlocks.putAll(new JsonToModuleConverter().convert(JsonParser.parseString(source).getAsJsonObject()));
        } else {
            this.brainBlocks.putAll(new AntDslConverter().convert(source));
        }
        this.needAiRestart = true;
    }

    public void tickBrainProgram() {
        if (!level().isClientSide()) {
            antScriptInterpreter.loadProgram(this.brainBlocks);
            if (needAiRestart) {
                antScriptInterpreter.start();
                needAiRestart = false;
            }
            antScriptInterpreter.tick();
            // 每5个tick同步一次任务调试信息
            if (this.tickCount % 5 == 0 && this.level() instanceof ServerLevel serverLevel) {
                String foreground = TaskDebugCodec.encode(antScriptInterpreter.debugForeground());
                String background = TaskDebugCodec.encode(antScriptInterpreter.debugBackground());
                int debugHash = Objects.hash(foreground, background);
                if (debugHash != lastTaskDebugHash || this.tickCount - lastTaskDebugSyncTick >= 20) {
                    lastTaskDebugHash = debugHash;
                    lastTaskDebugSyncTick = this.tickCount;
                    SyncAntTaskDebugPayload payload = new SyncAntTaskDebugPayload(getId(), getName().getString(), foreground, background);
                    //LittleAnt.LOGGER.info("[AntEntity] tickBrainProgram, entityId: {}, name: {}, foreground: {}, background: {}", getId(), getName().getString(), foreground, background);
                    for (ServerPlayer player : serverLevel.players()) {
                        if (player.distanceToSqr(this) <= 32 * 32) PacketDistributor.sendToPlayer(player, payload);
                    }
                }
            }
        }
    }

    public AntBlackboard getBrainBlackboard() {
        return antScriptInterpreter.blackboard();
    }

    public void setIsProgrammingBrain(Player player, boolean isProgramming) {
        this.isProgrammingBrain = isProgramming;
        this.programmingPlayer = player;
        this.needAiRestart = true;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.30D)
                .add(Attributes.FOLLOW_RANGE, 64.0D)
                .add(Attributes.ATTACK_DAMAGE, 2.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.10D)
                .add(Attributes.MINING_EFFICIENCY, 0.0D);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnType,
            @Nullable SpawnGroupData groupData
    ) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, spawnType, groupData);
        this.antEntityGlobalData = AntEntityGlobalData.get((ServerLevel) level.getLevel());
        this.setCustomName(getRandomCharacterName());
        this.getEntityData().set(skinNameAccessor, getRandomSkinName());
        this.setCustomNameVisible(true);
        this.setPersistenceRequired();
        this.setCanPickUpLoot(true);


        LittleAnt.LOGGER.info("[AntEntity] finalizeSpawn, full custom name: {}", Objects.requireNonNull(getCustomName()).getString());
        return data;
    }

    @Override
    protected void registerGoals() {
        //break block init
        this.breakBlockGoal = new BreakBlockGoal(this, BlockPos.ZERO);
        this.breakBlockGoal.clearTarget();
        this.goalSelector.addGoal(1, this.breakBlockGoal);
        //set block init
        this.setBlockGoal = new SetBlockGoal(this, BlockPos.ZERO);
        this.setBlockGoal.clearTarget();
        this.goalSelector.addGoal(1, this.setBlockGoal);
        // Crafting is explicitly requested by the ant's AI, never selected automatically.
        this.useCraftingTableGoal = new UseCraftingTableGoal(this);
        this.goalSelector.addGoal(1, this.useCraftingTableGoal);
        this.useInventoryCraftingGoal = new UseInventoryCraftingGoal(this);
        this.goalSelector.addGoal(1, this.useInventoryCraftingGoal);
        this.useContainerGoal = new UseContainerGoal(this);
        this.goalSelector.addGoal(1, this.useContainerGoal);
        this.meleeAttackGoal = new EntityMeleeAttackGoal(this, null, true);
        this.goalSelector.addGoal(1, this.meleeAttackGoal);

        //this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        //this.goalSelector.addGoal(2, new BetterFloatGoal(this));

        //this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        //this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        //this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    // Assigns the next block this ant should path to and mine.
    public void setBreakTarget(BlockPos target) {
        if (this.breakBlockGoal != null) {
            this.breakBlockGoal.setTarget(target);
        }
    }
    public void setSetTarget(BlockPos target) {
        if (this.setBlockGoal != null) {
            this.setBlockGoal.setTarget(target);
        }
    }

    public void setContainerTarget(BlockPos target, UseContainerGoal.Operation operation, Item item, int slot, int amount) {
        if (this.useContainerGoal != null) this.useContainerGoal.setRequest(target, operation, item, slot, amount);
    }



    public void setCraftingTableInput(CraftingInput input, BlockPos craftingTablePos, int amountCrafted) {
        if (this.useCraftingTableGoal != null) {
            this.useCraftingTableGoal.setInput(input, craftingTablePos, amountCrafted);
        }
    }

    public void setInventoryCraftingInput(CraftingInput input, int amountCrafted) {
        if (this.useInventoryCraftingGoal != null) {
            this.useInventoryCraftingGoal.setInput(input, amountCrafted);
        }
    }

    public void setMeleeAttackTarget(LivingEntity target) {
        if (this.meleeAttackGoal != null) {
            this.meleeAttackGoal.setTarget(target);
        }
    }

    public BlockPos setFindBlockTarget(List<Block> blockToFind) {
        return this.findNearestBlock.setTarget(blockToFind);
    }
    public BlockPos setFindBlockEntityTarget(List<Block> blockEntity) {
        if(blockEntity != null && blockEntity.stream().anyMatch(b -> b.defaultBlockState().hasBlockEntity())){
            return this.findNearestBlockEntity.setTarget(blockEntity);
        }
        return null;
    }
    public int setFindEntityTarget(List<EntityType<?>> entityType) {
        return this.findNearestEntity.setTarget(entityType);
    }
    public BlockPos setFindDropTarget(List<Item> item) {
        return this.findNearestDrop.setTarget(item);
    }
    public BlockPos setFindPheromoneTarget(String pheromoneType) {
        return this.findNearestPheromone.setTarget(pheromoneType);
    }
    public List<BlockPos> setFindBlockListTarget(List<Block> block, int count) { return findBlockList.setTarget(block, count); }
    public List<Integer> setFindEntityListTarget(List<EntityType<?>> type, int count) { return findEntityList.setTarget(type, count); }
    public List<BlockPos> setFindDropListTarget(List<Item> item, int count) { return findDropList.setTarget(item, count); }
    public List<BlockPos> setFindBlockEntityListTarget(List<Block> block, int count) { return findBlockEntityList.setTarget(block, count); }
    public List<BlockPos> setFindPheromoneListTarget(String type, int count) { return findPheromoneList.setTarget(type, count); }

    public Set<String> getSurroundingPheromoneTypes() {
        return this.getSurroundingPheromoneTypes.setTarget();
    }

    public boolean hasContainerOpen(ContainerOpenersCounter container, BlockPos blockPos) {
        for (BlockPos openedPos : this.openedContainerPositions.keySet()) {
            if (openedPos.equals(blockPos)) return true;
            BlockState state = this.level().getBlockState(openedPos);
            if (state.getBlock() instanceof ChestBlock
                    && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE
                    && openedPos.relative(ChestBlock.getConnectedDirection(state)).equals(blockPos)) {
                return true;
            }
        }
        return false;
    }

    public void registerContainerOpen(BlockPos pos) {
        openedContainerPositions.merge(pos.immutable(), 1, Integer::sum);
    }

    public void unregisterContainerOpen(BlockPos pos) {
        BlockPos immutable = pos.immutable();
        openedContainerPositions.computeIfPresent(immutable, (ignored, count) -> count > 1 ? count - 1 : null);
    }

    public double getContainerInteractionRange() {
        return 4.0D;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(skinNameAccessor, "");
        builder.define(selectedItemAccessor, ItemStack.EMPTY);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        this.lastHurtTime = this.level().getGameTime();
        // 记录最后攻击者
        if (attacker instanceof LivingEntity) {
            this.setLastHurtBy((LivingEntity) attacker);
        }
        return super.hurt(source, amount);
    }

    private void setLastHurtBy(LivingEntity attacker) {
        this.lastHurtBy = attacker;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        // 自定义掉落逻辑
        for(int i = 0; i<inventory.getContainerSize(); i++){
            ItemStack itemStack = inventory.getItem(i);
            if(!itemStack.isEmpty()){
                this.spawnAtLocation(itemStack);
            }
        }
        inventory.clearContent();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack itemStack = this.getItemBySlot(slot);
            if (!itemStack.isEmpty()
                    && !EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                this.spawnAtLocation(itemStack);
                this.setItemSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        super.readAdditionalSaveData(input);
        this.getEntityData().set(skinNameAccessor, input.getString("skin_name"));
        this.readInventoryFromTag(input,this.level().registryAccess());
        this.selectedSlot = input.getInt("selected_slot");
        this.selectedSlot = Mth.clamp(this.selectedSlot, 0, INVENTORY_SIZE - 1);
        this.syncSelectedItem();
        this.foodData.readAdditionalSaveData(input);
        this.doubleFoodLevel = this.foodData.getFoodLevel();
        this.lastTimePos = this.blockPosition();
        this.antScriptInterpreter.blackboard().readPermanentData(input);
        this.brainBlocks.clear();
        ListTag brainBlocks = input.getList("BrainBlocks", Tag.TAG_COMPOUND);
        for (int i = 0; i < brainBlocks.size(); i++) {
            CompoundTag child = brainBlocks.getCompound(i);
            String opcode = child.getString("opcode");
            if (!opcode.isEmpty()) {
                UUID next = parseUuid(child.getString("next"));
                UUID parent = parseUuid(child.getString("parent"));
                UUID id = parseUuid(child.getString("id"));
                List<InputSlot> inputs = new ArrayList<>();
                ListTag inputsList = child.getList("Inputs", Tag.TAG_COMPOUND);
                for (int j = 0; j < inputsList.size(); j++) {
                    CompoundTag savedInput = inputsList.getCompound(j);
                    String name = savedInput.getString("name");
                    ValueType type;
                    try { type = ValueType.valueOf(savedInput.getString("type")); }
                    catch (IllegalArgumentException ignored) { type = ValueType.ANY; }
                    String value = savedInput.getString("value");
                    inputs.add(new InputSlot(name, type, value, parseUuid(savedInput.getString("block"))));
                }
                if (inputs.isEmpty()) inputs = ModuleRegistry.createDefaultInputs(opcode);
                if (id != null) this.brainBlocks.put(id, new BrainBlock(opcode, child.getInt("x"), child.getInt("y"), id, inputs, next, parent));
            }
        }
        LittleAnt.LOGGER.info("[AntEntity] read skin name from save data: {}", getSkinNameAccessor());
        // 读取库存, 打印列表
        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            LittleAnt.LOGGER.info("[AntEntity] read inventory from save data, slot: {}, itemStack: {}", slot, this.inventory.getItem(slot).getDisplayName());
        }
        // 如果没有皮肤，随机选择一个皮肤
        ensureSkinName();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        super.addAdditionalSaveData(output);
        output.putString("skin_name", getSkinNameAccessor());
        this.writeInventoryToTag(output,this.level().registryAccess());
        output.putInt("selected_slot", this.selectedSlot);
        this.foodData.addAdditionalSaveData(output);
        this.antScriptInterpreter.blackboard().writePermanentData(output);
        ListTag brainBlockList = new ListTag();

        for (BrainBlock block : this.brainBlocks.values()) {
            CompoundTag child = new CompoundTag();
            child.putString("opcode", block.opcode());
            child.putInt("x", block.x());
            child.putInt("y", block.y());
            child.putString("id", block.id().toString());
            if (block.next() != null) child.putString("next", block.next().toString());
            if (block.parent() != null) child.putString("parent", block.parent().toString());
            ListTag inputList = getInputList(block);
            child.put("Inputs", inputList);
            brainBlockList.add(child);
        }
        output.put("BrainBlocks", brainBlockList);
        LittleAnt.LOGGER.info("[AntEntity] write skin name to save data: {}", getSkinNameAccessor());
    }

    private static ListTag getInputList(BrainBlock block) {
        ListTag inputList = new ListTag();
        for (InputSlot input : block.inputs()) {
            CompoundTag savedInput = new CompoundTag();
            savedInput.putString("name", input.name());
            savedInput.putString("type", input.type().name());
            if (input.value() != null) savedInput.putString("value", input.value());
            if (input.blockId() != null) savedInput.putString("block", input.blockId().toString());
            inputList.add(savedInput);
        }
        return inputList;
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.isEmpty()) return null;
        try { return UUID.fromString(value); } catch (IllegalArgumentException ignored) { return null; }
    }

    @Override
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    public FoodData getFoodData() {
        return this.foodData;
    }

    /** Keeps interaction services independent from the private sync implementation. */
    public void syncSelectedItemNow() {
        this.syncSelectedItem();
    }

    @Override
    public void setItemInHand(InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            this.inventory.setItem(this.selectedSlot, stack);
            this.syncSelectedItem();
        } else {
            super.setItemInHand(hand, stack);
        }
    }

    /** FoodProperties feeds Player only; apply nutrition after the ant really finishes eating. */
    @Override
    protected void completeUsingItem() {
        boolean potion = this.getUseItem().get(DataComponents.POTION_CONTENTS) != null;
        InteractionHand usedHand = this.getUsedItemHand();
        super.completeUsingItem();
        // PotionItem only consumes its stack for Player. Ant still receives the
        // effects through vanilla, then performs the missing container exchange.
        if (potion) {
            ItemStack remaining = this.getItemInHand(usedHand);
            remaining.shrink(1);
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (remaining.isEmpty()) {
                this.setItemInHand(usedHand, bottle);
            } else {
                ItemStack remainder = this.inventory.addItem(bottle);
                if (!remainder.isEmpty()) this.spawnAtLocation(remainder);
            }
        }
        this.syncSelectedItem();
    }

    @Override
    public ItemStack eat(Level level, ItemStack stack, FoodProperties food) {
        this.foodData.eat(food);
        this.doubleFoodLevel = this.foodData.getFoodLevel();
        ItemStack remaining = super.eat(level, stack, food);
        if (food.usingConvertsTo().isEmpty()) return remaining;
        ItemStack container = food.usingConvertsTo().get().copy();
        if (remaining.isEmpty()) return container;
        ItemStack remainder = this.inventory.addItem(container);
        if (!remainder.isEmpty()) this.spawnAtLocation(remainder);
        return remaining;
    }

    @Override
    public @Nullable SlotAccess getSlot(int slot) {
        int inventorySlot = slot - INVENTORY_SLOT_OFFSET;
        return inventorySlot >= 0 && inventorySlot < INVENTORY_SIZE
                ? SlotAccess.forContainer(this.inventory, inventorySlot)
                : super.getSlot(slot);
    }

    @Override
    protected void pickUpItem(ItemEntity itemEntity) {
        InventoryCarrier.pickUpItem(this, this, itemEntity);
        this.syncSelectedItem();
    }

    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {
        //LittleAnt.LOGGER.info("[AntEntity] wantsToPickUp, itemStack: {}", itemStack.getDisplayName());
        return this.inventory.canAddItem(itemStack);
    }

    /**
     * Consumes one food item from the ant's inventory when it is hungry.
     * AI goals can call this method when they decide that eating is appropriate.
     */
    public boolean eatFromInventory() {
        if (!this.foodData.needsFood() || this.inventory == null) {
            return false;
        }

        for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
            ItemStack itemStack = this.inventory.getItem(slot);
            FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
            if (foodProperties != null) {
                this.foodData.eat(foodProperties);
                this.doubleFoodLevel = this.foodData.getFoodLevel();
                itemStack.shrink(1);
                this.selectedSlot = slot;
                this.syncSelectedItem();
                return true;
            }
        }

        return false;
    }

    private Component getRandomCharacterName() {
        if (this.antEntityGlobalData == null) {
            this.antEntityGlobalData = this.level() instanceof ServerLevel serverLevel
                    ? AntEntityGlobalData.get(serverLevel)
                    : new AntEntityGlobalData();
        }
        // 从 CHARACTER_NAMES 中随机选择一个名字
        LinkedHashMap<String, Integer> CHARACTER_NAMES = antEntityGlobalData.getCharacterNames();
        int index = (int) (Math.random() * CHARACTER_NAMES.size());
        String selectedName = CHARACTER_NAMES.keySet().toArray(new String[0])[index];
        this.antEntityGlobalData.addNameCount(selectedName);
        if (CHARACTER_NAMES.get(selectedName) >= 2) {
            selectedName += CHARACTER_NAMES.get(selectedName);
        }

        LittleAnt.LOGGER.info("[AntEntity] random new character name: {}", selectedName);
        return Component.literal(selectedName);
    }

    private String getRandomSkinName() {
        if (this.antEntityGlobalData == null) {
            this.antEntityGlobalData = this.level() instanceof ServerLevel serverLevel
                    ? AntEntityGlobalData.get(serverLevel)
                    : new AntEntityGlobalData();
        }
        // 从 SKIN_NAMES 中随机选择一个皮肤
        int skinIndex = (int) (Math.random() * antEntityGlobalData.getSkinNames().length);
        LittleAnt.LOGGER.info("[AntEntity] random new skin name: {}", getSkinNameAccessor());
        return antEntityGlobalData.getSkinNames()[skinIndex];
    }

    public void ensureSkinName() {
        if (getSkinNameAccessor().isEmpty()) {
            this.getEntityData().set(skinNameAccessor, getRandomSkinName());
        }
    }

    public String getSkinNameAccessor() {
        return this.getEntityData().get(skinNameAccessor);
    }

    @Override
    public void aiStep() {
        this.updateSwingTime();
        super.aiStep();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {
        if(slot == EquipmentSlot.MAINHAND){
            // The inventory itself is not network-synchronized.  Clients must use
            // this synced snapshot when rendering the selected inventory slot.
            if(this.inventory == null){
                return ItemStack.EMPTY;
            }

            return this.level().isClientSide()
                    ? this.getEntityData().get(selectedItemAccessor)
                    : this.inventory.getItem(this.selectedSlot);
        }
        else if(slot == EquipmentSlot.OFFHAND){
            return ItemStack.EMPTY;
        }
        return super.getItemBySlot(slot);
    }

    public void getDownInWater() {
        this.sinkInFluid(NeoForgeMod.WATER_TYPE.value());
    }

    public void giveItem(ItemStack item){
        this.inventory.setItem(this.selectedSlot, item.copy());
        this.syncSelectedItem();
    }

    public int getSelectedSlot() {
        return this.selectedSlot;
    }

    public void setSelectedSlot(int slot) {
        this.selectedSlot = Mth.clamp(slot, 0, INVENTORY_SIZE - 1);
        this.syncSelectedItem();
    }

    private void syncSelectedItem() {
        if (!this.level().isClientSide()) {
            ItemStack selectedItem = this.inventory.getItem(this.selectedSlot);
            if (!ItemStack.matches(this.getEntityData().get(selectedItemAccessor), selectedItem)) {
                this.getEntityData().set(selectedItemAccessor, selectedItem.copy());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        this.syncSelectedItem();
        long currentTime = this.level().getGameTime();
        if(this.lastTimePos == null){
            this.lastTimePos = this.blockPosition();
            this.doubleFoodLevel = this.foodData.getFoodLevel();
        }
        // 每1200个tick更新一次食物等级
        if(currentTime % 1200 == 0){
            doubleFoodLevel -= Mth.sqrt((float)this.distanceToSqr(this.lastTimePos.getX(), this.lastTimePos.getY(), this.lastTimePos.getZ())) / 100.0D;
            doubleFoodLevel = Mth.clamp(doubleFoodLevel, 0.0D, 20.0D);
            this.foodData.setFoodLevel((int)doubleFoodLevel);
            this.lastTimePos = this.blockPosition();
        }

        if(currentTime % 20 == 0 && this.foodData.getFoodLevel() <= 0 && !this.level().isClientSide()) {
            this.hurt(this.damageSources().starve(), 2.0F);
        }

        // 执行脚本
        if(!this.isProgrammingBrain){
            tickBrainProgram();
        }

        // 更新姿势
        if (!this.level().isClientSide()) {
            this.updatePlayerPose();
        }
    }

    private void updatePlayerPose() {
        Pose desiredPose = this.getDesiredPose();
        if (desiredPose == Pose.CROUCHING) {
            // Unlike Player, Ant has no vanilla shift key state.  Its script flag
            // is the authoritative crouch state, and setPose() synchronizes the
            // pose and refreshes dimensions through Entity's data callback.
            this.setPose(desiredPose);
            return;
        }
        if (desiredPose == Pose.STANDING) {
            if (this.canAntFitWithinBlocksAndEntitiesWhen(Pose.STANDING)) this.setPose(Pose.STANDING);
            else if (this.canAntFitWithinBlocksAndEntitiesWhen(Pose.CROUCHING)) this.setPose(Pose.CROUCHING);
            else this.setPose(Pose.SWIMMING);
            return;
        }
        if (this.canAntFitWithinBlocksAndEntitiesWhen(Pose.SWIMMING)) {
            this.setPose(desiredPose);
        }
    }

    private boolean canAntFitWithinBlocksAndEntitiesWhen(Pose newPose) {
        return this.level().noCollision(this, this.getDimensions(newPose).makeBoundingBox(this.position()).deflate(1.0E-7));
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return switch (pose) {
            case CROUCHING -> CROUCHING_DIMENSIONS;
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> SWIMMING_DIMENSIONS;
            default -> super.getDefaultDimensions(pose);
        };
    }

    private Pose getDesiredPose() {
        if (this.isSleeping()) {
            return Pose.SLEEPING;
        } else if (this.isSwimming()) {
            return Pose.SWIMMING;
        } else if (this.isFallFlying()) {
            return Pose.FALL_FLYING;
        } else if (this.isAutoSpinAttack()) {
            return Pose.SPIN_ATTACK;
        } else {
            return this.isCrouching ? Pose.CROUCHING : Pose.STANDING;
        }
    }

    @Override
    public boolean hasLineOfSight(Entity target) {
        return super.hasLineOfSight(target);
    }

    public ItemEntity drop(ItemStack droppedItem, boolean dropAround, boolean includeThrowerName) {
        if (droppedItem.isEmpty()) {
            return null;
        } else {
            if (this.level().isClientSide) {
                this.swing(InteractionHand.MAIN_HAND);
            }

            double d0 = this.getEyeY() - 0.3F;
            ItemEntity itementity = new ItemEntity(this.level(), this.getX(), d0, this.getZ(), droppedItem);
            itementity.setPickUpDelay(40);
            if (includeThrowerName) {
                itementity.setThrower(this);
            }

            if (dropAround) {
                float f = this.random.nextFloat() * 0.5F;
                float f1 = this.random.nextFloat() * (float) (Math.PI * 2);
                itementity.setDeltaMovement((double)(-Mth.sin(f1) * f), 0.2F, (double)(Mth.cos(f1) * f));
            } else {
                float f7 = 0.3F;
                float f8 = Mth.sin(this.getXRot() * (float) (Math.PI / 180.0));
                float f2 = Mth.cos(this.getXRot() * (float) (Math.PI / 180.0));
                float f3 = Mth.sin(this.getYRot() * (float) (Math.PI / 180.0));
                float f4 = Mth.cos(this.getYRot() * (float) (Math.PI / 180.0));
                float f5 = this.random.nextFloat() * (float) (Math.PI * 2);
                float f6 = 0.02F * this.random.nextFloat();
                itementity.setDeltaMovement(
                        (double)(-f3 * f2 * 0.3F) + Math.cos((double)f5) * (double)f6,
                        (double)(-f8 * 0.3F + 0.1F + (this.random.nextFloat() - this.random.nextFloat()) * 0.1F),
                        (double)(f4 * f2 * 0.3F) + Math.sin((double)f5) * (double)f6
                );
            }

            return itementity;
        }
    }
}
