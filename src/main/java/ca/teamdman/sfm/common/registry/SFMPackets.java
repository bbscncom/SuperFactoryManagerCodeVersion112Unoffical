package ca.teamdman.sfm.common.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.net.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public class SFMPackets {
    private static int discriminator=1;
    private static SimpleNetworkWrapper NETWORK_CHANNEL;

    static {
        NETWORK_CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel(SFM.MOD_ID);
    }

    public static void register() {
        registerPacket( new ServerboundLabelGunUpdatePacket());
        registerPacket(new ClientboundBoolExprStatementInspectionResultsPacket(null));
//        registerPacket(new ClientboundClientConfigCommandPacket());
        registerPacket(new ClientboundContainerExportsInspectionResultsPacket(0,null));
        registerPacket(new ClientboundIfStatementInspectionResultsPacket(null));
        registerPacket(new ClientboundInputInspectionResultsPacket(null));
//        registerPacket(new ClientboundLabelInspectionResultsPacket());
        registerPacket(new ClientboundManagerGuiUpdatePacket());
        registerPacket(new ClientboundManagerLogLevelUpdatedPacket());
        registerPacket(new ClientboundManagerLogsPacket());
        registerPacket(new ClientboundOutputInspectionResultsPacket());
        registerPacket(new ClientboundServerConfigCommandPacket());
//        registerPacket(new ClientboundShowChangelogPacket());
        registerPacket(new ServerboundBoolExprStatementInspectionRequestPacket());
        registerPacket(new ServerboundServerConfigRequestPacket());
        registerPacket(new ServerboundContainerExportsInspectionRequestPacket(0,null));
        registerPacket(new ServerboundDiskItemSetProgramPacket(null,null));
        registerPacket(new ServerboundFacadePacket(null,null,null,null));
        registerPacket(new ServerboundIfStatementInspectionRequestPacket());
        registerPacket(new ServerboundInputInspectionRequestPacket());
        registerPacket(new ServerboundLabelGunClearPacket());
        registerPacket(new ServerboundLabelGunPrunePacket());
        registerPacket(new ServerboundLabelGunCycleViewModePacket());
        registerPacket(new ServerboundLabelGunUpdatePacket());
        registerPacket(new ServerboundLabelGunUsePacket(null,null,false,false,false));
        registerPacket(new ServerboundLabelInspectionRequestPacket(null));
        registerPacket(new ServerboundManagerClearLogsPacket());
        registerPacket(new ServerboundManagerFixPacket());
        registerPacket(new ServerboundManagerLogDesireUpdatePacket());
        registerPacket(new ServerboundManagerProgramPacket());
        registerPacket(new ServerboundManagerRebuildPacket());
        registerPacket(new ServerboundManagerResetPacket());
        registerPacket(new ServerboundManagerSetLogLevelPacket(0,null,null));
        registerPacket(new ServerboundOutputInspectionRequestPacket(null,0));
        registerPacket(new ServerboundServerConfigUpdatePacket());
    }

    public static void sendToServer(IMessage packet) {
        NETWORK_CHANNEL.sendToServer(packet);
    }

    public static void sendToPlayer(EntityPlayerMP player, IMessage packet) {
        NETWORK_CHANNEL.sendTo(packet, player);
    }

    public static void registerPacket(SFMMessage reg) {
        NETWORK_CHANNEL.registerMessage(
                reg,
                reg.getClass(),
                discriminator++,
                reg.getSide()
        );
    }
}
