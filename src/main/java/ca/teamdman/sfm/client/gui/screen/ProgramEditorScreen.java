package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.ProgramSyntaxHighlightingHelper;
import ca.teamdman.sfm.client.ProgramTokenContextActions;
import ca.teamdman.sfm.client.gui.widget.PickList;
import ca.teamdman.sfm.client.gui.widget.PickListItem;
import ca.teamdman.sfm.client.gui.widget.SFMButtonBuilder;
import ca.teamdman.sfm.common.config.SFMClientProgramEditorConfig;
import ca.teamdman.sfm.common.config.SFMConfig;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.util.MCVersionDependentBehaviour;
import ca.teamdman.sfm.common.util.SFMDisplayUtils;
import ca.teamdman.sfml.ast.Program;
import ca.teamdman.sfml.intellisense.IntellisenseAction;
import ca.teamdman.sfml.intellisense.IntellisenseContext;
import ca.teamdman.sfml.intellisense.SFMLIntellisense;
import ca.teamdman.sfml.manipulation.ManipulationResult;
import ca.teamdman.sfml.manipulation.ProgramStringManipulationUtils;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;
import ca.teamdman.sfml.program_builder.ProgramBuilder;
import com.google.common.collect.Lists;
import my.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@SuppressWarnings("NotNullFieldNotInitialized")
public class ProgramEditorScreen extends GuiScreenExtend {
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<Renderable> renderables=new ArrayList<>();
    private final ProgramEditScreenOpenContext openContext;
    protected MyMultiLineEditBox textarea;
    protected String lastProgram = "";
    protected List<ITextComponent> lastProgramWithSyntaxHighlighting = new ArrayList<>();
    protected PickList<IntellisenseAction> suggestedActions;
    private GuiEventListener focused;

    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;

