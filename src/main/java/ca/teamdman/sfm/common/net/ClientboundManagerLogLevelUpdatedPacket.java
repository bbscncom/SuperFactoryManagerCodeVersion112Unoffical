package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundManagerLogLevelUpdatedPacket implements SFMMessage<ClientboundManagerLogLevelUpdatedPacket, IMessage> {
    private int windowId;
    private String logLevel;

    // 默认构造函数用于反序列化
    public ClientboundManagerLogLevelUpdatedPacket() {
    }

    public ClientboundManagerLogLevelUpdatedPacket(int windowId, String logLevel) {
        this.windowId = windowId;
        this.logLevel = logLevel;
    }

    public int getWindowId() {
        return windowId;
    }

    public String getLogLevel() {
        return logLevel;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readVarInt();
        this.logLevel = packetBuffer.readString(ServerboundManagerSetLogLevelPacket.MAX_LOG_LEVEL_NAME_LENGTH);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(this.windowId);
        packetBuffer.writeString(this.logLevel);
    }

    @Override
    public IMessage onMessage(ClientboundManagerLogLevelUpdatedPacket message, MessageContext ctx) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) {
            SFM.LOGGER.error("Invalid log level packet received, ignoring.");
            return null;
        }
        if (!(player.openContainer instanceof ManagerContainerMenu)) {
            SFM.LOGGER.error("Invalid log level packet received, ignoring.");
            return null;
        }
        ;
        ManagerContainerMenu openContainer = (ManagerContainerMenu) player.openContainer;
        if (openContainer.windowId != message.getWindowId()) {
            SFM.LOGGER.error("Invalid log level packet received, ignoring.");
            return null;
        }
        openContainer.logLevel = message.getLogLevel();
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}
