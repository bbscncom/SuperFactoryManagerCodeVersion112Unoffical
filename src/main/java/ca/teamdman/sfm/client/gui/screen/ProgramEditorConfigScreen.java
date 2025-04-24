package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.client.gui.widget.SFMButtonBuilder;
import ca.teamdman.sfm.common.config.SFMClientProgramEditorConfig;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import com.google.common.collect.Lists;
import my.*;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("NotNullFieldNotInitialized")
public class ProgramEditorConfigScreen extends GuiScreenExtend {
    private final SFMClientProgramEditorConfig config;
    private final ProgramEditorScreen parent;
    private final Runnable closeCallback;
    private Button lineNumbersOnButton;
    private Button lineNumbersOffButton;
    private Button intellisenseOffButton;
    private Button intellisenseBasicButton;
    private Button intellisenseAdvancedButton;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private List<Renderable> renderables=new ArrayList<>();
    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;
    private GuiEventListener focused;

    public ProgramEditorConfigScreen(
            ProgramEditorScreen parent,
            SFMClientProgramEditorConfig config,
            Runnable closeCallback
    ) {
//        super(LocalizationKeys.PROGRAM_EDITOR_CONFIG_SCREEN_TITLE.getComponent());
        this.config = config;
        this.parent = parent;
        this.closeCallback = closeCallback;
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        closeCallback.run();
    }

    @Override
    public void drawScreen(
            int pMouseX,
            int pMouseY,
            float pPartialTick
    ) {
        FontRenderer font = this.fontRenderer;
        this.drawDefaultBackground();
        super.drawScreen(pMouseX, pMouseY, pPartialTick);

        int y = this.height / 2 - 65;
        int x = this.width / 2 - 150; // Shifted to the left for centering
        font.drawString(
                LocalizationKeys.PROGRAM_EDITOR_CONFIG_LINE_NUMBERS.getComponent().getFormattedText(),
                x,
                y,
                0xFFFFFF
        );
        font.drawString(
                LocalizationKeys.PROGRAM_EDITOR_CONFIG_INTELLISENSE.getComponent().getFormattedText(),
                x,
                y + 50,
                0xFFFFFF
        );
        this.drawCenteredString(
                font,
                "title",
                this.width / 2,
                15,
                0xFFFFFF
        ); // Ensure title is still displayed
    }

    @Override
    public void initGui() {
        super.initGui();

        int buttonWidth = 100;
        int buttonHeight = 20;
        int x = this.width / 2 - (3 * buttonWidth) / 2
                - 10; // Centering the buttons
        int y = this.height / 2 - 50;
        int spacing = 50;
        int buttonSpacing = 10; // Space between buttons

        // Line Numbers Buttons
        lineNumbersOnButton =
                new SFMButtonBuilder()
                        .setPosition(x + buttonWidth + buttonSpacing, y)
                        .setSize(buttonWidth, buttonHeight)
                        .setText(CommonComponents.OPTION_ON)
                        .setOnPress(button -> setLineNumbers(true))
                        .build();
        lineNumbersOffButton =
                new SFMButtonBuilder()
                        .setPosition(x, y)
                        .setSize(buttonWidth, buttonHeight)
                        .setText(CommonComponents.OPTION_OFF)
                        .setOnPress(button -> setLineNumbers(false))
                        .build();

        this.addRenderableWidget(lineNumbersOnButton);
        this.addRenderableWidget(lineNumbersOffButton);

        // Intellisense Buttons
        intellisenseOffButton =
                new SFMButtonBuilder()
                        .setPosition(x, y + spacing)
                        .setSize(buttonWidth, buttonHeight)
                        .setText(LocalizationKeys.PROGRAM_EDITOR_CONFIG_INTELLISENSE_OFF)
                        .setOnPress(button -> setIntellisenseLevel(SFMClientProgramEditorConfig.IntellisenseLevel.OFF))
                        .build();
        intellisenseBasicButton =
                new SFMButtonBuilder()
                        .setPosition(x + buttonWidth + buttonSpacing, y + spacing)
                        .setSize(buttonWidth, buttonHeight)
                        .setText(LocalizationKeys.PROGRAM_EDITOR_CONFIG_INTELLISENSE_BASIC)
                        .setOnPress(button -> setIntellisenseLevel(SFMClientProgramEditorConfig.IntellisenseLevel.BASIC))
                        .build();
        intellisenseAdvancedButton =
                new SFMButtonBuilder()
                        .setPosition(
                                x + 2 * (buttonWidth + buttonSpacing), y + spacing
                        )
                        .setSize(buttonWidth, buttonHeight)
                        .setText(LocalizationKeys.PROGRAM_EDITOR_CONFIG_INTELLISENSE_ADVANCED)
                        .setOnPress(button -> setIntellisenseLevel(SFMClientProgramEditorConfig.IntellisenseLevel.ADVANCED))
                        .build();

        this.addRenderableWidget(intellisenseOffButton);
        this.addRenderableWidget(intellisenseBasicButton);
        this.addRenderableWidget(intellisenseAdvancedButton);

        // Done Button
        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(this.width / 2 - 100, this.height - 50)
                        .setSize(200, 20)
                        .setText(CommonComponents.GUI_DONE)
                        .setOnPress((button) -> this.onGuiClosed())
                        .build());

