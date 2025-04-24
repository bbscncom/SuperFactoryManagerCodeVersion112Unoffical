package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.ClientTranslationHelpers;
import ca.teamdman.sfm.client.ProgramSyntaxHighlightingHelper;
import ca.teamdman.sfm.client.gui.widget.SFMButtonBuilder;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.diagnostics.SFMDiagnostics;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.logging.TranslatableLogEvent;
import ca.teamdman.sfm.common.net.ServerboundManagerClearLogsPacket;
import ca.teamdman.sfm.common.net.ServerboundManagerLogDesireUpdatePacket;
import ca.teamdman.sfm.common.net.ServerboundManagerSetLogLevelPacket;
import ca.teamdman.sfm.common.registry.SFMPackets;
import com.google.common.collect.Lists;
import my.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

// todo: checkbox for auto-scrolling
public class LogsScreen extends GuiScreenExtend {
    private final ManagerContainerMenu MENU;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private MyMultiLineEditBox textarea;
    private List<ITextComponent> content = Collections.emptyList();
    private int lastSize = 0;
    private Map<Level,Button> levelButtons = new HashMap<>();
    private String lastKnownLogLevel;

    private GuiEventListener focused;

    private final List<GuiEventListener> children = Lists.newArrayList();
    private List<Renderable> renderables=new ArrayList<>();
    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;
    public LogsScreen(ManagerContainerMenu menu) {
//        super(LocalizationKeys.LOGS_SCREEN_TITLE.getComponent());
        this.MENU = menu;
        this.lastKnownLogLevel = MENU.logLevel;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    private boolean shouldRebuildText() {
        return MENU.logs.size() != lastSize;
//        return false;
    }

    private void rebuildText() {
        List<ITextComponent> processedLogs = new ArrayList<>();
        ArrayDeque<TranslatableLogEvent> toProcess = MENU.logs;
        if (toProcess.isEmpty() && MENU.logLevel.equals(Level.OFF.name())) {
            MutableInstant instant = new MutableInstant();
            instant.initFromEpochMilli(System.currentTimeMillis(), 0);
            toProcess.add(new TranslatableLogEvent(
                    Level.INFO,
                    instant,
                    LocalizationKeys.LOGS_GUI_NO_CONTENT.get()
            ));
        }
        for (TranslatableLogEvent log : toProcess) {
            int seconds = (int) (System.currentTimeMillis() - log.instant().getEpochMillisecond()) / 1000;
            int minutes = seconds / 60;
            seconds = seconds % 60;
            ITextComponent ago = new TextComponentString(minutes + "m" + seconds + "s ago").setStyle(new Style().setColor(TextFormatting.GRAY));
            ITextComponent level = new TextComponentString(" [" + log.level() + "] ");
            if (log.level() == Level.ERROR) {
                level = level.setStyle(new Style().setColor(TextFormatting.RED));
            } else if (log.level() == Level.WARN) {
                level = level.setStyle(new Style().setColor(TextFormatting.YELLOW));
            } else if (log.level() == Level.INFO) {
                level = level.setStyle(new Style().setColor(TextFormatting.GREEN));
            } else if (log.level() == Level.DEBUG) {
                level = level.setStyle(new Style().setColor(TextFormatting.AQUA));
            } else if (log.level() == Level.TRACE) {
                level = level.setStyle(new Style().setColor(TextFormatting.DARK_GRAY));
            }

            String[] lines = ClientTranslationHelpers.resolveTranslation(log.contents()).split("\n", -1);

            StringBuilder codeBlock = new StringBuilder();
            boolean insideCodeBlock = false;

            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                ITextComponent lineComponent;

                if (line.equals("```")) {
                    if (insideCodeBlock) {
                        // output processed code
                        List<ITextComponent> codeLines = ProgramSyntaxHighlightingHelper.withSyntaxHighlighting(
                                codeBlock.toString(),
                                false
                        );
                        processedLogs.addAll(codeLines);
                        codeBlock = new StringBuilder();
                    } else {
                        // begin tracking code
                        insideCodeBlock = true;
                    }
                } else if (insideCodeBlock) {
                    codeBlock.append(line).append("\n");
                } else {
                    lineComponent = new TextComponentString(line).setStyle(new Style().setColor(TextFormatting.WHITE));
                    if (i == 0) {
                        lineComponent = ago
                                .appendText(level.getFormattedText())
                                .appendText(level.getFormattedText());
                    }
                    processedLogs.add(lineComponent);
                }
            }
        }
        this.content = processedLogs;


        // update textarea with plain string contents so select and copy works
        StringBuilder sb = new StringBuilder();
        for (ITextComponent line : this.content) {
            sb.append(line.getFormattedText()).append("\n");
        }
        textarea.setValue(sb.toString());
        lastSize = MENU.logs.size();
    }

