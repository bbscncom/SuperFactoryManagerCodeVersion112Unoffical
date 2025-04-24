package ca.teamdman.sfm.client.registry;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.gui.overlay.LabelGunReminderOverlay;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = SFM.MOD_ID)
public class SFMOverlays {
    public static final LabelGunReminderOverlay LABEL_GUN_REMINDER_OVERLAY = new LabelGunReminderOverlay();

    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            LABEL_GUN_REMINDER_OVERLAY.renderGameOverlay(event.getPartialTicks());
        }
    }
}
