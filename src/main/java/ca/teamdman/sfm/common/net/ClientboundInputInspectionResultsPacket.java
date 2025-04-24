package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundInputInspectionResultsPacket implements  SFMMessage<ClientboundInputInspectionResultsPacket,IMessage> {
    private String results;
    public static final int MAX_RESULTS_LENGTH = 20480;


    public ClientboundInputInspectionResultsPacket(String results) {
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
    public IMessage onMessage(ClientboundInputInspectionResultsPacket message, MessageContext ctx) {
        // Handle the packet on the client side
        SFMScreenChangeHelpers.showProgramEditScreen(message.getResults());
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}
