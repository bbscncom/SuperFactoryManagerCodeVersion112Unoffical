package ca.teamdman.sfm.common.net;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import ca.teamdman.sfm.client.gui.screen.TomlEditScreenOpenContext;
import ca.teamdman.sfm.common.command.ConfigCommandBehaviourInput;
import ca.teamdman.sfm.common.config.SFMConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class ClientboundClientConfigCommandPacket implements SFMMessage<ClientboundClientConfigCommandPacket, IMessage> {
    ConfigCommandBehaviourInput requestingEditMode;

    public ClientboundClientConfigCommandPacket() {
    }

    public ClientboundClientConfigCommandPacket(ConfigCommandBehaviourInput behaviour) {
        this.requestingEditMode=behaviour;
    }


    public static void handleNewClientConfig(String newConfigToml) {
//        SFMConfigReadWriter.ConfigSyncResult configSyncResult = SFMConfigReadWriter.updateClientConfig(newConfigToml);
//        LocalPlayer player = Minecraft.getInstance().player;
//        if (player != null) {
//            player.sendSystemMessage(configSyncResult.component());
//        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        this.requestingEditMode=packetBuffer.readEnumValue(ConfigCommandBehaviourInput.class);

    }

    @Override
    public void toBytes(ByteBuf buf) {
        PacketBuffer packetBuffer = new PacketBuffer(buf);
        packetBuffer.writeEnumValue(requestingEditMode);
    }

    @Override
    public IMessage onMessage(ClientboundClientConfigCommandPacket message, MessageContext ctx) {
        //todo temp
//        String configTomlString = SFMConfigReadWriter.getConfigToml(SFMConfig.CLIENT_SPEC);
//        if (configTomlString == null) {
//            SFM.LOGGER.error("Unable to get client config");
//            return;
//        }
//        configTomlString = configTomlString.replaceAll("\r", "");
//        switch (msg.requestingEditMode()) {
//            case SHOW -> SFMScreenChangeHelpers.showTomlEditScreen(new TomlEditScreenOpenContext(
//                    configTomlString,
//                    $ -> {
//                    }
//            ));
//            case EDIT -> SFMScreenChangeHelpers.showTomlEditScreen(new TomlEditScreenOpenContext(
//                    configTomlString,
//                    Daddy::handleNewClientConfig
//            ));
//        }
        return null;
    }

    @Override
    public Side getSide() {
        return Side.CLIENT;
    }
}

