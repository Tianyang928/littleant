package net.tianyang928.littleant.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.tianyang928.littleant.entity.AntEntity;

import javax.annotation.Nullable;
import java.util.EnumSet;

/** Moves a requested item type between an ant inventory and one container slot. */
public class UseContainerGoal extends Goal {
    public enum Operation { PUT, TAKE }

    private static final double REACH_DISTANCE_SQR = 16.0D;
    private static final int OPEN_ANIMATION_TICKS = 3;
    private final AntEntity ant;
    @Nullable private BlockPos containerPos;
    @Nullable private Item item;
    @Nullable private Path path;
    @Nullable private Container openedContainer;
    private Operation operation = Operation.PUT;
    private int containerSlot;
    private int amount;
    private int openTicks;
    private boolean hasRequest;

    public UseContainerGoal(AntEntity ant) {
        this.ant = ant;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    public void setRequest(BlockPos pos, Operation operation, Item item, int slot, int amount) {
        closeOpenedContainer();
        this.containerPos = pos.immutable();
        this.operation = operation;
        this.item = item;
        this.containerSlot = slot;
        this.amount = amount;
        this.path = null;
        this.openTicks = 0;
        this.hasRequest = true;
    }

    public void clearRequest() {
        closeOpenedContainer();
        this.containerPos = null;
        this.item = null;
        this.path = null;
        this.amount = 0;
        this.openTicks = 0;
        this.hasRequest = false;
    }

    @Override
    public boolean canUse() {
        Container container = getContainer();
        if (!hasValidRequest(container)) {
            clearRequest();
            return false;
        }
        if (isReachable()) return true;
        this.path = this.ant.getNavigation().createPath(this.containerPos, 2, 64);
        return this.path != null && !this.path.isDone();
    }

    @Override
    public void start() {
        if (!isReachable() && this.path != null) this.ant.getNavigation().moveTo(this.path, this.ant.speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return hasValidRequest(getContainer()) && (isReachable() || !this.ant.getNavigation().isDone());
    }

    @Override
    public void stop() {
        this.ant.getNavigation().stop();
        closeOpenedContainer();
    }

    @Override
    public void tick() {
        if (this.containerPos == null) return;
        this.ant.getLookControl().setLookAt(this.containerPos.getX() + 0.5D, this.containerPos.getY() + 0.5D, this.containerPos.getZ() + 0.5D);
        if (!isReachable()) {
            if (this.ant.getNavigation().isDone()) {
                this.path = this.ant.getNavigation().createPath(this.containerPos, 2, 64);
                if (this.path != null) this.ant.getNavigation().moveTo(this.path, this.ant.speedModifier);
            }
            return;
        }

        Container container = getContainer();
        if (hasValidRequest(container)) {
            if (this.openedContainer == null) {
                // FakePlayer is not retained by ContainerOpenersCounter's real
                // player scan.  Open the lid through the block event instead of
                // entering a counter that will later underflow on stopOpen.
                setContainerAnimation(true);
                this.openedContainer = container;
                return;
            }
            if (++this.openTicks < OPEN_ANIMATION_TICKS) return;
            if (this.operation == Operation.PUT) putIntoContainer(container);
            else takeFromContainer(container);
            this.ant.swing(InteractionHand.MAIN_HAND, true);
        }
        clearRequest();
    }

    private boolean hasValidRequest(@Nullable Container container) {
        return this.hasRequest && this.containerPos != null && this.item != null && this.amount > 0 && container != null
                && this.containerSlot >= 0 && this.containerSlot < container.getContainerSize();
    }

    @Nullable
    private Container getContainer() {
        if (this.containerPos == null || !this.ant.level().hasChunkAt(this.containerPos)) return null;
        BlockState state = this.ant.level().getBlockState(this.containerPos);
        if (state.getBlock() instanceof ChestBlock chestBlock) {
            return ChestBlock.getContainer(chestBlock, state, this.ant.level(), this.containerPos, false);
        }
        if (state.getBlock() instanceof WorldlyContainerHolder holder) {
            return holder.getContainer(state, this.ant.level(), this.containerPos);
        }
        BlockEntity blockEntity = this.ant.level().getBlockEntity(this.containerPos);
        return blockEntity instanceof Container container ? container : null;
    }

//    /** Kept for compatibility with the old container-user integration. */
//    public boolean isContainerOpenAt(BlockPos pos) {
//        if (this.openedContainer == null || this.containerPos == null) return false;
//        if (this.containerPos.equals(pos)) return true;
//        BlockState state = this.ant.level().getBlockState(this.containerPos);
//        return state.getBlock() instanceof ChestBlock
//                && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE
//                && ChestBlock.getConnectedBlockPos(this.containerPos, state).equals(pos);
//    }

    private void closeOpenedContainer() {
        if (this.openedContainer != null) {
            if (this.containerPos != null && this.ant.level().hasChunkAt(this.containerPos)) {
                setContainerAnimation(false);
            }
            this.openedContainer = null;
        }
    }

    private boolean isReachable() {
        return this.containerPos != null && this.ant.distanceToSqr(
                this.containerPos.getX() + 0.5D, this.containerPos.getY() - 0.5D, this.containerPos.getZ() + 0.5D) <= REACH_DISTANCE_SQR;
    }

    private void setContainerAnimation(boolean open) {
        if (this.containerPos == null) return;
        BlockState state = this.ant.level().getBlockState(this.containerPos);
        this.ant.level().blockEvent(this.containerPos, state.getBlock(), 1, open ? 1 : 0);
        if (state.getBlock() instanceof ChestBlock
                && state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            BlockPos otherPos = this.containerPos.relative(ChestBlock.getConnectedDirection(state));
            BlockState otherState = this.ant.level().getBlockState(otherPos);
            this.ant.level().blockEvent(otherPos, otherState.getBlock(), 1, open ? 1 : 0);
        }
    }

    private void putIntoContainer(Container container) {
        ItemStack destination = container.getItem(this.containerSlot);
        ItemStack example = new ItemStack(this.item);
        if (!container.canPlaceItem(this.containerSlot, example)
                || (!destination.isEmpty() && !ItemStack.isSameItemSameComponents(destination, example))) return;
        int room = Math.min(container.getMaxStackSize(example), example.getMaxStackSize()) - destination.getCount();
        int remaining = Math.min(this.amount, Math.max(0, room));
        for (int slot = 0; slot < this.ant.getInventory().getContainerSize() && remaining > 0; slot++) {
            ItemStack source = this.ant.getInventory().getItem(slot);
            if (!ItemStack.isSameItemSameComponents(source, example)) continue;
            ItemStack removed = this.ant.getInventory().removeItem(slot, Math.min(remaining, source.getCount()));
            if (destination.isEmpty()) destination = removed;
            else destination.grow(removed.getCount());
            remaining -= removed.getCount();
        }
        container.setItem(this.containerSlot, destination);
        container.setChanged();
    }

    private void takeFromContainer(Container container) {
        ItemStack source = container.getItem(this.containerSlot);
        if (source.isEmpty() || source.getItem() != this.item
                || !container.canTakeItem(this.ant.getInventory(), this.containerSlot, source)) return;
        int movable = Math.min(Math.min(this.amount, source.getCount()), inventoryCapacityFor(source, this.amount));
        if (movable <= 0) return;
        ItemStack remainder = this.ant.getInventory().addItem(container.removeItem(this.containerSlot, movable));
        if (!remainder.isEmpty()) {
            ItemStack current = container.getItem(this.containerSlot);
            if (current.isEmpty()) container.setItem(this.containerSlot, remainder);
            else if (ItemStack.isSameItemSameComponents(current, remainder)) {
                current.grow(remainder.getCount());
                container.setItem(this.containerSlot, current);
            }
        }
        container.setChanged();
    }

    private int inventoryCapacityFor(ItemStack stack, int limit) {
        int capacity = 0;
        for (int slot = 0; slot < this.ant.getInventory().getContainerSize() && capacity < limit; slot++) {
            ItemStack existing = this.ant.getInventory().getItem(slot);
            if (existing.isEmpty()) capacity += Math.min(stack.getMaxStackSize(), limit - capacity);
            else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                capacity += Math.min(existing.getMaxStackSize() - existing.getCount(), limit - capacity);
            }
        }
        return capacity;
    }
}
