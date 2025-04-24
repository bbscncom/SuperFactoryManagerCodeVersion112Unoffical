package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.command.ConfigCommandBehaviourInput;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ServerboundServerConfigRequestPacket implements SFMMessage<ServerboundServerConfigRequestPacket, IMessage> {
    private ConfigCommandBehaviourInput requestingEditMode;

    public ServerboundServerConfigRequestPacket() {
    }

    public ServerboundServerConfigRequestPacket(ConfigCommandBehaviourInput requestingEditMode) {
        this.requestingEditMode = requestingEditMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.requestingEditMode = packetBuffer.readEnumValue(ConfigCommandBehaviourInput.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeEnumValue(this.requestingEditMode);
    }

    @Override
    public IMessage onMessage(ServerboundServerConfigRequestPacket msg, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player == null) {
            SFM.LOGGER.error("Received {} from null player", this.getClass().getName());
            return null;
        }
//        if (!player.hasPermissionLevel(Commands.LEVEL_OWNERS)
        if (!player.canUseCommand(2, "")
            && msg.requestingEditMode == ConfigCommandBehaviourInput.EDIT) {
            SFM.LOGGER.warn(
                    "Player {} tried to request server config for editing but does not have the necessary permissions, this should never happen o-o",
                    player.getName()
            );
            return null;
        }
//        String configToml = SFMConfigReadWriter.getConfigToml(SFMConfig.SERVER_SPEC);
//        if (configToml == null) {
//            SFM.LOGGER.warn("Unable to get server config for player {}", player.getName());
//            player.sendMessage(SFMConfigReadWriter.ConfigSyncResult.INTERNAL_FAILURE.component());
//            return null;
//        }
//        configToml = configToml.replaceAll("(?m)^#", "--");
//        configToml = configToml.replaceAll("\r", "");
//        SFM.LOGGER.info("Sending config to player: {}", player.getName());
//        SFMPackets.sendToPlayer(
//                () -> player,
//                new ClientboundServerConfigCommandPacket(configToml, msg.requestingEditMode)
//        );
        return null;
    }

    @Override
    public Side getSide() {
        return Side.SERVER;
    }
}