    public ProgramEditorScreen(
            ProgramEditScreenOpenContext openContext
    ) {
//        super(LocalizationKeys.PROGRAM_EDIT_SCREEN_TITLE.getComponent());
        this.openContext = openContext;
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

    public static String substring(
            ITextComponent component,
            int start,
            int end
    ) {
        ITextComponent rtn = new TextComponentString("");
        AtomicInteger seen = new AtomicInteger(0);
        for (ITextComponent sibling : component.getSiblings()) {
            String content = sibling.getUnformattedText();
            int contentStart = Math.max(start - seen.get(), 0);
            int contentEnd = Math.min(end - seen.get(), content.length());

            if (contentStart < contentEnd) {
                rtn.appendSibling(new TextComponentString(content.substring(contentStart, contentEnd)).setStyle(sibling.getStyle()));
            }
            seen.addAndGet(content.length());
        }
        return rtn.getFormattedText();
    }

    public void scrollToTop() {
        this.textarea.scrollToTop();
    }

    // isgamepause()
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    /**
     * The user has indicated to save by hitting Shift+Enter or by pressing the Done button
     */
    public void saveAndClose() {
        openContext.getSaveCallback().accept(textarea.getValue());
        SFMScreenChangeHelpers.popScreen();
    }

    public void closeWithoutSaving() {
        SFMScreenChangeHelpers.popScreen();
    }

    public void onIntellisensePreferenceChanged() {
        textarea.rebuildIntellisense();
    }

    /**
     * The user has tried to close the GUI without saving by hitting the Esc key
     */
    @Override
    public void onGuiClosed() {

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


    void setDragging(boolean pIsDragging) {
        this.dragging=pIsDragging;
    }

    boolean isDragging() {
        return dragging;
    }
    Optional<GuiEventListener> getChildAt(int pMouseX, int pMouseY) {
        for (GuiEventListener guieventlistener : this.children) {
            if (guieventlistener.isMouseOver(pMouseX, pMouseY)) {
                return Optional.of(guieventlistener);
            }
        }

        return Optional.empty();
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
    //      this version no keyReleased
//    @Override
//    public boolean keyReleased(
//            int pKeyCode,
//            int pScanCode,
//            int pModifiers
//    ) {
//        if (pKeyCode == GLFW.GLFW_KEY_LEFT_CONTROL || pKeyCode == GLFW.GLFW_KEY_RIGHT_CONTROL) {
//            // if control released => update syntax highlighting
//            textarea.rebuild(GuiScreen.isCtrlKeyDown());
//            return true;
//        }
//        return false;
//    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        try {
            super.keyTyped(typedChar, keyCode);

            // 模拟高版本 keyPressed
            boolean b = this.keyPressed(keyCode, -1, 0);
            if(b)return;

            // 模拟高版本 charTyped，仅在字符有效时调用
            if (typedChar != 0 && Tools.isAllowedChatCharacter(typedChar)) {
                this.charTyped(typedChar, 0);
            }
        }catch (Exception e){
            e.toString();
        }
    }

    public boolean charTyped(
            char pCodePoint,
            int pModifiers
    ) {
        if (GuiScreen.isCtrlKeyDown() && pCodePoint == ' ') {
            return true;
        }
        if (!suggestedActions.isEmpty() && pCodePoint == '\\') {
            // prevent intellisense-accept hotkey from being typed
            return true;
        }
        return this.getFocused() != null && this.getFocused().charTyped(pCodePoint, pModifiers);
    }

    public boolean keyPressed(
            int pKeyCode,
            int pScanCode,
            int pModifiers
    ) {
        if ((pKeyCode == Keyboard.KEY_RETURN || pKeyCode == Keyboard.KEY_NUMPADENTER) && GuiScreen.isShiftKeyDown()) {
            saveAndClose();
            return true;
        }

        if (pKeyCode == Keyboard.KEY_TAB) {
            String content = textarea.getValue();
            int cursor = textarea.getCursorPosition();
            int selectionCursor = textarea.getSelectionCursorPosition();
            double scrollAmount = textarea.getScrollAmount();
            ManipulationResult result;
            if (GuiScreen.isShiftKeyDown()) {
                result = ProgramStringManipulationUtils.deindent(content, cursor, selectionCursor);
            } else {
                result = ProgramStringManipulationUtils.indent(content, cursor, selectionCursor);
            }
            textarea.setValue(result.content());
            textarea.setCursorPosition(result.cursorPosition());
            textarea.setSelectionCursorPosition(result.selectionCursorPosition());
            textarea.setScrollAmount(scrollAmount);
            return true;
        }

        if (pKeyCode == Keyboard.KEY_BACKSLASH && !suggestedActions.isEmpty()) {
            IntellisenseAction action = suggestedActions.getSelected();
            assert action != null;
            ManipulationResult result = action.perform(
                    new IntellisenseContext(
                            ProgramBuilder.build(textarea.getValue()),
                            textarea.getCursorPosition(),
                            textarea.getSelectionCursorPosition(),
                            openContext.getLabelPositionHolder(),
                            SFMConfig.CLIENT_PROGRAM_EDITOR.intellisenseLevel
                    )
            );
            double scrollAmount = textarea.getScrollAmount();
            textarea.setValue(result.content());
            textarea.setSelectionCursorPosition(result.selectionCursorPosition());
            textarea.setCursorPosition(result.cursorPosition());
            textarea.setScrollAmount(scrollAmount);
            return true;
        }

        if (pKeyCode == Keyboard.KEY_LCONTROL || pKeyCode == Keyboard.KEY_RCONTROL) {
            textarea.rebuild(GuiScreen.isCtrlKeyDown());
            return true;
        }

        if (pKeyCode == Keyboard.KEY_SLASH && GuiScreen.isCtrlKeyDown()) {
            String content = textarea.getValue();
            int cursor = textarea.getCursorPosition();
            int selectionCursor = textarea.getSelectionCursorPosition();
            ManipulationResult result = ProgramStringManipulationUtils.toggleComments(content, cursor, selectionCursor);
            textarea.setValue(result.content());
            textarea.setCursorPosition(result.cursorPosition());
            textarea.setSelectionCursorPosition(result.selectionCursorPosition());
            return true;
        }

        if (pKeyCode == Keyboard.KEY_SPACE && GuiScreen.isCtrlKeyDown()) {
            ProgramTokenContextActions.getContextAction(
                            textarea.getValue(),
                            textarea.getCursorPosition()
                    )
                    .ifPresent(Runnable::run);

            textarea.rebuild(false);
            return true;
        }

        if ((pKeyCode == Keyboard.KEY_UP || pKeyCode == Keyboard.KEY_DOWN) && !suggestedActions.getItems().isEmpty()) {
            if (pKeyCode == Keyboard.KEY_UP) {
                suggestedActions.selectPreviousWrapping();
            } else {
                suggestedActions.selectNextWrapping();
            }
            return true;
        }

        if (pKeyCode == Keyboard.KEY_ESCAPE && !suggestedActions.isEmpty()) {
            suggestedActions.clear();
            return true;
        }

        if (pKeyCode == Keyboard.KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        } else if (this.getFocused() != null && this.getFocused().keyPressed(pKeyCode, pScanCode, pModifiers)){
            return true;
        }
//        else {
//            FocusNavigationEvent focusnavigationevent = (FocusNavigationEvent)(switch (pKeyCode) {
//                case Keyboard.KEY_TAB -> this.createTabEvent();
//                default -> null;
//                case Keyboard.KEY_RIGHT -> this.createArrowEvent(ScreenDirection.RIGHT);
//                case Keyboard.KEY_LEFT -> this.createArrowEvent(ScreenDirection.LEFT);
//                case Keyboard.KEY_DOWN -> this.createArrowEvent(ScreenDirection.DOWN);
//                case Keyboard.KEY_UP -> this.createArrowEvent(ScreenDirection.UP);
//            });
//            if (focusnavigationevent != null) {
//                ComponentPath componentpath = super.nextFocusPath(focusnavigationevent);
//                if (componentpath == null && focusnavigationevent instanceof FocusNavigationEvent.TabNavigation) {
//                    this.clearFocus();
//                    componentpath = super.nextFocusPath(focusnavigationevent);
//                }
//
//                if (componentpath != null) {
//                    this.changeFocus(componentpath);
//                }
//            }
//            return false;
//        }
        return false;
    }
    public boolean shouldCloseOnEsc() {
        return true;
    }
    public void onClose() {
        // If the content is different, ask to save
        if (!openContext.getProgramString().equals(textarea.getValue())) {
            GuiYesNo exitWithoutSavingConfirmScreen = getExitWithoutSavingConfirmScreen();
            SFMScreenChangeHelpers.setOrPushScreen(exitWithoutSavingConfirmScreen);
            exitWithoutSavingConfirmScreen.setButtonDelay(20);
        } else{
            super.onClose();
        }
    }



    @Override
    public void onResize(
            Minecraft mc,
            int x,
            int y
    ) {
        String prev = this.textarea.getValue();
        this.setWorldAndResolution(mc, width, height);
        super.onResize(mc, x, y);
        this.textarea.setValue(prev);
    }

    @Override
    public void drawScreen(int mx, int my, float partialTicks) {
        this.drawDefaultBackground();

//        this.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        for (Renderable renderable : this.renderables) {
            renderable.render(mx, my, partialTicks);
        }

        super.drawScreen(mx, my, partialTicks);
    }

