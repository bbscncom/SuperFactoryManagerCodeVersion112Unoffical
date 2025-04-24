package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.logging.TranslatableLogEvent;
import ca.teamdman.sfm.common.logging.TranslatableLogger;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayDeque;
import java.util.Collection;

public class ClientboundManagerLogsPacket implements SFMMessage<ClientboundManagerLogsPacket, IMessage> {
    private int windowId;
    private PacketBuffer logsBuf;

    // 默认构造函数用于反序列化
    public ClientboundManagerLogsPacket() {
    }

    public ClientboundManagerLogsPacket(int windowId, PacketBuffer logsBuf) {
        this.windowId = windowId;
        this.logsBuf = logsBuf;
    }

    public int getWindowId() {
        return windowId;
    }

    public PacketBuffer getLogsBuf() {
        return logsBuf;
    }

    public static ClientboundManagerLogsPacket drainToCreate(
            int windowId,
            Collection<TranslatableLogEvent> logs
    ) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        TranslatableLogger.encodeAndDrain(logs, buf);
        return new ClientboundManagerLogsPacket(windowId, buf);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readVarInt();
        int size = packetBuffer.readVarInt();
        this.logsBuf = new PacketBuffer(Unpooled.buffer(size));
        packetBuffer.readBytes(this.logsBuf, size);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(this.windowId);
        packetBuffer.writeVarInt(this.logsBuf.readableBytes());
        packetBuffer.writeBytes(this.logsBuf, 0, this.logsBuf.readableBytes());
    }

    @Override
    public IMessage onMessage(ClientboundManagerLogsPacket message, MessageContext ctx) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return null;
        if (!(player.openContainer instanceof ManagerContainerMenu))  return null;
        ManagerContainerMenu openContainer = (ManagerContainerMenu) player.openContainer;
        if (openContainer.windowId != message.getWindowId()) return null;
        ArrayDeque<TranslatableLogEvent> logs = TranslatableLogger.decode(message.getLogsBuf());
        openContainer.logs.addAll(logs);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}
