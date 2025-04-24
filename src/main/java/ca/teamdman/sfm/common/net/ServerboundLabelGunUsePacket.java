package ca.teamdman.sfm.common.net;


import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.cablenetwork.CableNetwork;
import ca.teamdman.sfm.common.cablenetwork.CableNetworkManager;
import ca.teamdman.sfm.common.item.LabelGunItem;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.util.SFMDirections;
import ca.teamdman.sfm.common.util.SFMStreamUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ServerboundLabelGunUsePacket implements SFMMessage<ServerboundLabelGunUsePacket, IMessage> {
    private EnumHand hand;
    private BlockPos pos;
    private boolean isCtrlKeyDown;
    private boolean isPickBlockModifierKeyDown;
    private boolean isShiftKeyDown;

    public ServerboundLabelGunUsePacket() {
    }

    public ServerboundLabelGunUsePacket(EnumHand hand, BlockPos pos, boolean ctrlKeyDown, boolean pickBlock, boolean shiftKeyDown) {
        this.hand=hand;
        this.pos=pos;
        this.isCtrlKeyDown=ctrlKeyDown;
        this.isPickBlockModifierKeyDown=pickBlock;
        this.isShiftKeyDown=shiftKeyDown;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.hand = packetBuffer.readEnumValue(EnumHand.class);
        this.pos = BlockPos.fromLong(packetBuffer.readLong());
        this.isCtrlKeyDown = packetBuffer.readBoolean();
        this.isPickBlockModifierKeyDown = packetBuffer.readBoolean();
        this.isShiftKeyDown = packetBuffer.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeEnumValue(this.hand);
        packetBuffer.writeLong(this.pos.toLong());
        packetBuffer.writeBoolean(this.isCtrlKeyDown);
        packetBuffer.writeBoolean(this.isPickBlockModifierKeyDown);
        packetBuffer.writeBoolean(this.isShiftKeyDown);
    }



    @Override
    public IMessage onMessage(ServerboundLabelGunUsePacket msg, MessageContext context) {
        EntityPlayerMP sender = context.getServerHandler().player;
        if (sender == null) {
            return null;
        }
        ItemStack stack = sender.getHeldItem(msg.hand);
        World world = sender.world;
        if (!(stack.getItem() instanceof LabelGunItem)) {
            return null;
        }

        LabelPositionHolder gunLabels = LabelPositionHolder.from(stack).toOwned();
        BlockPos position = msg.pos;

        // target is a manager, perform push or pull action
        if (world.getTileEntity(position) instanceof ManagerBlockEntity ) {
            ManagerBlockEntity tileEntity = (ManagerBlockEntity) world.getTileEntity(position);
            @Nullable ItemStack disk = tileEntity.getDisk();
            if (disk != null) {
                if (msg.isShiftKeyDown) {
                    // start with labels from disk
                    LabelPositionHolder newLabels = LabelPositionHolder.from(disk).toOwned();
                    // ensure script-referenced labels are included
                    tileEntity.getReferencedLabels().forEach(newLabels::addReferencedLabel);
                    // save to gun
                    newLabels.save(stack);
                    // give feedback to player
                    sender.sendMessage(LocalizationKeys.LABEL_GUN_CHAT_PULLED.getComponent());
                } else {
                    // save gun labels to disk
                    gunLabels.save(disk);
                    // rebuild program
                    tileEntity.rebuildProgramAndUpdateDisk();
                    // mark manager dirty
                    tileEntity.markDirty();
                    // give feedback to player
                    sender.sendMessage(LocalizationKeys.LABEL_GUN_CHAT_PUSHED.getComponent());
                }
            }
            return null;
        }

        // target is not a manager, we will perform label toggle
        String activeLabel = LabelGunItem.getActiveLabel(stack);
        if (msg.isShiftKeyDown) {
            // clear all labels from pos
            gunLabels.removeAll(position);
        } else if (!activeLabel.isEmpty()) {
            if (msg.isCtrlKeyDown) {
                // find all connected inventories of the same block type and toggle the label on all of them
                // if any of them don't have it, apply it, otherwise strip from all

                // find all cable positions so that we only include inventories adjacent to a cable
                Set<BlockPos> cablePositions = CableNetworkManager
                        .getNetworksForLevel(world)
                        .flatMap(CableNetwork::getCablePositions)
                        .collect(Collectors.toSet());

                // get the block type of the target position
                Block targetBlock = world.getBlockState(position).getBlock();

                // predicate to check if a position is adjacent to a cable
                Predicate<BlockPos> isAdjacentToCable = p -> Arrays
                        .stream(SFMDirections.DIRECTIONS)
                        .anyMatch(d -> cablePositions.contains(p.offset(d.NORTH)));

                // get positions of all connected blocks of the same type
                List<BlockPos> positions = SFMStreamUtils
                        .<BlockPos, BlockPos>getRecursiveStream((current, nextQueue, results) -> {
                            results.accept(current);
                            SFMStreamUtils.get3DNeighboursIncludingKittyCorner(current)
                                    .filter(p -> world.getBlockState(p).getBlock() == targetBlock)
                                    .filter(isAdjacentToCable)
                                    .forEach(nextQueue);
                        }, position)
                        .collect(Collectors.toList());
                // check if any of the positions are missing the label
                HashSet<BlockPos> existing = new HashSet<>(gunLabels.getPositions(activeLabel));
                boolean anyMissing = positions.stream().anyMatch(p -> !existing.contains(p));

                // apply or strip label from all positions
                if (anyMissing) {
                    gunLabels.addAll(activeLabel, positions);
                } else {
                    positions.forEach(p -> gunLabels.remove(activeLabel, p));
                }
            } else if (msg.isPickBlockModifierKeyDown) {
                // set one of the labels from the block as active
                ArrayList<String> labels = new ArrayList<>(gunLabels.getLabels(position));
                labels.sort(Comparator.naturalOrder());
                if (labels.isEmpty()) return null;
                int index = (labels.indexOf(activeLabel) + 1) % labels.size();
                String nextLabel = labels.get(index);
                LabelGunItem.setActiveLabel(stack, nextLabel);
            } else {
                gunLabels.toggle(activeLabel, position);
            }
        }

        // write changes to label gun stack
        gunLabels.save(stack);
        return null;
    }
}
