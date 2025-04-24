package ca.teamdman.sfm.client.gui.screen;


import ca.teamdman.sfm.common.net.ServerboundLabelGunUpdatePacket;
import ca.teamdman.sfm.common.registry.SFMPackets;
import com.google.common.collect.Lists;
import my.GuiEventListener;
import my.Renderable;
import my.Tools;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.DeclareParents;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ScreenToGUIScreenAspect {

    @DeclareParents(
            value = "com.example.TargetClass",
            defaultImpl = FieldHolderImpl.class
    )
    private FieldHolder fieldHolder;

    public interface FieldHolder {
         GuiEventListener getFocused();
         void setFocused(GuiEventListener focused) ;
         List<GuiEventListener> getChildren() ;
         List<Renderable> getRenderables();
         void setRenderables(List<Renderable> renderables);
         boolean isDragging() ;
         void setDragging(boolean dragging);
         int getLastMouseX();
         void setLastMouseX(int lastMouseX) ;
         int getLastMouseY();
         void setLastMouseY(int lastMouseY);
    }
    public class FieldHolderImpl implements FieldHolder{
        private GuiEventListener focused;
        private final List<GuiEventListener> children = Lists.newArrayList();
        private List<Renderable> renderables=new ArrayList<>();
        private boolean dragging;
        private int lastMouseX;
        private int lastMouseY;

        public GuiEventListener getFocused() {
            return focused;
        }

        public void setFocused(GuiEventListener focused) {
            this.focused = focused;
        }

        public List<GuiEventListener> getChildren() {
            return children;
        }

        public List<Renderable> getRenderables() {
            return renderables;
        }

        public void setRenderables(List<Renderable> renderables) {
            this.renderables = renderables;
        }

        public boolean isDragging() {
            return dragging;
        }

        public void setDragging(boolean dragging) {
            this.dragging = dragging;
        }

        public int getLastMouseX() {
            return lastMouseX;
        }

        public void setLastMouseX(int lastMouseX) {
            this.lastMouseX = lastMouseX;
        }

        public int getLastMouseY() {
            return lastMouseY;
        }

        public void setLastMouseY(int lastMouseY) {
            this.lastMouseY = lastMouseY;
        }
    }

}
