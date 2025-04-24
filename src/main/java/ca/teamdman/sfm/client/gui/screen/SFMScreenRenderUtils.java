package ca.teamdman.sfm.client.gui.screen;


import my.AbstractWidget;
import net.minecraft.client.gui.Gui;

public class SFMScreenRenderUtils {
    public static int getX(AbstractWidget widget) {
        return widget.getX();
    }
    public static int getY(AbstractWidget widget) {
        return widget.getY();
    }


    public static void enableKeyRepeating() {
        // 1.19.2
//        Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(true);
    }

    /**
     * Applies a colour inversion for a region to impart a highlight effect.
     * <p/>
     * See also: {@link net.minecraft.client.gui.components.MultiLineEditBox#renderHighlight(PoseStack, int, int, int, int)}
     */
    @SuppressWarnings("JavadocReference")
    public static void renderHighlight(
            int startX,
            int startY,
            int endX,
            int endY
    ) {
        Gui.drawRect(startX, startY, endX, endY, -16776961);
    }

}
