package ca.teamdman.sfm.client;

import ca.teamdman.sfm.client.gui.screen.GuiYesNoExtend;
import ca.teamdman.sfm.client.gui.screen.SFMScreenChangeHelpers;
import ca.teamdman.sfm.common.facade.FacadePlanWarning;
import ca.teamdman.sfm.common.facade.FacadePlanner;
import ca.teamdman.sfm.common.facade.IFacadePlan;
import ca.teamdman.sfm.common.net.ServerboundFacadePacket;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.SFMPlayerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.world.World;

public class ClientFacadeWarningHelper {
    public static void sendFacadePacketFromClientWithConfirmationIfNecessary(ServerboundFacadePacket msg) {
        // Given the incentives for a single cable network to be used,
        // we want to protect users from accidentally clobbering their designs in a single action
        Minecraft minecraft = Minecraft.getMinecraft();
        EntityPlayerSP player = minecraft.player;
        assert player != null;
        World level = SFMPlayerUtils.getWorld(player);

        IFacadePlan facadePlan = FacadePlanner.getFacadePlan(
                player,
                level,
                msg
        );
        if (facadePlan == null) return;
        FacadePlanWarning warning = facadePlan.computeWarning(level);
        if (warning == null) {
            // No confirmation necessary for single updates
            SFMPackets.sendToServer(msg);
            // Perform eager update
            facadePlan.apply(level);
        } else {
            GuiYesNo confirmScreen = new GuiYesNoExtend(
                    (result, id) -> {
                        SFMScreenChangeHelpers.popScreen(); // Close confirm screen
                        if (result) {
                            // Send packet
                            SFMPackets.sendToServer(msg);
                            // Perform eager update
                            facadePlan.apply(level);
                        }
                    },
                    warning.confirmTitle.getFormattedText(),
                    warning.confirmMessage.getFormattedText(),
                    warning.confirmYes.getFormattedText(),
                    warning.confirmNo.getFormattedText(),
//                    todo 不知道填什么
                    0
            );
            SFMScreenChangeHelpers.setOrPushScreen(confirmScreen);
            confirmScreen.setButtonDelay(10);
        }
    }
}
