package my;

import my.net.neoforged.api.distmarker.Dist;
import my.net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class AbstractContainerEventHandler implements ContainerEventHandler {


    @Nullable
    private GuiEventListener focused;
    private boolean isDragging;

    @Override
    public final boolean isDragging() {
        return this.isDragging;
    }


    /**
     * Sets if the GUI element is dragging or not.
     *
     * @param pDragging the dragging state of the GUI element.
     */
    @Override
    public final void setDragging(boolean pDragging) {
        this.isDragging = pDragging;
    }

    @Nullable
    @Override
    public GuiEventListener getFocused() {
        return this.focused;
    }

    /**
     * Sets the focus state of the GUI element.
     *
     * @param pListener the focused GUI element.
     */
    @Override
    public void setFocused(@Nullable GuiEventListener pListener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (pListener != null) {
            pListener.setFocused(true);
        }

        this.focused = pListener;
    }
}
