package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.inventory.Container;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundContainerExportsInspectionResultsPacket implements SFMMessage<ClientboundContainerExportsInspectionResultsPacket, IMessage>{

    public int windowId;
    public String results;
    public static final int MAX_RESULTS_LENGTH = 20480;

    public ClientboundContainerExportsInspectionResultsPacket() {
    }

    public ClientboundContainerExportsInspectionResultsPacket(int windowId, String result) {
        this.windowId=windowId;
        this.results=result;
    }


    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.windowId = packetBuffer.readVarInt();
        this.results=packetBuffer.readString(MAX_RESULTS_LENGTH);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeVarInt(windowId);
        packetBuffer.writeString(results);
    }

    @Override
    public IMessage onMessage(ClientboundContainerExportsInspectionResultsPacket msg, MessageContext context) {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player == null) return null;
        Container container = player.openContainer;
        if (container.windowId != msg.windowId) return null;
        SFMScreenChangeHelpers.showProgramEditScreen(msg.results);
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}

