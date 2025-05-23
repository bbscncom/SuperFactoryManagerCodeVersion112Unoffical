package my;

import my.net.neoforged.api.distmarker.Dist;
import my.net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public interface LayoutElement {
    void setX(int pX);

    void setY(int pY);

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    default ScreenRectangle getRectangle() {
        return new ScreenRectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    default void setPosition(int pX, int pY) {
        this.setX(pX);
        this.setY(pY);
    }

    void visitWidgets(Consumer<AbstractWidget> pConsumer);
}