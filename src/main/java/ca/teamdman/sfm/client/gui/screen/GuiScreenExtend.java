package ca.teamdman.sfm.client.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

public class GuiScreenExtend extends GuiScreen implements IStackableScreen{

    private GuiScreen parentScreen;

    @Override
    public void setParent(GuiScreen parent) {
        this.parentScreen=parent;
    }

    @Override
    public GuiScreen getParent() {
        return this.parentScreen;
    }

    public void onClose() {
        if (this.getParent() != null) {
            Minecraft.getMinecraft().displayGuiScreen(this.getParent());
        }else{
            this.mc.displayGuiScreen(null);
        }
    }
}
