package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMItems;
import ca.teamdman.sfml.ast.Program;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundLabelInspectionRequestPacket implements SFMMessage<ServerboundLabelInspectionRequestPacket, IMessage> {
    private static final int MAX_RESULTS_LENGTH = 20480;
    public String label;

    public ServerboundLabelInspectionRequestPacket() {
    }

    public ServerboundLabelInspectionRequestPacket(String name) {
        this.label=name;
    }


    @Override
    public IMessage onMessage(ServerboundLabelInspectionRequestPacket msg, MessageContext context)
    {
        SFMPacketHandlingContext sfmPacketHandlingContext = new SFMPacketHandlingContext(context);
        // we don't know if the player has the program edit screen open from a manager or a disk in hand
        EntityPlayerMP player = context.getServerHandler().player;
        if (player == null) return null;
        SFM.LOGGER.info("Received label inspection request packet from player {}", player.getUUID(context.getServerHandler().player.getGameProfile()));
        LabelPositionHolder labelPositionHolder;
        if (player.openContainer instanceof ManagerContainerMenu ) {
            ManagerContainerMenu mcm = (ManagerContainerMenu) player.openContainer;
            SFM.LOGGER.info("Player is using a manager container menu - will append additional info to payload");
//            todo inventorySlots可能是
            labelPositionHolder = LabelPositionHolder.from(mcm.inventorySlots.get(0).getStack());
        } else {
            if (player.getHeldItemMainhand().getItem()==SFMItems.DISK_ITEM) {
                labelPositionHolder = LabelPositionHolder.from(player.getHeldItemMainhand());
            } else if (player.getHeldItemOffhand().getItem()==SFMItems.DISK_ITEM) {
                labelPositionHolder = LabelPositionHolder.from(player.getHeldItemOffhand());
            } else {
                labelPositionHolder = null;
            }
        }
        if (labelPositionHolder == null) {
            SFM.LOGGER.info("Label holder wasn't found - aborting");
            return null;
        }
        SFM.LOGGER.info("building payload");
        StringBuilder payload = new StringBuilder();
        payload.append("-- Positions for label \"").append(msg.label).append("\" --\n");
        payload.append(labelPositionHolder.getPositions(msg.label).size()).append(" assignments\n");
        payload.append("-- Summary --\n");
        labelPositionHolder.getPositions(msg.label).forEach(pos -> {
            payload
                    .append(pos.getX())
                    .append(",")
                    .append(pos.getY())
                    .append(",")
                    .append(pos.getZ());
            if (player.world.isBlockLoaded(pos)) {
                payload
                        .append(" -- ")
                        .append(player.world.getBlockState(pos).getBlock().getLocalizedName());
            } else {
                payload
                        .append(" -- chunk not loaded");
            }
            payload
                    .append("\n");
        });

        payload.append("\n\n\n-- Detailed --\n");
        for (BlockPos pos : labelPositionHolder.getPositions(msg.label)) {
            if (payload.length() > 20_000) {
                payload.append("... (truncated)");
                break;
            }
            payload
                    .append(pos.getX())
                    .append(",")
                    .append(pos.getY())
                    .append(",")
                    .append(pos.getZ());
            if (player.world.isBlockLoaded(pos)) {
                payload
                        .append(" -- ")
                        .append(player.world.getBlockState(pos).getBlock().getLocalizedName());

//                payload.append("\n").append(ServerboundContainerExportsInspectionRequestPacket
//                                                    .buildInspectionResults(player.world(), pos)
//                                                    .indent(1));
            } else {
                payload
                        .append(" -- chunk not loaded");
            }
            payload
                    .append("\n");
        }
        SFM.LOGGER.info(
                "Sending payload response length={} to player {}",
                payload.length(),
                player.getUUID(context.getServerHandler().player.getGameProfile())
        );
//        SFMPackets.sendToPlayer(() -> player, new ClientboundLabelInspectionResultsPacket(
//                SFMPacketDaddy.truncate(
//                        payload.toString(),
//                        ServerboundLabelInspectionRequestPacket.MAX_RESULTS_LENGTH
//                )
//        ));
        return null;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.label=packetBuffer.readString(Program.MAX_LABEL_LENGTH);

    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(this.label);

    }


    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
