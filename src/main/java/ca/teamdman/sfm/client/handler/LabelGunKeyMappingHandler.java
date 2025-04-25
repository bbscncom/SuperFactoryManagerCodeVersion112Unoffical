package ca.teamdman.sfm.client.handler;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.ClientKeyHelpers;
import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.common.item.LabelGunItem;
import ca.teamdman.sfm.common.net.ServerboundLabelGunCycleViewModePacket;
import ca.teamdman.sfm.common.net.ServerboundLabelGunUpdatePacket;
import ca.teamdman.sfm.common.registry.SFMItems;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.SFMHandUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

@SideOnly(Side.CLIENT)

@Mod.EventBusSubscriber(modid = SFM.MOD_ID, value = Side.CLIENT)
public class LabelGunKeyMappingHandler {
    private static AltState altState = AltState.Idle;
    private static boolean labelSwitchKeyDown = false;

    public static void setExternalDebounce() {
        altState = AltState.PressCancelledExternally;
    }

    @SuppressWarnings("DuplicatedCode")
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) return;
        EntityPlayerSP player = minecraft.player;
        if (player == null) return;
        handleAltKeyLogic();
        handleLabelSwitchKeyLogic(player);
    }

    private static void handleLabelSwitchKeyLogic(EntityPlayerSP player) {
        boolean nextLabelKeyDown = ClientKeyHelpers.isKeyDownInWorld(SFMKeyMappings.LABEL_GUN_NEXT_LABEL_KEY);
        boolean prevLabelKeyDown = ClientKeyHelpers.isKeyDownInWorld(SFMKeyMappings.LABEL_GUN_PREVIOUS_LABEL_KEY);
        boolean justPressed = !labelSwitchKeyDown && (nextLabelKeyDown || prevLabelKeyDown);
        labelSwitchKeyDown = nextLabelKeyDown || prevLabelKeyDown;
        if (justPressed) {
            SFMHandUtils.@Nullable ItemStackInHand labelGun = SFMHandUtils.getItemAndHand(player, SFMItems.LABEL_GUN_ITEM);
            if (labelGun == null) return;
            String nextLabel = LabelGunItem.getNextLabel(labelGun.stack, prevLabelKeyDown ? -1 : 1);
            SFMPackets.sendToServer(new ServerboundLabelGunUpdatePacket(nextLabel, labelGun.hand));
        }
    }

    private static void handleAltKeyLogic() {
        Minecraft minecraft = Minecraft.getMinecraft();

        // don't do anything if a screen is open
        if (minecraft.currentScreen != null) return;

        // only do something if the key was pressed
        boolean alt_down = ClientKeyHelpers.isKeyDownInWorld(SFMKeyMappings.CYCLE_LABEL_VIEW_KEY);
        switch (altState) {
            case Idle : {
                if (alt_down) {
                    altState = AltState.Pressed;
                }
                break;
            }
            case Pressed : {
                if (!alt_down) {
                    altState = AltState.Idle;
                    assert minecraft.player != null;
                    EnumHand hand = SFMHandUtils.getHandHoldingItem(
                            minecraft.player,
                            SFMItems.LABEL_GUN_ITEM
                    );
                    if (hand == null) return;
                    // send packet to server to toggle mode
                    SFMPackets.sendToServer(new ServerboundLabelGunCycleViewModePacket(hand));
                }
                break;
            }
            case PressCancelledExternally :{
                if (!alt_down) {
                    altState = AltState.Idle;
                }
            }
        }
    }

    private enum AltState {
        Idle,
        Pressed,
        PressCancelledExternally,
    }
}
