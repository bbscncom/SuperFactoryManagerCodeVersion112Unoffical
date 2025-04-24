package ca.teamdman.sfm.client.gui.overlay;

import ca.teamdman.sfm.client.registry.SFMKeyMappings;
import ca.teamdman.sfm.common.config.SFMClientConfig;
import ca.teamdman.sfm.common.item.LabelGunItem;
import ca.teamdman.sfm.common.localization.LocalizationEntry;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.registry.SFMItems;
import ca.teamdman.sfm.common.util.SFMHandUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class LabelGunReminderOverlay {

    public void renderGameOverlay(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.gameSettings.hideGUI) {
            return;
        }
        EntityPlayer player = minecraft.player;
        if (player == null) {
            return;
        }

        LabelGunItem.LabelGunViewMode viewMode = getViewMode(minecraft);
        if (viewMode == null) return;
        LocalizationEntry msg=null;
        switch (viewMode) {
            case SHOW_ALL: break;
            case SHOW_ONLY_ACTIVE_LABEL_AND_TARGETED_BLOCK :
                msg= LocalizationKeys.LABEL_GUN_VIEW_MODE_SHOW_ONLY_ACTIVE_AND_TARGETED;break;
            case SHOW_ONLY_TARGETED_BLOCK :
                msg=LocalizationKeys.LABEL_GUN_VIEW_MODE_SHOW_ONLY_TARGETED;
        }
        ;
        if (msg == null) return;
        FontRenderer font = minecraft.fontRenderer;
        String reminder = msg + " (" + SFMKeyMappings.CYCLE_LABEL_VIEW_KEY.getDisplayName() + ")";
        int reminderWidth = font.getStringWidth(reminder);
        int x = minecraft.displayWidth / 2 - reminderWidth / 2;
        int y = 20;
        //todo 未确定替换
//        SFMFontUtils.draw(
//                guiGraphics,
//                font,
//                reminder,
//                x,
//                y,
//                FastColor.ARGB32.color(255, 172, 208, 255),
//                true
//        );
        font.drawStringWithShadow(reminder, x, y, 0xFFACD0FF);
    }


    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static @Nullable LabelGunItem.LabelGunViewMode getViewMode(Minecraft minecraft) {
        EntityPlayerSP player = minecraft.player;
        if (player == null) return null;
        if (!SFMClientConfig.showLabelGunReminderOverlay) return null;
        ItemStack labelGun = SFMHandUtils.getItemInEitherHand(player, SFMItems.LABEL_GUN_ITEM);
        if (labelGun.isEmpty()) return null;
        return LabelGunItem.getViewMode(labelGun);
    }
}
