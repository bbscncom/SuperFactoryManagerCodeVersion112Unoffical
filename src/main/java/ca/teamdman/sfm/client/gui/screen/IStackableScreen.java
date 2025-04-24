package ca.teamdman.sfm.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public interface IStackableScreen {
    void setParent(GuiScreen parent);

    GuiScreen getParent();

    default void onClose() {
        if (this.getParent() != null) {
            Minecraft.getMinecraft().displayGuiScreen(this.getParent());
        } else {
            Minecraft.getMinecraft().displayGuiScreen(null);
        }
    }
}