    public boolean isReadOnly() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player == null || player.isSpectator();
    }

    public void onLogLevelChange() {
        // disable buttons that equal the current level
        for (Map.Entry<Level,Button> entry : levelButtons.entrySet()) {
            Level level = entry.getKey();
            Button button = entry.getValue();
            button.active = !MENU.logLevel.equals(level.name());
        }
        lastKnownLogLevel = MENU.logLevel;
    }

    @Override
    public void initGui() {
        super.initGui();
        this.textarea = this.addRenderableWidget(new MyMultiLineEditBox());

        rebuildText();

//        this.setInitialFocus(textarea);


        Level[] buttons = isReadOnly() ? new Level[]{} : new Level[]{
                Level.OFF,
                Level.TRACE,
                Level.DEBUG,
                Level.INFO,
                Level.WARN,
                Level.ERROR
        };
        int buttonWidth = 60;
        int buttonHeight = 20;
        int spacing = 5;
        int startX = (this.width - (buttonWidth * buttons.length + spacing * 4)) / 2;
        int startY = this.height / 2 - 115;
        int buttonIndex = 0;

        this.levelButtons = new HashMap<>();
        for (Level level : buttons) {
            Button levelButton = new SFMButtonBuilder()
                    .setSize(buttonWidth, buttonHeight)
                    .setPosition(
                            startX + (buttonWidth + spacing) * buttonIndex,
                            startY
                    )
                    .setText(new TextComponentString(level.name()))
                    .setOnPress(button -> {
                        String logLevel = level.name();
                        SFMPackets.sendToServer(new ServerboundManagerSetLogLevelPacket(
                                MENU.windowId,
                                MENU.MANAGER_POSITION,
                                logLevel
                        ));
                        MENU.logLevel = logLevel;
                        onLogLevelChange();
                    })
                    .build();
            levelButtons.put(level, levelButton);
            this.addRenderableWidget(levelButton);
            buttonIndex++;
        }
        onLogLevelChange();


        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(this.width / 2 - 200, this.height / 2 - 100 + 195)
                        .setSize(80, 20)
                        .setText(LocalizationKeys.LOGS_GUI_COPY_LOGS_BUTTON)
                        .setOnPress(this::onCopyLogsClicked)
//                        .setTooltip(this, font, LocalizationKeys.LOGS_GUI_COPY_LOGS_BUTTON_TOOLTIP)
                        .build()
        );
        this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(this.width / 2 - 2 - 100, this.height / 2 - 100 + 195)
                        .setSize(200, 20)
                        .setText(CommonComponents.GUI_DONE)
                        .setOnPress((p_97691_) -> this.onClose())
