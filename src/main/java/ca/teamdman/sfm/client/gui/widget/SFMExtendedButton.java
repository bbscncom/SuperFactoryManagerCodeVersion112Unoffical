package ca.teamdman.sfm.client.gui.widget;

import my.ExtendedButton;
import net.minecraft.util.text.ITextComponent;

public class SFMExtendedButton extends ExtendedButton {
    public SFMExtendedButton(
            int xPos,
            int yPos,
            int width,
            int height,
            ITextComponent displayString,
            OnPress handler
    ) {
        super(xPos, yPos, width, height, displayString, handler);
    }
}
