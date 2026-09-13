package net.tianyang928.littleant.gui;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.tianyang928.littleant.entity.AntEntity;


/**
 * The ant's nine carried-item slots and its four wearable equipment slots.
 *
 * <p>The normal container synchronizer sends slot updates to the client, so the
 * inventory must be exposed as {@link Slot}s instead of copied into a custom
 * network payload.</p>
 */
public class AntInventoryMenu extends AbstractContainerMenu {
    private static final int ANT_SLOT_COUNT = AntEntity.INVENTORY_SIZE + 4;
    // 1.21.1 moved armor placeholders from container/slot/* to item/empty_armor_slot_*.
    private static final ResourceLocation EMPTY_HELMET_SLOT = InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
    private static final ResourceLocation EMPTY_CHESTPLATE_SLOT = InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
    private static final ResourceLocation EMPTY_LEGGINGS_SLOT = InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
    private static final ResourceLocation EMPTY_BOOTS_SLOT = InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;

    public final AntEntity ant;

    /**
     * Client-side factory constructor. Slot contents are populated by the
     * standard menu synchronization packets immediately after construction.
     */
    public AntInventoryMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        super(ModMenus.ANT_INVENTORY_MENU.get(), containerId);
        int entityId = data.readInt();
        this.ant = playerInventory.player.level().getEntity(entityId) instanceof AntEntity ? (AntEntity) playerInventory.player.level().getEntity(entityId) : null;
        addClientEquipmentSlots();
        addInventorySlots(new SimpleContainer(AntEntity.INVENTORY_SIZE));
        addPlayerInventorySlots(playerInventory, 8, 92);
    }

    /** Server-side constructor used by AntEntity#createMenu. */
    public AntInventoryMenu(int containerId, Inventory playerInventory, AntEntity ant) {
        this(containerId, playerInventory, ant, ant.getInventory());
    }

    private AntInventoryMenu(int containerId, Inventory playerInventory, AntEntity ant, Container antInventory) {
        super(ModMenus.ANT_INVENTORY_MENU.get(), containerId);
        this.ant = ant;
        checkContainerSize(antInventory, AntEntity.INVENTORY_SIZE);

        addServerEquipmentSlot(EquipmentSlot.HEAD, 8, 18, EMPTY_HELMET_SLOT);
        addServerEquipmentSlot(EquipmentSlot.CHEST, 8, 36, EMPTY_CHESTPLATE_SLOT);
        addServerEquipmentSlot(EquipmentSlot.LEGS, 8, 54, EMPTY_LEGGINGS_SLOT);
        addServerEquipmentSlot(EquipmentSlot.FEET, 8, 72, EMPTY_BOOTS_SLOT);
        addInventorySlots(antInventory);
        addPlayerInventorySlots(playerInventory, 8, 92);
    }

    private void addInventorySlots(Container antInventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                this.addSlot(new Slot(antInventory, column + row * 3, 81 + column * 18, 18 + row * 18));
            }
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory, int x, int y) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, x + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, x + column * 18, y + 58));
        }
    }

    private void addServerEquipmentSlot(EquipmentSlot equipmentSlot, int x, int y, ResourceLocation emptySlotSprite) {
        Container equipment = new EquipmentSlotContainer(this.ant, equipmentSlot);
        this.addSlot(new Slot(equipment, 0, x, y) {
            @Override
            public int getMaxStackSize() {
                return 1;
            }

            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.canEquip(equipmentSlot, AntInventoryMenu.this.ant);
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, emptySlotSprite);
            }
        });
    }

    private void addClientEquipmentSlots() {
        SimpleContainer equipment = new SimpleContainer(4);
        this.addSlot(new Slot(equipment, 0, 8, 18) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_HELMET_SLOT); }
            @Override public boolean mayPlace(ItemStack itemStack) {
                    return itemStack.is(ItemTags.HEAD_ARMOR);
                }
            }
        );
        this.addSlot(new Slot(equipment, 1, 8, 36) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_CHESTPLATE_SLOT); }
            @Override public boolean mayPlace(ItemStack itemStack) {
                    return itemStack.is(ItemTags.CHEST_ARMOR);
                }
            }
        );
        this.addSlot(new Slot(equipment, 2, 8, 54) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_LEGGINGS_SLOT); }
            @Override public boolean mayPlace(ItemStack itemStack) {
                    return itemStack.is(ItemTags.LEG_ARMOR);
                }
            }
        );
        this.addSlot(new Slot(equipment, 3, 8, 72) {
            @Override public int getMaxStackSize() { return 1; }
            @Override public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() { return Pair.of(InventoryMenu.BLOCK_ATLAS, EMPTY_BOOTS_SLOT); }
            @Override public boolean mayPlace(ItemStack itemStack) {
                    return itemStack.is(ItemTags.FOOT_ARMOR);
                }
            }
        );
    }

    @Override
    public boolean stillValid(Player player) {
        return this.ant == null || (this.ant.isAlive() && player.distanceTo(this.ant) <= 4.0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < ANT_SLOT_COUNT) {
            if (!this.moveItemStackTo(stack, ANT_SLOT_COUNT, this.slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.moveItemStackTo(stack, 4, ANT_SLOT_COUNT, false)
                && !this.moveItemStackTo(stack, 0, 4, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private static final class EquipmentSlotContainer extends SimpleContainer {
        private final AntEntity ant;
        private final EquipmentSlot equipmentSlot;

        private EquipmentSlotContainer(AntEntity ant, EquipmentSlot equipmentSlot) {
            super(1);
            this.ant = ant;
            this.equipmentSlot = equipmentSlot;
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? ant.getItemBySlot(equipmentSlot) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (slot != 0) return ItemStack.EMPTY;
            ItemStack stack = ant.getItemBySlot(equipmentSlot);
            ItemStack removed = stack.split(amount);
            ant.setItemSlot(equipmentSlot, stack);
            return removed;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            if (slot != 0) return ItemStack.EMPTY;
            ItemStack stack = ant.getItemBySlot(equipmentSlot);
            ant.setItemSlot(equipmentSlot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot == 0) ant.setItemSlot(equipmentSlot, stack);
        }
    }

}