        updateButtonStates();
    }
    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T pWidget) {
        this.renderables.add(pWidget);
        return this.addWidget(pWidget);
    }

    protected <T extends GuiEventListener > T addWidget(T pListener) {
        this.children.add(pListener);
        return pListener;
    }

    protected void removeWidget(GuiEventListener pListener) {
        if (pListener instanceof Renderable) {
            this.renderables.remove((Renderable)pListener);
        }
        this.children.remove(pListener);
    }
    Optional<GuiEventListener> getChildAt(int pMouseX, int pMouseY) {
        for (GuiEventListener guieventlistener : this.children) {
            if (guieventlistener.isMouseOver(pMouseX, pMouseY)) {
                return Optional.of(guieventlistener);
            }
        }

        return Optional.empty();
    }

    void setDragging(boolean pIsDragging) {
        this.dragging=pIsDragging;
    }

    boolean isDragging() {
        return dragging;
    }
    @javax.annotation.Nullable
    public GuiEventListener getFocused() {
        return this.focused;
    }

    /**
     * Sets the focus state of the GUI element.
     *
     * @param pListener the focused GUI element.
     */
    public void setFocused(@javax.annotation.Nullable GuiEventListener pListener) {
        if (this.focused != null) {
            this.focused.setFocused(false);
        }

        if (pListener != null) {
            pListener.setFocused(true);
        }

        this.focused = pListener;
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if(mouseButton==0){
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        for (GuiEventListener guieventlistener : this.children) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, mouseButton)) {
                this.setFocused(guieventlistener);
                if (mouseButton == 0) {
                    this.setDragging(true);
                }
                return ;
            }
        }
        return;
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        if (state == 0 && this.isDragging()) {
            this.setDragging(false);
            if (this.getFocused() != null) {
                this.getFocused().mouseReleased(mouseX, mouseY, state);
            }
        }

        this.getChildAt(mouseX, mouseY).filter(p_94708_ -> p_94708_.mouseReleased(mouseX, mouseY, state));

    }
    boolean mouseScrolled(int pMouseX, int pMouseY, int pScrollX, int pScrollY) {
        return this.getChildAt(pMouseX, pMouseY).filter(p_293596_ -> p_293596_.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY)).isPresent();
    }
    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (this.getFocused() != null && this.isDragging() && clickedMouseButton == 0) {
            int dragX = mouseX - lastMouseX;
            int dragY = mouseY - lastMouseY;
            this.getFocused().mouseDragged(mouseX, mouseY, clickedMouseButton, dragX, dragY);
        }
        lastMouseX = mouseX;
        lastMouseY = mouseY;
    }

    public boolean charTyped(
            char pCodePoint,
            int pModifiers
    ) {
        if (GuiScreen.isCtrlKeyDown() && pCodePoint == ' ') {
            return true;
        }
        return this.getFocused() != null && this.getFocused().charTyped(pCodePoint, pModifiers);
    }
    @Override
    public void handleMouseInput() throws IOException {
        int i = Mouse.getEventX() * this.width / this.mc.displayWidth;
        int j = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            this.mouseScrolled(i, j, 0, scroll > 0 ? 1 : -1);
        }
        super.handleMouseInput();
    }



    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        // 模拟高版本 keyPressed
        boolean b = this.keyPressed(keyCode, -1, 0);
        if(b)return;

        // 模拟高版本 charTyped，仅在字符有效时调用
        if (typedChar != 0 && Tools.isAllowedChatCharacter(typedChar)) {
            this.charTyped(typedChar, 0);
        }
    }

    public boolean keyPressed(int pKeyCode, int mod1, int mod2) {
        if (pKeyCode == Keyboard.KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        } else if (this.focused != null && this.focused.keyPressed(pKeyCode, mod1, mod2)){
            return true;
        }

        if (pKeyCode != Keyboard.KEY_RETURN) return false;

        return true;
    }
    public void onClose() {
        super.onClose();
    }


    public boolean shouldCloseOnEsc() {
        return true;
    }
    private void setLineNumbers(boolean show) {
        config.showLineNumbers=show;
        updateButtonStates();
    }

    private void setIntellisenseLevel(SFMClientProgramEditorConfig.IntellisenseLevel level) {
        config.intellisenseLevel=level;
        updateButtonStates();
        parent.onIntellisensePreferenceChanged();
    }

    private void updateButtonStates() {
        lineNumbersOnButton.active = !config.showLineNumbers;
        lineNumbersOffButton.active = config.showLineNumbers;

        intellisenseOffButton.active =
                config.intellisenseLevel != SFMClientProgramEditorConfig.IntellisenseLevel.OFF;
        intellisenseBasicButton.active =
                config.intellisenseLevel != SFMClientProgramEditorConfig.IntellisenseLevel.BASIC;
        intellisenseAdvancedButton.active =
                config.intellisenseLevel != SFMClientProgramEditorConfig.IntellisenseLevel.ADVANCED;
    }
}
