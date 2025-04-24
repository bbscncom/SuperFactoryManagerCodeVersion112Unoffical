package ca.teamdman.sfm.common.command;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.net.ClientboundClientConfigCommandPacket;
import ca.teamdman.sfm.common.registry.SFMPackets;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class ConfigCommand extends CommandBase {
    public static final int FAILURE = 0;

    private ConfigCommandBehaviourInput behaviour;
    private ConfigCommandVariantInput variant;

    public ConfigCommand(ConfigCommandBehaviourInput behaviour, ConfigCommandVariantInput variant) {
        this.behaviour = behaviour;
        this.variant = variant;
    }

    @Override
    public String getName() {
        return "configCommand";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/configCommand <client|server>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        if (!(sender instanceof EntityPlayerMP)) {
            SFM.LOGGER.error("Command can only be executed by a player.");
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) sender;
        if (args.length < 1) {
            SFM.LOGGER.error("Invalid arguments.");
            return;
        }

        switch (args[0].toLowerCase()) {
            case "client":
                handleClientConfigCommand(player);
                break;
            case "server":
                handleServerConfigCommand(player);
                break;
            default:
                SFM.LOGGER.error("Unknown command variant.");
        }
    }

    private int handleClientConfigCommand(EntityPlayerMP player) {
        SFMPackets.sendToPlayer(
                player,
                new ClientboundClientConfigCommandPacket(behaviour)
        );
        return FAILURE;
    }

    private int handleServerConfigCommand(EntityPlayerMP player) {
//        String configToml = SFMConfigReadWriter.getConfigToml(
//                SFMConfig.SERVER_SPEC);
//        if (configToml == null) {
        SFM.LOGGER.warn(
                "Unable to get server config for player {} to {}",
                player.getName(),
                behaviour
        );
//            player.sendSystemMessage(
//                    SFMConfigReadWriter.ConfigSyncResult.FAILED_TO_FIND
//                            .component()
//                            .withStyle(ChatFormatting.RED)
//            );
//        } else {
//            SFMPackets.sendToPlayer(
//                    player,
//                    new ClientboundServerConfigCommandPacket(
//                            configToml,
//                            behaviour
//                    )
//            );
//        }
//        return SINGLE_SUCCESS;
        return 0;
    }

    public ConfigCommandBehaviourInput getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(ConfigCommandBehaviourInput behaviour) {
        this.behaviour = behaviour;
    }

    public ConfigCommandVariantInput getVariant() {
        return variant;
    }

    public void setVariant(ConfigCommandVariantInput variant) {
        this.variant = variant;
    }
}