    private static boolean shouldShowLineNumbers() {
        return SFMClientProgramEditorConfig.showLineNumbers;
    }

//    protected void renderTooltip(
//            PoseStack pose,
//            int mx,
//            int my
//    ) {
//        if (Minecraft.getInstance().screen != this) {
//            // this should fix the annoying Ctrl+E popup when editing
//            this.renderables
//                    .stream()
//                    .filter(AbstractWidget.class::isInstance)
//                    .map(AbstractWidget.class::cast)
//                    .forEach(w -> w.setFocused(false));
//            return;
//        }
//        drawChildTooltips(pose, mx, my);
//    }

//    private void drawChildTooltips(
//            PoseStack pose,
//            int mx,
//            int my
//    ) {
        //// 1.19.2: manually render button tooltips
//        this.renderables
//                .stream()
//                .filter(SFMExtendedButtonWithTooltip.class::isInstance)
//                .map(SFMExtendedButtonWithTooltip.class::cast)
//                .forEach(x -> x.renderToolTip(pose, mx, my));
//    }

    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T pWidget) {
        this.renderables.add(pWidget);
        return this.addWidget(pWidget);
    }

    protected <T extends GuiEventListener > T addWidget(T pListener) {
        this.children.add(pListener);
        return pListener;
    }

    @Override
    public void initGui() {
        super.initGui();
        SFMScreenRenderUtils.enableKeyRepeating();

        this.textarea = this.addRenderableWidget(new MyMultiLineEditBox());

        this.suggestedActions = this.addRenderableWidget(new PickList<>(
                this.fontRenderer,
                0,
                0,
                180,
                this.fontRenderer.FONT_HEIGHT * 6,
                LocalizationKeys.INTELLISENSE_PICK_LIST_GUI_TITLE.getComponent(),
                new ArrayList<>()
        ));

        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(this.width / 2 - 200, this.height / 2 - 100 + 195)
                        .setSize(16, 20)
                        .setText(new TextComponentString("#"))
                        .setOnPress((button) -> {
                            int cursorPos = textarea.getCursorPosition();
                            int selectionCursorPos = textarea.getSelectionCursorPosition();
                            SFMScreenChangeHelpers.setOrPushScreen(
                                    new ProgramEditorConfigScreen(
                                            this,
                                            SFMConfig.CLIENT_PROGRAM_EDITOR,
                                            () -> {
//                                                this.setInitialFocus(textarea);
                                                textarea.setCursorPosition(cursorPos);
                                                textarea.setSelectionCursorPosition(selectionCursorPos);
                                            }
                                    )
                            );
                        })
//                        .setTooltip(this, font, PROGRAM_EDIT_SCREEN_CONFIG_BUTTON_TOOLTIP)
                        .build()
        );
        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(this.width / 2 - 2 - 150, this.height / 2 - 100 + 195)
                        .setSize(200, 20)
                        .setText(CommonComponents.GUI_DONE)
                        .setOnPress((button) -> this.saveAndClose())
