package my;

import my.net.neoforged.api.distmarker.Dist;
import my.net.neoforged.api.distmarker.OnlyIn;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class Button extends AbstractButton {
    public static final int SMALL_WIDTH = 120;
    public static final int DEFAULT_WIDTH = 150;
    public static final int BIG_WIDTH = 200;
    public static final int DEFAULT_HEIGHT = 20;
    public static final int DEFAULT_SPACING = 8;
    protected static final Button.CreateNarration DEFAULT_NARRATION = p_253298_ -> p_253298_.get();
    protected final Button.OnPress onPress;
    protected final Button.CreateNarration createNarration;

    public static Button.Builder builder(ITextComponent pMessage, Button.OnPress pOnPress) {
        return new Button.Builder(pMessage, pOnPress);
    }

    protected Button(
            int pX, int pY, int pWidth, int pHeight, ITextComponent pMessage, Button.OnPress pOnPress, Button.CreateNarration pCreateNarration
    ) {
        super(pX, pY, pWidth, pHeight, pMessage);
        this.onPress = pOnPress;
        this.createNarration = pCreateNarration;
    }

    protected Button(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, builder.createNarration);
    }

    @Override
    public void onPress() {
        this.onPress.onPress(this);
    }


    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final ITextComponent message;
        private final Button.OnPress onPress;
        @Nullable
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private Button.CreateNarration createNarration = Button.DEFAULT_NARRATION;

        public Builder(ITextComponent pMessage, Button.OnPress pOnPress) {
            this.message = pMessage;
            this.onPress = pOnPress;
        }

        public Button.Builder pos(int pX, int pY) {
            this.x = pX;
            this.y = pY;
            return this;
        }

        public Button.Builder width(int pWidth) {
            this.width = pWidth;
            return this;
        }

        public Button.Builder size(int pWidth, int pHeight) {
            this.width = pWidth;
            this.height = pHeight;
            return this;
        }

        public Button.Builder bounds(int pX, int pY, int pWidth, int pHeight) {
            return this.pos(pX, pY).size(pWidth, pHeight);
        }


        public Button.Builder createNarration(Button.CreateNarration pCreateNarration) {
            this.createNarration = pCreateNarration;
            return this;
        }

        public Button build() {
            return build(Button::new);
        }

        public Button build(java.util.function.Function<Builder, Button> builder) {
            return builder.apply(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public interface CreateNarration {
        ITextComponent createNarrationMessage(Supplier<ITextComponent> pMessageSupplier);
    }

    @OnlyIn(Dist.CLIENT)
    public interface OnPress {
        void onPress(Button pButton);
    }
}
