package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import ca.teamdman.sfm.client.gui.screen.TomlEditScreenOpenContext;
import ca.teamdman.sfm.common.command.ConfigCommandBehaviourInput;
import ca.teamdman.sfm.common.registry.SFMPackets;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundServerConfigCommandPacket implements SFMMessage<ClientboundServerConfigCommandPacket, IMessage> {
    private String configToml;
    private ConfigCommandBehaviourInput requestingEditMode;
    public static final int MAX_LENGTH = 20480;

    public ClientboundServerConfigCommandPacket() {
    }

    public ClientboundServerConfigCommandPacket(String configToml, ConfigCommandBehaviourInput requestingEditMode) {
        this.configToml = configToml;
        this.requestingEditMode = requestingEditMode;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.configToml = packetBuffer.readString(MAX_LENGTH);
        this.requestingEditMode = packetBuffer.readEnumValue(ConfigCommandBehaviourInput.class);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeString(this.configToml);
        packetBuffer.writeEnumValue(this.requestingEditMode);
    }

    @Override
    public IMessage onMessage(ClientboundServerConfigCommandPacket message, MessageContext ctx) {
        String configTomlString = message.configToml;
        configTomlString = configTomlString.replaceAll("\r", "");
        switch (message.requestingEditMode) {
            case SHOW : SFMScreenChangeHelpers.showTomlEditScreen(new TomlEditScreenOpenContext(
                    configTomlString,
                    $ -> {
                    }
            ));
            case EDIT : SFMScreenChangeHelpers.showTomlEditScreen(new TomlEditScreenOpenContext(
                    configTomlString,
                    (newContent) -> SFMPackets.sendToServer(new ServerboundServerConfigUpdatePacket(newContent))
            ));
        }
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}