//                        .setTooltip(this, font, PROGRAM_EDIT_SCREEN_DONE_BUTTON_TOOLTIP)
                        .build()
        );
        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(this.width / 2 - 2 + 100, this.height / 2 - 100 + 195)
                        .setSize(100, 20)
                        .setText(CommonComponents.GUI_CANCEL)
                        .setOnPress((button) -> this.onClose())
                        .build()
        );

        textarea.setValue(openContext.getProgramString());
//        this.setInitialFocus(textarea);
    }


    protected @NotNull GuiYesNo getSaveConfirmScreen(Runnable onConfirm) {
        return new GuiYesNoExtend(
                (result, id) -> {
                    SFMScreenChangeHelpers.popScreen(); // Close confirm screen

                    if (result) {
                        onConfirm.run();
                    } else {
                        // do nothing, continue editing
                    }
                },
                LocalizationKeys.SAVE_CHANGES_CONFIRM_SCREEN_TITLE.getComponent().getUnformattedComponentText(),
                LocalizationKeys.SAVE_CHANGES_CONFIRM_SCREEN_MESSAGE.getComponent().getUnformattedComponentText(),
                LocalizationKeys.SAVE_CHANGES_CONFIRM_SCREEN_YES_BUTTON.getComponent().getUnformattedComponentText(),
                LocalizationKeys.SAVE_CHANGES_CONFIRM_SCREEN_NO_BUTTON.getComponent().getUnformattedComponentText(),
                0
        );
    }

    protected @NotNull GuiYesNo getExitWithoutSavingConfirmScreen() {
        return new GuiYesNoExtend(
                (result, id) -> {
                    SFMScreenChangeHelpers.popScreen();
                    // Only close editor if user confirms
                    if (result) {
                        closeWithoutSaving();
                    }
                },
                LocalizationKeys.EXIT_WITHOUT_SAVING_CONFIRM_SCREEN_TITLE.getComponent().getUnformattedComponentText(),
                LocalizationKeys.EXIT_WITHOUT_SAVING_CONFIRM_SCREEN_MESSAGE.getComponent().getUnformattedComponentText(),
                LocalizationKeys.EXIT_WITHOUT_SAVING_CONFIRM_SCREEN_YES_BUTTON.getComponent().getUnformattedComponentText(),
                LocalizationKeys.EXIT_WITHOUT_SAVING_CONFIRM_SCREEN_NO_BUTTON.getComponent().getUnformattedComponentText(),
                0
        );
    }

    protected class MyMultiLineEditBox extends MultiLineEditBox {
        private int frame = 0;

        public MyMultiLineEditBox() {
            super(
                    ProgramEditorScreen.this.fontRenderer,
                    ProgramEditorScreen.this.width / 2 - 200,
                    ProgramEditorScreen.this.height / 2 - 110,
                    400,
                    200,
                    "",
                    ""
            );
            this.textField.setValueListener(this::onValueOrCursorChanged);
            this.textField.setCursorListener(() -> this.onValueOrCursorChanged(this.textField.value()));
        }

        public void scrollToTop() {
            this.setScrollAmount(0);
        }

        public int getCursorPosition() {
            return this.textField.cursor;
        }

        public void setCursorPosition(int cursor) {
            this.textField.seekCursor(Whence.ABSOLUTE, cursor);
        }

        public int getLineNumberWidth() {
            if (shouldShowLineNumbers()) {
                return this.font.getStringWidth("000");
            } else {
                return 0;
            }
        }

        @MCVersionDependentBehaviour
        @Override
        public boolean mouseClicked(
                int pMouseX,
                int pMouseY,
                int pButton
        ) {
            // Accommodate line numbers
            if (pMouseX >= this.getX() + 1 && pMouseX <= this.getX() + this.width - 1) {
                pMouseX -= getLineNumberWidth();
            }

            // we need to override the default behaviour because Mojang broke it
            // if it's not scrolling, it should return false for cursor click movement
            boolean rtn;
            if (!this.visible) {
                rtn = false;
            } else {
                //noinspection unused
                boolean flag = this.withinContentAreaPoint(pMouseX, pMouseY);
                boolean flag1 = this.scrollbarVisible()
                        && pMouseX >= (double) (this.getX() + this.width)
                        && pMouseX <= (double) (this.getX() + this.width + 8)
                        && pMouseY >= (double) this.getY()
                        && pMouseY < (double) (this.getY() + this.height);
                if (flag1 && pButton == 0) {
                    this.scrolling = true;
                    rtn = true;
                } else {
                    //1.19.4 behaviour:
                    //rtn=flag || flag1;
                    // instead, we want to return false if we're not scrolling
                    // (like how it was in 1.19.2)
                    // https://bugs.mojang.com/browse/MC-262754
                    rtn = false;
                }
            }

            if (rtn) {
                return true;
            } else if (this.withinContentAreaPoint(pMouseX, pMouseY) && pButton == 0) {
                this.textField.setSelecting(GuiScreen.isShiftKeyDown());
                this.seekCursorScreen(pMouseX, pMouseY);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public int getInnerHeight() {
            // parent method uses this.textField.getLineCount() which is split for text wrapping
            // we don't use the wrapped text, so we need to calculate the height ourselves to avoid overshooting
            return this.font.FONT_HEIGHT * (lastProgramWithSyntaxHighlighting.size() + 2);
        }

        @Override
        public boolean mouseDragged(
                int mx,
                int my,
                int button,
                int dx,
                int dy
        ) {
            // if mouse in bounds, translate to accommodate line numbers
            int thisX = SFMScreenRenderUtils.getX(this);
            if (mx >= thisX + 1 && mx <= thisX + this.width - 1) {
                mx -= getLineNumberWidth();
            }
            return super.mouseDragged(mx, my, button, dx, dy);
        }


        public int getSelectionCursorPosition() {
            return this.textField.selectCursor;
        }

        public void setSelectionCursorPosition(int cursor) {
            this.textField.selectCursor = cursor;
        }

        public double getScrollAmount() {
            return this.getScrollAmount();
        }

        @Override
        public void setScrollAmount(double d) {
            super.setScrollAmount(d);
        }

        private void onValueOrCursorChanged(String programString) {
            int cursorPosition = getCursorPosition();

            // Build the program
            ProgramBuildResult buildResult = ProgramBuilder.build(programString);

            // Update the intellisense picklist
            IntellisenseContext intellisenseContext = new IntellisenseContext(
                    buildResult,
                    cursorPosition,
                    getSelectionCursorPosition(),
                    openContext.getLabelPositionHolder(),
                    SFMConfig.CLIENT_PROGRAM_EDITOR.intellisenseLevel
            );
            List<IntellisenseAction> suggestions = SFMLIntellisense.getSuggestions(intellisenseContext);
            ProgramEditorScreen.this.suggestedActions.setItems(suggestions);


            // Update the intellisense picklist query used to sort the suggestions
            String cursorWord = buildResult.getWordAtCursorPosition(cursorPosition);
            ProgramEditorScreen.this.suggestedActions.setQuery(new TextComponentString(cursorWord));

            boolean shouldPrint = false;
            //noinspection ConstantValue
            if (shouldPrint) {
                String cursorPositionDisplay = SFMDisplayUtils.getCursorPositionDisplay(programString, cursorPosition);
                String cursorTokenDisplay = SFMDisplayUtils.getCursorTokenDisplay(buildResult, cursorPosition);
                String tokenHierarchyDisplay;
                @Nullable Program program = buildResult.getProgram();
                if (program == null) {
                    tokenHierarchyDisplay = "<INVALID PROGRAM>";
                } else {
                    tokenHierarchyDisplay = SFMDisplayUtils.getTokenHierarchyDisplay(program, cursorPosition);
                }

                String suggestionsDisplay = suggestedActions.getItems()
                        .stream()
                        .map(PickListItem::getComponent)
                        .map(ITextComponent::getUnformattedText)
                        .collect(Collectors.joining(", "));

                SFM.LOGGER.info(
                        "PROGRAM OR CURSOR CHANGE! {}   {}   {}  |||  {} ||| {}",
                        cursorPositionDisplay,
                        cursorTokenDisplay,
                        tokenHierarchyDisplay,
                        cursorWord,
                        suggestionsDisplay
                );
            }
        }

        private void rebuildIntellisense() {
            onValueOrCursorChanged(getValue());
        }

        /**
         * Rebuilds the syntax-highlighted program text.
         * This runs more frequently than when the value is changed.
         *
         * @param showContextActionHints Should underline words that have context actions
         */
        private void rebuild(boolean showContextActionHints) {
            lastProgram = this.textField.value();
            lastProgramWithSyntaxHighlighting = ProgramSyntaxHighlightingHelper.withSyntaxHighlighting(
                    lastProgram,
                    showContextActionHints
            );
        }

        @Override
        protected void renderContents(int mx, int my, float partialTicks) {
            if (!lastProgram.equals(this.textField.value())) {
                rebuild(GuiScreen.isCtrlKeyDown());
            }
            List<ITextComponent> lines = lastProgramWithSyntaxHighlighting;
            boolean isCursorVisible = this.isFocused() && this.frame++ / 60 % 2 == 0;
            boolean isCursorAtEndOfLine = false;
            int cursorIndex = textField.cursor();
            int lineX = SFMScreenRenderUtils.getX(this) + this.innerPadding() + getLineNumberWidth();
            int lineY = SFMScreenRenderUtils.getY(this) + this.innerPadding();
            int charCount = 0;
            int cursorX = 0;
            int cursorY = 0;
            MultilineTextField.StringView selectedRange = this.textField.getSelected();
            int selectionStart = selectedRange.beginIndex();
            int selectionEnd = selectedRange.endIndex();

            for (int line = 0; line < lines.size(); ++line) {
                ITextComponent componentColoured = lines.get(line);
                int lineLength = componentColoured.getUnformattedText().length();
                int lineHeight = this.font.FONT_HEIGHT;
                boolean cursorOnThisLine = isCursorVisible
                        && cursorIndex >= charCount
                        && cursorIndex <= charCount + lineLength;
//                buffer = graphics.bufferSource();
                if (shouldShowLineNumbers()) {
                    // Draw line number
                    String lineNumber = String.valueOf(line + 1);
                    SFMFontUtils.drawInBatch(
                            lineNumber,
                            this.font,
                            lineX - 2 - this.font.getStringWidth(lineNumber),
                            lineY,
                            true
                    );
                }

                if (cursorOnThisLine) {
                    isCursorAtEndOfLine = cursorIndex == charCount + lineLength;
                    cursorY = lineY;
                    // draw text before cursor
                    cursorX = SFMFontUtils.drawInBatch(
                            substring(componentColoured, 0, cursorIndex - charCount),
                            font,
                            lineX,
                            lineY,
                            true
                    ) - 1;
                    ProgramEditorScreen.this.suggestedActions.setXY(cursorX + 10, cursorY);
                    // draw text after cursor
                    SFMFontUtils.drawInBatch(
                            substring(componentColoured, cursorIndex - charCount, lineLength),
                            font,
                            cursorX,
                            lineY,
                            true
                    );
                } else {
                    SFMFontUtils.drawInBatch(
                            componentColoured,
                            font,
                            lineX,
                            lineY,
                            true
                    );
                }

                // Check if the selection is within the current line
                if (selectionStart <= charCount + lineLength && selectionEnd > charCount) {
                    int lineSelectionStart = Math.max(selectionStart - charCount, 0);
                    int lineSelectionEnd = Math.min(selectionEnd - charCount, lineLength);

                    int highlightStartX = this.font.getStringWidth(substring(componentColoured, 0, lineSelectionStart));
                    int highlightEndX = this.font.getStringWidth(substring(componentColoured, 0, lineSelectionEnd));

                    SFMScreenRenderUtils.renderHighlight(
                            lineX + highlightStartX,
                            lineY,
                            lineX + highlightEndX,
                            lineY + lineHeight
                    );
                    Style style = componentColoured.getStyle();
                    SFMFontUtils.drawInBatch(
                            new TextComponentString(componentColoured.getUnformattedText().substring(lineSelectionStart,lineSelectionEnd)).setStyle(style),
                            font,
                            lineX + highlightStartX,
                            lineY,
                            true);
                }

                lineY += lineHeight;
                charCount += lineLength +1;
            }

            if (isCursorAtEndOfLine) {
                SFMFontUtils.draw(font,"_", cursorX, cursorY, -1, true);
            } else {
                Gui.drawRect(cursorX-1, cursorY - 1, cursorX, cursorY + 1 + 9, -1);
            }
        }
    }
}

