package ca.teamdman.sfm.common.container;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.logging.TranslatableLogEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayDeque;

public class ManagerContainerMenu extends Container {
    public ManagerBlockEntity entity;
    public InventoryPlayer playerInventory;
    public BlockPos MANAGER_POSITION;
    public ArrayDeque<TranslatableLogEvent> logs;
    public String logLevel;
    public boolean isLogScreenOpen = false;
    public String program;
    public ManagerBlockEntity.State state;
    public long[] tickTimeNanos;

    public ManagerContainerMenu(int ID, EntityPlayer entityPlayer, ManagerBlockEntity tileEntity, BlockPos pos) {
        this.windowId = ID;
        this.entity = tileEntity;
        this.playerInventory = entityPlayer.inventory;
        this.MANAGER_POSITION = pos;
        this.program = tileEntity.getProgramStringOrEmptyIfNull();
        this.logLevel = tileEntity.logger.getLogLevel().name();
        this.state = tileEntity.getState();
        this.tickTimeNanos = tileEntity.getTickTimeNanos();
        this.logs = new ArrayDeque<>();


        // Add disk slot
        this.addSlotToContainer(new Slot(entity, 0, 15, 47) {
            @Override
            public int getSlotStackLimit() {
                return 1;
            }

            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof DiskItem;
            }
        });

        // Add player inventory slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        // Add player hotbar slots
        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(playerInventory, k, 8 + k * 18, 142));
        }
    }


    public static void encode(
            ManagerBlockEntity manager,
            PacketBuffer buf
    ) {
        buf.writeBlockPos(manager.getPos());
        buf.writeString(manager.getProgramStringOrEmptyIfNull());
        buf.writeString(
                manager.logger.getLogLevel().name()
//                ServerboundManagerSetLogLevelPacket.MAX_LOG_LEVEL_NAME_LENGTH
        );
        buf.writeEnumValue(manager.getState());
        buf.writeLongArray(manager.getTickTimeNanos());
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return entity.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = this.inventorySlots.get(index);
        if (!slot.getHasStack()) return ItemStack.EMPTY;

        ItemStack content = slot.getStack();
        ItemStack result = content.copy();

        if (index < entity.getSizeInventory()) {
            if (!this.mergeItemStack(content, entity.getSizeInventory(), this.inventorySlots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!this.mergeItemStack(content, 0, entity.getSizeInventory(), false)) {
            return ItemStack.EMPTY;
        }

        if (content.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }

        return result;
    }

    public ItemStack getDisk() {
        return this.getSlot(0).getStack();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
//        for (int i = 0; i < this.inventorySlots.size(); ++i) {
        ItemStack newStack = this.inventorySlots.get(0).getStack();  // 当前最新物品
        ItemStack oldStack = this.inventoryItemStacks.get(0);       // 旧物品快照
        // 比较物品是否变化
        if (!ItemStack.areItemStacksEqual(oldStack, newStack)) {
            this.entity.rebuildProgramAndUpdateDisk();
        }
    }
}