//                        .setTooltip(this, font, PROGRAM_EDIT_SCREEN_DONE_BUTTON_TOOLTIP)
                        .build()
        );
        if (!isReadOnly()) {
            this.addRenderableWidget(
                    new SFMButtonBuilder()
                            .setPosition(this.width / 2 - 2 + 115, this.height / 2 - 100 + 195)
                            .setSize(80, 20)
                            .setText(LocalizationKeys.LOGS_GUI_CLEAR_LOGS_BUTTON)
                            .setOnPress((button) -> {
                                SFMPackets.sendToServer(new ServerboundManagerClearLogsPacket(
                                        MENU.windowId,
                                        MENU.MANAGER_POSITION
                                ));
                                MENU.logs.clear();
                            })
                            .build()
            );
        }
    }

    private void onCopyLogsClicked(Button button) {
        StringBuilder clip = new StringBuilder();
        clip.append(SFMDiagnostics.getDiagnosticsSummary(
                MENU.getSlot(0).getStack()
        ));
        clip.append("\n-- LOGS --\n");
        if (isShiftKeyDown()) {
            for (TranslatableLogEvent log : MENU.logs) {
                clip.append(log.level().name()).append(" ");
                clip.append(log.instant().toString()).append(" ");
                clip.append(log.contents().getKey());
                for (Object arg : log.contents().getFormatArgs()) {
                    clip.append(" ").append(arg);
                }
                clip.append("\n");
            }
        } else {
            for (ITextComponent line : content) {
                clip.append(line.getFormattedText()).append("\n");
            }
        }
        setClipboardString(clip.toString());
    }

    @Override
    public void onGuiClosed() {
        SFMPackets.sendToServer(new ServerboundManagerLogDesireUpdatePacket(
                MENU.windowId,
                MENU.MANAGER_POSITION,
                false
        ));
        super.onGuiClosed();
    }

    public void scrollToBottom() {
        textarea.scrollToBottom();
    }

    @Override
    public void onResize(Minecraft mc, int x, int y) {
        String prev = this.textarea.getValue();
        this.setWorldAndResolution(mc, x, y);

        super.onResize(mc, x, y);
        this.textarea.setValue(prev);
    }

    @Override
    public void drawScreen(
            int mx,
            int my,
            float partialTicks
    ) {
        for (Renderable renderable : this.renderables) {
            renderable.render(mx, my, partialTicks);
        }
        this.drawDefaultBackground();
        super.drawScreen(mx, my, partialTicks);
        if (!MENU.logLevel.equals(lastKnownLogLevel)) {
            onLogLevelChange();
        }
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
        return true;
    }
    public void onClose() {
        super.onClose();
    }


    public boolean shouldCloseOnEsc() {
        return true;
    }

    // TODO: enable scrolling without focus
    private class MyMultiLineEditBox extends MultiLineEditBox {
        private int frame = 0;
        public MyMultiLineEditBox() {
            super(
                    LogsScreen.this.fontRenderer,
                    LogsScreen.this.width / 2 - 200,
                    LogsScreen.this.height / 2 - 90,
                    400,
                    180,
                    "",
                    ""
            );
        }

        public void scrollToBottom() {
            setScrollAmount(Double.MAX_VALUE);
        }

        @Override
        public void setValue(String p_240160_) {
//            cursorListener = textField::scro
            this.textField.setValue(p_240160_);
//            setCursorPosition(cursor);
        }

        @Override
        public boolean mouseClicked(int p_239101_, int p_239102_, int p_239103_) {
            try {
                return super.mouseClicked(p_239101_, p_239102_, p_239103_);
            } catch (Exception e) {
                SFM.LOGGER.error("Error in LogsScreen.MyMultiLineEditBox.mouseClicked", e);
                return false;
            }
        }

        @Override
        public int getInnerHeight() {
            // parent method uses this.textField.getLineCount() which is split for text wrapping
            // we don't use the wrapped text, so we need to calculate the height ourselves to avoid overshooting
            return this.font.FONT_HEIGHT * (content.size() + 2);
        }

        @Override
        protected void renderContents(int mx, int my, float partialTicks) {
            if (shouldRebuildText()) {
                rebuildText();
            }
            boolean isCursorVisible = this.isFocused() && this.frame++ / 60 % 2 == 0;
            boolean isCursorAtEndOfLine = false;
            int cursorIndex = textField.cursor();
            int lineX = SFMScreenRenderUtils.getX(this) + this.innerPadding();
            int lineY = SFMScreenRenderUtils.getY(this) + this.innerPadding();
            int charCount = 0;
            int cursorX = 0;
            int cursorY = 0;
            MultilineTextField.StringView selectedRange = this.textField.getSelected();
            int selectionStart = selectedRange.beginIndex();
            int selectionEnd = selectedRange.endIndex();

//            for (int line = 0; line < content.size(); ++line) {
            // draw the last 500 lines
            for (int line = Math.max(0, content.size() - 500); line < content.size(); ++line) {
                ITextComponent componentColoured = content.get(line);
                int lineLength = componentColoured.getUnformattedText().length();
                int lineHeight = this.font.FONT_HEIGHT + (line == 0 ? 2 : 0);
                boolean cursorOnThisLine = isCursorVisible
                                           && cursorIndex >= charCount
                                           && cursorIndex <= charCount + lineLength;

                if (cursorOnThisLine) {
                    isCursorAtEndOfLine = cursorIndex == charCount + lineLength;
                    cursorY = lineY;
                    // we draw the raw before coloured in case of token recognition errors
                    // draw before cursor
                    cursorX = SFMFontUtils.drawInBatch(
                            ProgramEditorScreen.substring(componentColoured, 0, cursorIndex - charCount),
                            font,
                            lineX,
                            lineY,
                            true) - 1;
                    SFMFontUtils.drawInBatch(
                            ProgramEditorScreen.substring(componentColoured, cursorIndex - charCount, lineLength),
                            font,
                            cursorX,
                            lineY,
                            true);
                } else {
                    SFMFontUtils.drawInBatch(
                            componentColoured.getFormattedText(),
                            font,
                            lineX,
                            lineY,
                            true);
                }

                // Check if the selection is within the current line
                if (selectionStart <= charCount + lineLength && selectionEnd > charCount) {
                    int lineSelectionStart = Math.max(selectionStart - charCount, 0);
                    int lineSelectionEnd = Math.min(selectionEnd - charCount, lineLength);

                    int highlightStartX = this.font.getStringWidth(ProgramEditorScreen.substring(
                            componentColoured,
                            0,
                            lineSelectionStart
                    ));
                    int highlightEndX = this.font.getStringWidth(ProgramEditorScreen.substring(
                            componentColoured,
                            0,
                            lineSelectionEnd
                    ));

                    SFMScreenRenderUtils.renderHighlight(
                            lineX + highlightStartX,
                            lineY,
                            lineX + highlightEndX,
                            lineY + lineHeight
                    );
                }

                lineY += lineHeight;
                charCount += lineLength + 1;
            }

            if (isCursorAtEndOfLine) {
                SFMFontUtils.draw(
                        "_",
                        cursorX,
                        cursorY,
                        -1,
                        true
                );
            } else {
                //todo fill-> drawRect?
                Gui.drawRect(cursorX, cursorY - 1, cursorX + 1, cursorY + 1 + 9, -1);
            }
        }
    }
}

