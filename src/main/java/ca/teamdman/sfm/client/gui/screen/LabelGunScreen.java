package ca.teamdman.sfm.client.gui.screen;


import ca.teamdman.sfm.client.gui.widget.SFMButtonBuilder;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.net.ServerboundLabelGunClearPacket;
import ca.teamdman.sfm.common.net.ServerboundLabelGunCycleViewModePacket;
import ca.teamdman.sfm.common.net.ServerboundLabelGunPrunePacket;
import ca.teamdman.sfm.common.net.ServerboundLabelGunUpdatePacket;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import ca.teamdman.sfm.common.registry.SFMPackets;
import com.google.common.collect.Lists;
import my.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LabelGunScreen extends GuiScreenExtend {
    private final EnumHand HAND;
    private final LabelPositionHolder LABEL_HOLDER;
    private final ArrayList<Button> labelButtons = new ArrayList<>();
    @SuppressWarnings("NotNullFieldNotInitialized")
    private EditBox labelField;
    private boolean shouldRebuildWidgets = false;
    private String tempActiveLabelname = "";
    private GuiEventListener focused;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private List<Renderable> renderables=new ArrayList<>();
    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;
    public LabelGunScreen(ItemStack stack, EnumHand hand) {
//        super(LocalizationKeys.LABEL_GUN_GUI_TITLE.getComponent());
        LABEL_HOLDER = LabelPositionHolder.from(stack);
        HAND = hand;
    }

    @Override
    public void initGui() {
        super.initGui();
        rebuildWidgets();

        SFMScreenRenderUtils.enableKeyRepeating();
        this.labelField = addRenderableWidget(new EditBox(
                this.fontRenderer,
                this.width / 2 - 150,
                50,
                300,
                20,
                LocalizationKeys.LABEL_GUN_GUI_LABEL_PLACEHOLDER.getComponent()
        ));
        this.labelField.setResponder(this::onTextUpdated);
        this.labelField.setSuggestion(LocalizationKeys.LABEL_GUN_GUI_LABEL_EDIT_PLACEHOLDER.getString());

//        this.setInitialFocus(labelField);
        this.setFocused(labelField);
        this.labelField.setFocused(true);

        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setSize(50, 20)
                        .setPosition(this.width / 2 - 210, 50)
                        .setText(LocalizationKeys.LABEL_GUN_GUI_CLEAR_BUTTON)
                        .setOnPress((btn) -> {
                            SFMPackets.sendToServer(new ServerboundLabelGunClearPacket(HAND));
                            LABEL_HOLDER.clear();
                            shouldRebuildWidgets = true;
                        })
                        .build()
        );
        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setSize(50, 20)
                        .setPosition(this.width / 2 + 160, 50)
                        .setText(LocalizationKeys.LABEL_GUN_GUI_PRUNE_BUTTON)
                        .setOnPress((btn) -> {
                            SFMPackets.sendToServer(new ServerboundLabelGunPrunePacket(HAND));
                            LABEL_HOLDER.prune();
                            shouldRebuildWidgets = true;
                        })
                        .build()
        );
        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setSize(200, 20)
                        .setPosition(this.width / 2 - 2 - 100, this.height - 25)
                        .setText(LocalizationKeys.LABEL_GUN_GUI_CYCLE_VIEW_BUTTON)
                        .setOnPress((btn) -> {
                            SFMPackets.sendToServer(new ServerboundLabelGunCycleViewModePacket(HAND));
                            onClose();
                        })
                        .build()
        );
        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setSize(300, 20)
                        .setPosition(this.width / 2 - 2 - 150, this.height - 50)
                        .setText(CommonComponents.GUI_DONE)
                        .setOnPress((p_97691_) -> this.onDone())
                        .build()
        );
        onTextUpdated("");
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
        onDone();

        return true;
    }
    public void onClose() {
        super.onClose();
    }


    public boolean shouldCloseOnEsc() {
        return true;
    }


    public void onDone() {
        SFMPackets.sendToServer(new ServerboundLabelGunUpdatePacket(
                this.tempActiveLabelname,
                HAND
        ));
        onClose();
    }

    @Override
    public void onResize(Minecraft mc, int x, int y) {
        String prev = this.labelField.getValue();
        this.setWorldAndResolution(mc, x, y);
        super.onResize(mc, x, y);
        this.labelField.setValue(prev);
    }

    @Override
    public void drawScreen(
            int mx,
            int my,
            float partialTicks
    ) {
        if (shouldRebuildWidgets) {
            // we delay this because focus gets reset _after_ the button event handler
            // we want to end with the label input field focused
            shouldRebuildWidgets = false;
            for (Button labelButton : this.labelButtons) {
                this.removeWidget(labelButton);
            }
            this.labelButtons.clear();

        }

        this.drawDefaultBackground();

        for (Renderable renderable : this.renderables) {
            renderable.render(mx, my, partialTicks);
        }

        super.drawScreen(mx, my, partialTicks);
    }

    private void rebuildWidgets() {
        this.buttonList.clear();     // 清空所有按钮
        this.labelList.clear();
        this.children.clear();
        this.renderables.clear();
        this.labelButtons.clear();
    }


    private void onTextUpdated(String newText) {
        labelField.setSuggestion(newText.isEmpty() ? LocalizationKeys.LABEL_GUN_GUI_LABEL_EDIT_PLACEHOLDER.getString() : "");
        labelButtons.forEach(this::removeWidget);
        labelButtons.clear();

        int buttonWidth = LABEL_HOLDER.labels.entrySet().stream()
                                  .map(entry -> LocalizationKeys.LABEL_GUN_GUI_LABEL_BUTTON.getComponent(entry.getKey(), entry.getValue()
                                          .size()).getUnformattedText()).mapToInt(this.fontRenderer::getStringWidth).max().orElse(50) + 10;
        int paddingX = 5;
        int paddingY = 5;
        int buttonHeight = 20;

        int buttonsPerRow = this.width / (buttonWidth + paddingX);

        int i = 0;
        List<String> labels = LABEL_HOLDER.labels.keySet().stream()
                .filter(text -> text.toLowerCase().contains(newText.toLowerCase()))
                .sorted(Comparator.naturalOrder()).collect(Collectors.toList());

        for (String label : labels) {
            int x = (this.width - (buttonWidth + paddingX) * Math.min(buttonsPerRow, labels.size())) / 2 + paddingX + (i % buttonsPerRow) * (buttonWidth + paddingX);
            int y = 80 + (i / buttonsPerRow) * (buttonHeight + paddingY);
            addLabelButton(label, x, y, buttonWidth, buttonHeight);

            i++;
        }
    }

    private void addLabelButton(
            String label,
            int x,
            int y,
            int width,
            int height
    ) {
        int count = LABEL_HOLDER.getPositions(label).size();
        Button button = new SFMButtonBuilder()
                .setSize(width, height)
                .setPosition(x, y)
                .setText(LocalizationKeys.LABEL_GUN_GUI_LABEL_BUTTON.getComponent(label, count))
                .setOnPress((btn) -> {
                    this.tempActiveLabelname=label;
                    this.onDone();
                })
                .build();
        labelButtons.add(button);
        this.addRenderableWidget(button);
    }

}
