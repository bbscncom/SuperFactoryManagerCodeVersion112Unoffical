package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundBoolExprStatementInspectionResultsPacket implements SFMMessage<ClientboundBoolExprStatementInspectionResultsPacket, IMessage> {
    public static final int MAX_RESULTS_LENGTH = 2048;
    public String results;

    public ClientboundBoolExprStatementInspectionResultsPacket(String results) {
        this.results = results;
    }

    public String results() {
        return results;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.results=packetBuffer.readString(MAX_RESULTS_LENGTH);

    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(this.results);
    }

    @Override
    public IMessage onMessage(ClientboundBoolExprStatementInspectionResultsPacket message, MessageContext ctx) {
        SFMScreenChangeHelpers.showProgramEditScreen(message.results());
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}
