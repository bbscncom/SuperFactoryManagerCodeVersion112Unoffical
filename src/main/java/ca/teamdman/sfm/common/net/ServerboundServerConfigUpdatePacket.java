package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class ServerboundServerConfigUpdatePacket implements SFMMessage<ServerboundServerConfigUpdatePacket, IMessage> {
    private String newConfig;
    public static final int MAX_CONFIG_LENGTH = 32767;

    public ServerboundServerConfigUpdatePacket() {
    }

    public ServerboundServerConfigUpdatePacket(String newConfig) {
        this.newConfig = newConfig;
    }

    public String getNewConfig() {
        return newConfig;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.newConfig = packetBuffer.readString(MAX_CONFIG_LENGTH);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(this.newConfig);
    }

    @Override
    public IMessage onMessage(ServerboundServerConfigUpdatePacket message, MessageContext ctx) {
        EntityPlayerMP player = ctx.getServerHandler().player;
        if (player == null) {
            SFM.LOGGER.error("Received {} from null player", this.getClass().getName());
            return null;
        }
        if (!player.canUseCommand(2,"")) {
            SFM.LOGGER.fatal(
                    "Player {} tried to WRITE server config but does not have the necessary permissions, this should never happen o-o",
                    player.getName()
            );
            return null;
        }
        //todo 暂时删除
//        SFMConfigReadWriter.ConfigSyncResult result = SFMConfigReadWriter.updateAndSyncServerConfig(message.newConfig);
//        player.sendSystemMessage(result.component());
        return null;
    }

    @Override
    public net.minecraftforge.fml.relauncher.Side getSide() {
        return net.minecraftforge.fml.relauncher.Side.SERVER;
    }
}

