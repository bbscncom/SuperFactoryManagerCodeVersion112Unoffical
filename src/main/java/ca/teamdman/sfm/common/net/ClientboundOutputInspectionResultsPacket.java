package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundOutputInspectionResultsPacket implements SFMMessage<ClientboundOutputInspectionResultsPacket, IMessage> {
    private String results;
    public static final int MAX_RESULTS_LENGTH = 10240;

    // 默认构造函数用于反序列化
    public ClientboundOutputInspectionResultsPacket() {
    }

    public ClientboundOutputInspectionResultsPacket(String results) {
        this.results = results;
    }

    public String getResults() {
        return results;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.results = packetBuffer.readString(MAX_RESULTS_LENGTH);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(this.results);
    }

    @Override
    public IMessage onMessage(ClientboundOutputInspectionResultsPacket message, MessageContext ctx) {
        // 在客户端处理数据包
        SFMScreenChangeHelpers.showProgramEditScreen(message.getResults());
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}