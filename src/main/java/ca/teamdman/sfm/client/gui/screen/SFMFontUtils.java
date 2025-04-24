package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.common.util.MCVersionDependentBehaviour;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class SFMFontUtils {
    private static FontRenderer font = Minecraft.getMinecraft().fontRenderer;

    /**
     * Draws text to the screen
     *
     * @return the width of the drawn text
     */
    @MCVersionDependentBehaviour
    public static int drawInBatch(
            ITextComponent text,
            FontRenderer font,
            float x,
            float y,
            boolean dropShadow
    ) {
        return font.drawString(
                text.getFormattedText(),
                x,
                y,
                -1,
                dropShadow
        );
    }

    /**
     * Draws text to the screen
     *
     * @return the width of the drawn text
     */
    @SuppressWarnings("UnusedReturnValue")
    @MCVersionDependentBehaviour
    public static int drawInBatch(
            String text,
            FontRenderer font,
            float x,
            float y,
            boolean dropShadow
    ) {
        return font.drawString(
                text,
                x,
                y,
                -1,
                dropShadow
        );
    }


    @MCVersionDependentBehaviour
    public static void draw(
            FontRenderer font,
            ITextComponent text,
            int x,
            int y,
            int colour,
            boolean shadow
    ) {
        font.drawString(text.getFormattedText(), x, y, colour, shadow);
    }

    @MCVersionDependentBehaviour
    public static void draw(
            ITextComponent text,
            int x,
            int y,
            int colour,
            boolean shadow
    ) {
        font.drawString(text.getFormattedText(), x, y, colour, shadow);
    }

    @MCVersionDependentBehaviour
    public static void draw(
            String text,
            int x,
            int y,
            int colour,
            boolean shadow
    ) {
        font.drawString(text, x, y, colour, shadow);
    }

    @MCVersionDependentBehaviour
    public static void draw(
            FontRenderer font,
            String text,
            int x,
            int y,
            int colour,
            boolean shadow
    ) {
        font.drawString(text, x, y, colour, shadow);
    }
}
