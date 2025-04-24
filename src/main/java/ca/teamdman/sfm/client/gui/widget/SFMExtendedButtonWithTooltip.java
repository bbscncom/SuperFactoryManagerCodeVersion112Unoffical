package ca.teamdman.sfm.client.gui.widget;

import net.minecraft.util.text.ITextComponent;

public class SFMExtendedButtonWithTooltip extends SFMExtendedButton{
    public SFMExtendedButtonWithTooltip(int xPos, int yPos, int width, int height, ITextComponent displayString, OnPress handler,Tooltip tooltip) {
        super(xPos, yPos, width, height, displayString, handler);
        setTooltip(tooltip);
    }
}
