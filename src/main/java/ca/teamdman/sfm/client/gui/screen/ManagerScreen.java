package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.client.gui.widget.SFMButtonBuilder;
import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.capability.AbstarctCapabilityProvider;
import ca.teamdman.sfm.common.command.ConfigCommandBehaviourInput;
import ca.teamdman.sfm.common.container.ManagerContainerMenu;
import ca.teamdman.sfm.common.diagnostics.SFMDiagnostics;
import ca.teamdman.sfm.common.item.DiskItem;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.net.*;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfm.common.registry.SFMDataComponents;
import ca.teamdman.sfm.common.registry.SFMPackets;
import ca.teamdman.sfm.common.util.MCVersionDependentBehaviour;
import ca.teamdman.sfml.ast.Program;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import my.Button;
import my.GuiEventListener;
import my.Renderable;
import my.Tools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.logging.log4j.Level;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ca.teamdman.sfm.common.localization.LocalizationKeys.*;

@SuppressWarnings({"FieldCanBeLocal", "unused", "NotNullFieldNotInitialized"})
public class ManagerScreen extends GuiContainerExtend {
    private static final ResourceLocation BACKGROUND_TEXTURE_LOCATION = new ResourceLocation(
            SFM.MOD_ID,
            "textures/gui/container/manager.png"
    );
    private final float STATUS_DURATION = 40;
    private ITextComponent status = new TextComponentString("");
    private float statusCountdown = 0;
    private Button diagButton;
    private Button clipboardPasteButton;
    private Button clipboardCopyButton;
    private Button discordButton;
    private Button resetButton;
    private Button editButton;
    private Button examplesButton;
    private Button logsButton;
    private Button rebuildButton;
    private Button serverConfigButton;


    private GuiEventListener focused;

    private final List<GuiEventListener> children = Lists.newArrayList();
    private List<Renderable> renderables = new ArrayList<>();
    private boolean dragging;
    private int lastMouseX;
    private int lastMouseY;

    private ManagerContainerMenu menu;

    public ManagerScreen(
            ManagerContainerMenu menu
//            Inventory inv,
//            ITextComponent title
    ) {
        super(menu);
        this.menu = menu;
    }

    public List<Button> getButtonsForJEIExclusionZones() {
        return Tools.of(
                clipboardPasteButton,
                editButton,
                examplesButton,
                clipboardCopyButton,
                logsButton,
                rebuildButton,
                serverConfigButton
        );
    }

    public boolean isReadOnly() {
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        return player == null || player.isSpectator();
    }

    public void updateVisibilities() {
        boolean diskPresent = !inventorySlots.getSlot(0).getStack().isEmpty();
        diagButton.visible = shouldShowDiagButton();
        clipboardCopyButton.visible = diskPresent;
        logsButton.visible = diskPresent;
        rebuildButton.visible = diskPresent && !isReadOnly();
        clipboardPasteButton.visible = diskPresent && !isReadOnly();
        resetButton.visible = diskPresent && !isReadOnly();
        editButton.visible = diskPresent && !isReadOnly();
    }

    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T pWidget) {
        this.renderables.add(pWidget);
        return this.addWidget(pWidget);
    }

    protected <T extends GuiEventListener> T addWidget(T pListener) {
        this.children.add(pListener);
        return pListener;
    }

    protected void removeWidget(GuiEventListener pListener) {
        if (pListener instanceof Renderable) {
            this.renderables.remove((Renderable) pListener);
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
        this.dragging = pIsDragging;
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

        if (mouseButton == 0) {
            lastMouseX = mouseX;
            lastMouseY = mouseY;
        }

        for (GuiEventListener guieventlistener : this.children) {
            if (guieventlistener.mouseClicked(mouseX, mouseY, mouseButton)) {
                this.setFocused(guieventlistener);
                if (mouseButton == 0) {
                    this.setDragging(true);
                }
                return;
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
        if (b) return;

        // 模拟高版本 charTyped，仅在字符有效时调用
        if (typedChar != 0 && Tools.isAllowedChatCharacter(typedChar)) {
            this.charTyped(typedChar, 0);
        }
    }

    public void onClose() {
        super.onClose();
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public boolean keyPressed(
            int pKeyCode,
            int pScanCode,
            int pModifiers
    ) {
        if (GuiScreen.isKeyComboCtrlV(pKeyCode) && clipboardPasteButton.visible) {
            onClipboardPasteButtonClicked();
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(pKeyCode) && clipboardCopyButton.visible) {
            onClipboardCopyButtonClicked();
            return true;
        } else if (pKeyCode == Keyboard.KEY_E
                && GuiScreen.isCtrlKeyDown()
                && GuiScreen.isShiftKeyDown()
                && examplesButton.visible) {
            onExamplesButtonClicked();
            return true;
        } else if (pKeyCode == Keyboard.KEY_E && GuiScreen.isCtrlKeyDown() && editButton.visible) {
            onEditButtonClicked();
            return true;
        }

        if (pKeyCode == Keyboard.KEY_ESCAPE && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        } else if (this.focused != null && this.focused.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return true;
        }
        return true;
    }

    public ChatFormatting getMillisecondColour(float ms) {
        if (ms <= 5) {
            return ChatFormatting.GREEN;
        } else if (ms <= 15) {
            return ChatFormatting.YELLOW;
        } else {
            return ChatFormatting.RED;
        }
    }

    @Override
    public void drawScreen(
            int mx,
            int my,
            float partialTicks
    ) {
        this.drawDefaultBackground();
        super.drawScreen(mx, my, partialTicks);
        for (Renderable renderable : this.renderables) {
            renderable.render(mx, my, partialTicks);
        }
        this.renderHoveredToolTip(mx, my);

        updateVisibilities();

        // update status countdown
        statusCountdown -= partialTicks;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.clear();     // 清空所有按钮
        this.labelList.clear();
        this.children.clear();
        this.renderables.clear();

        int buttonWidth = 120;
        int buttonHeight = 16;
        clipboardPasteButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 - buttonWidth,
                                (this.height - this.ySize) / 2 + 16
                        )
                        .setSize(buttonWidth, buttonHeight)
                        .setText(MANAGER_GUI_PASTE_FROM_CLIPBOARD_BUTTON)
                        .setOnPress(button -> this.onClipboardPasteButtonClicked())
//                        .setTooltip(
//                                this,
//                                font,
//                                MANAGER_GUI_PASTE_FROM_CLIPBOARD_BUTTON_TOOLTIP
//                        )
                        .build()
        );
        editButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 - buttonWidth,
                                (this.height - this.ySize) / 2 + 16 + 50
                        )
                        .setSize(buttonWidth, buttonHeight)
                        .setText(MANAGER_GUI_EDIT_BUTTON)
                        .setOnPress(button -> onEditButtonClicked())
//                        .setTooltip(this, font, MANAGER_GUI_EDIT_BUTTON_TOOLTIP)
                        .build()
        );
        examplesButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 - buttonWidth,
                                (this.height - this.ySize) / 2 + 16 * 2 + 50
                        )
                        .setSize(buttonWidth, buttonHeight)
                        .setText(MANAGER_GUI_VIEW_EXAMPLES_BUTTON)
                        .setOnPress(button -> onExamplesButtonClicked())
//                        .setTooltip(
//                                this,
//                                font,
//                                MANAGER_GUI_VIEW_EXAMPLES_BUTTON_TOOLTIP
//                        )
                        .build()
        );
//        discordButton = this.addRenderableWidget(
//                new SFMButtonBuilder()
//                        .setPosition(
//                                (this.width - this.xSize) / 2 - buttonWidth,
//                                (this.height - this.ySize) / 2 + 112
//                        )
//                        .setSize(buttonWidth, buttonHeight)
//                        .setText(MANAGER_GUI_DISCORD_BUTTON)
//                        .setOnPress(button -> this.onDiscordButtonClicked())
//                        .build()
//        );
        clipboardCopyButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 - buttonWidth,
                                (this.height - this.ySize) / 2 + 128
                        )
                        .setSize(buttonWidth, buttonHeight)
                        .setText(MANAGER_GUI_COPY_TO_CLIPBOARD_BUTTON)
                        .setOnPress(button -> this.onClipboardCopyButtonClicked())
                        .build()
        );
        logsButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 - buttonWidth,
                                (this.height - this.ySize) / 2 + 16 * 9
                        )
                        .setSize(buttonWidth, buttonHeight)
                        .setText(MANAGER_GUI_VIEW_LOGS_BUTTON)
                        .setOnPress(button -> onLogsButtonClicked())
                        .build()
        );
        rebuildButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 - buttonWidth,
                                (this.height - this.ySize) / 2 + 16 * 10
                        )
                        .setSize(buttonWidth, buttonHeight)
                        .setText(MANAGER_GUI_REBUILD_BUTTON)
                        .setOnPress(button -> this.onRebuildButtonClicked())
                        .build()
        );
        resetButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 + 120,
                                (this.height - this.ySize) / 2 + 10
                        )
                        .setSize(50, 12)
                        .setText(MANAGER_GUI_RESET_BUTTON)
                        .setOnPress(button -> onResetButtonClicked())
//                        .setTooltip(this, font, MANAGER_GUI_RESET_BUTTON_TOOLTIP)
                        .build()
        );
        diagButton = this.addRenderableWidget(
                new SFMButtonBuilder()
                        .setPosition(
                                (this.width - this.xSize) / 2 + 35,
                                (this.height - this.ySize) / 2 + 48
                        )
                        .setSize(12, 14)
                        .setText(new TextComponentString("!"))
                        .setOnPress(button -> onDiagButtonClicked())
                        .setTooltip(this, fontRenderer, isReadOnly()
                                                ? MANAGER_GUI_WARNING_BUTTON_TOOLTIP_READ_ONLY
                                                : MANAGER_GUI_WARNING_BUTTON_TOOLTIP)
                        .build()
        );
        updateVisibilities();
    }

    private void onDiagButtonClicked() {
        if (GuiScreen.isShiftKeyDown() && !isReadOnly()) {
            sendAttemptFix();
        } else {
            this.onSaveDiagnosticsToClipboard();
        }
    }

    private String getProgram() {
         AbstarctCapabilityProvider.readSingleCapabilityFromNBT(menu.getDisk(), SFMDataComponents.PROGRAM_DATA);
         return menu.getDisk().getCapability(SFMDataComponents.PROGRAM_DATA,null).get();
    }

    private void onEditButtonClicked() {
        SFMScreenChangeHelpers.showProgramEditScreen(new ProgramEditScreenOpenContext(
                getProgram(),
                LabelPositionHolder.from(menu.getDisk()),
                this::sendProgram
        ));
    }

    private void onExamplesButtonClicked() {
        SFMScreenChangeHelpers.showExampleListScreen(
                getProgram(),
                LabelPositionHolder.from(menu.getDisk()),
                this::sendProgram
        );
    }

    private void onLogsButtonClicked() {
        SFMScreenChangeHelpers.showLogsScreen(menu);
    }

    private void performReset() {
        SFMPackets.sendToServer(new ServerboundManagerResetPacket(
                menu.windowId,
                menu.MANAGER_POSITION
        ));
        status = MANAGER_GUI_STATUS_RESET.getComponent();
        statusCountdown = STATUS_DURATION;
    }

    private void onResetButtonClicked() {
        if (getProgram().isEmpty() && LabelPositionHolder.from(menu.getDisk()).isEmpty()) {
            performReset();
            return;
        }
        GuiYesNo confirmScreen = new GuiYesNoExtend(
                (result, id) -> {
                    SFMScreenChangeHelpers.popScreen(); // Close confirm screen
                    if (result) {
                        performReset();
                    }
                },
                LocalizationKeys.MANAGER_RESET_CONFIRM_SCREEN_TITLE.getComponent().getFormattedText(),
                LocalizationKeys.MANAGER_RESET_CONFIRM_SCREEN_MESSAGE.getComponent().getFormattedText(),
                LocalizationKeys.MANAGER_RESET_CONFIRM_SCREEN_YES_BUTTON.getComponent().getFormattedText(),
                LocalizationKeys.MANAGER_RESET_CONFIRM_SCREEN_NO_BUTTON.getComponent().getFormattedText(),
                0
        );

        SFMScreenChangeHelpers.setOrPushScreen(confirmScreen);
        confirmScreen.setButtonDelay(20);
    }

    private void onRebuildButtonClicked() {
        SFMPackets.sendToServer(new ServerboundManagerRebuildPacket(
                menu.windowId,
                menu.MANAGER_POSITION
        ));
        status = MANAGER_GUI_STATUS_REBUILD.getComponent();
        statusCountdown = STATUS_DURATION;
    }

    private void onServerConfigButtonClicked() {
        SFMPackets.sendToServer(new ServerboundServerConfigRequestPacket(ConfigCommandBehaviourInput.SHOW));
    }

    private void sendAttemptFix() {
        SFMPackets.sendToServer(new ServerboundManagerFixPacket(
                menu.windowId,
                menu.MANAGER_POSITION
        ));
        status = MANAGER_GUI_STATUS_FIX.getComponent();
        statusCountdown = STATUS_DURATION;
    }

    private void sendProgram(String program) {
        program = Tools.truncate(program, Program.MAX_PROGRAM_LENGTH);
        SFMPackets.sendToServer(new ServerboundManagerProgramPacket(
                menu.windowId,
                menu.MANAGER_POSITION,
                program
        ));
        menu.program = program;
        status = MANAGER_GUI_STATUS_LOADED_CLIPBOARD.getComponent();
        statusCountdown = STATUS_DURATION;

    }

    private void onDiscordButtonClicked() {
//        String discordUrl = "https://discord.gg/xjXYj9MmS4";
//        SFMScreenChangeHelpers.setOrPushScreen(
//                new ConfirmLinkScreen(
//                        proceed -> {
//                            if (proceed) {
//                                Util.getPlatform().openUri(discordUrl);
//                            }
//                            SFMScreenChangeHelpers.popScreen();
//                        },
//                        discordUrl,
//                        false
//                )
//        );
    }

    private void onClipboardCopyButtonClicked() {
        try {
            setClipboardString(menu.program);
            status = MANAGER_GUI_STATUS_SAVED_CLIPBOARD.getComponent();
            statusCountdown = STATUS_DURATION;
        } catch (Throwable t) {
            SFM.LOGGER.error("failed to save clipboard", t);
        }
    }

    private boolean shouldShowDiagButton() {
        ItemStack disk = menu.getDisk();
        if (!(disk.getItem() instanceof DiskItem)) return false;
        List<TextComponentTranslation> errors = DiskItem.getErrors(disk);
        List<TextComponentTranslation> warnings = DiskItem.getWarnings(disk);
        return !errors.isEmpty() || !warnings.isEmpty();
    }

    private void onSaveDiagnosticsToClipboard() {
        try {
            ItemStack disk = menu.getSlot(0).getStack();
            if (!(disk.getItem() instanceof DiskItem)) return;
            String diagnosticInfo = SFMDiagnostics.getDiagnosticsSummary(disk);
            setClipboardString(diagnosticInfo);
            status = MANAGER_GUI_STATUS_SAVED_CLIPBOARD.getComponent();
            statusCountdown = STATUS_DURATION;
        } catch (Throwable t) {
            SFM.LOGGER.error("failed saving clipboard", t);
        }
    }

    private void onClipboardPasteButtonClicked() {
        String clipboardContents;
        try {

            clipboardContents = getClipboardString();
        } catch (Throwable t) {
            SFM.LOGGER.error("failed loading clipboard", t);
            return;
        }
        String existingProgram = getProgram();
        boolean shouldConfirm = !existingProgram.isEmpty() && !existingProgram.equals(clipboardContents);
        if (!shouldConfirm) {
            sendProgram(clipboardContents);
            return;
        }

        GuiYesNo confirmScreen = new GuiYesNoExtend(
                (GuiYesNoCallback) (proceed, id) -> {
                    SFMScreenChangeHelpers.popScreen(); // Close confirm screen
                    if (proceed) {
                        sendProgram(clipboardContents);
                    }
                },
                LocalizationKeys.MANAGER_PASTE_CONFIRM_SCREEN_TITLE.getComponent().getFormattedText(),
                LocalizationKeys.MANAGER_PASTE_CONFIRM_SCREEN_MESSAGE.getComponent().getFormattedText(),
                LocalizationKeys.MANAGER_PASTE_CONFIRM_SCREEN_YES_BUTTON.getComponent().getFormattedText(),
                LocalizationKeys.MANAGER_PASTE_CONFIRM_SCREEN_NO_BUTTON.getComponent().getFormattedText(),
                0
        );
        SFMScreenChangeHelpers.setOrPushScreen(confirmScreen);
        confirmScreen.setButtonDelay(20);
    }

    @MCVersionDependentBehaviour
    private void disableTexture() {
//        RenderSystem.disableTexture(); // 1.19.2
    }

    //renderLabels
    @Override
    protected void drawGuiContainerForegroundLayer(
            int mx,
            int my
    ) {
//        PoseStack poseStack = graphics.pose();        // draw title
        super.drawGuiContainerForegroundLayer(mx, my);
        FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        // draw state string
        ManagerBlockEntity.State state = menu.state;
        SFMFontUtils.draw(
                MANAGER_GUI_STATE.getComponent(state.LOC.getComponent().setStyle(new Style().setColor(state.COLOR)))
                ,
//                titleLabelX,
                8,
                20,
                0,
                false
        );

        if (!menu.logLevel.equals(Level.OFF.name())) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(
                    0,
                    font.FONT_HEIGHT * 1.5f,
                    0f
            );
            GlStateManager.scale(0.5f, 0.5f, 1f);
            font.drawString(
                    menu.logLevel,
                    0,
                    0,
                    0xFFFFFF  // 白色
            );
            GlStateManager.popMatrix();
        }


        // draw status string
        if (statusCountdown > 0) {
            SFMFontUtils.draw(
                    status,
//                    xSize + fontRenderer.getStringWidth(playerInventoryTitle.getString()) + 5,
                    0,
                    ySize,
                    0,
                    false
            );
        }

        // Find the maximum tick time for normalization
        long peakTickTimeNanoseconds = 0;
        for (int i = 0; i < menu.tickTimeNanos.length; i++) {
            peakTickTimeNanoseconds = Long.max(peakTickTimeNanoseconds, menu.tickTimeNanos[i]);
        }
        long yMax = Long.max(peakTickTimeNanoseconds, 50000000); // Start with max at 50ms but allow it to grow

        // Constants for the plot size and position
        final int plotX = 50;
        final int plotY = 40;
        final int spaceBetweenPoints = 6;
        final int plotWidth = spaceBetweenPoints * (menu.tickTimeNanos.length - 1);
        final int plotHeight = 30;


        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        // Draw the plot background
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(plotX, plotY, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.pos(plotX + plotWidth, plotY, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.pos(plotX + plotWidth, plotY + plotHeight, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.pos(plotX, plotY + plotHeight, 0).color(0, 0, 0, 0.5f).endVertex();
        bufferbuilder.pos(plotX, plotY, 0).color(0, 0, 0, 0.5f).endVertex();
        tessellator.draw();

        // Draw lines for each data point
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        int mouseTickTimeIndex = -1;
        for (int i = 0; i < menu.tickTimeNanos.length; i++) {
            long y = menu.tickTimeNanos[i];
            float normalizedTickTime = y == 0 ? 0 : (float) (Math.log10(y) / Math.log10(yMax));
            int plotPosY = plotY + plotHeight - (int) (normalizedTickTime * plotHeight);
            int plotPosX = plotX + spaceBetweenPoints * i;

            ChatFormatting c = getMillisecondColour(y / 1_000_000f);
//            float red = ((c.getColor() >> 16) & 0xFF) / 255f;
//            float green = ((c.getColor() >> 8) & 0xFF) / 255f;
//            float blue = (c.getColor() & 0xFF) / 255f;
            float red = ((1 >> 16) & 0xFF) / 255f;
            float green = ((2 >> 8) & 0xFF) / 255f;
            float blue = (3 & 0xFF) / 255f;

            bufferbuilder.pos(plotPosX, plotPosY, 0).color(red, green, blue, 1f).endVertex();

            if (mx - guiLeft >= plotPosX - spaceBetweenPoints / 2
                    && mx - guiLeft <= plotPosX + spaceBetweenPoints / 2
                    && my - guiTop >= plotY - 2
                    && my - guiTop <= plotY + plotHeight + 2) {
                mouseTickTimeIndex = i;
            }
        }
        tessellator.draw();

        // Draw the tick time text
        DecimalFormat format = new DecimalFormat("0.000");
        if (mouseTickTimeIndex != -1) {
            // 绘制悬停点的tick时间文本
            long hoveredTickTimeNanoseconds = menu.tickTimeNanos[mouseTickTimeIndex];
            float hoveredTickTimeMilliseconds = hoveredTickTimeNanoseconds / 1_000_000f;
            String formattedMillis = format.format(hoveredTickTimeMilliseconds);
            ChatFormatting lagColor = getMillisecondColour(hoveredTickTimeMilliseconds);

            // 修复1：确保正确的渲染状态
            GlStateManager.enableTexture2D(); // 必须启用纹理
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            // 修复2：使用正确的颜色格式
            int textColor = 0xFFFFFF; // 白色
            if (lagColor != null) {
                textColor = Tools.toHex(lagColor); // 使用ChatFormatting的颜色
            }

            fontRenderer.drawStringWithShadow(
                    MANAGER_GUI_HOVERED_TICK_TIME_MS.getComponent(formattedMillis).getFormattedText(),
                    8,
                    20 + fontRenderer.FONT_HEIGHT,
                    textColor
            );

            // 绘制垂直线
            GlStateManager.disableTexture2D(); // 线框渲染需要禁用纹理
            tessellator = Tessellator.getInstance();
            bufferbuilder = tessellator.getBuffer();
            int x = plotX + spaceBetweenPoints * mouseTickTimeIndex;
            bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
            bufferbuilder.pos(x, plotY, 0).color(1f, 1f, 1f, 0.5f).endVertex(); // 半透明
            bufferbuilder.pos(x, plotY + plotHeight, 0).color(1f, 1f, 1f, 0.5f).endVertex();
            tessellator.draw();
            GlStateManager.enableTexture2D(); // 恢复纹理状态
        } else {
            // 绘制峰值tick时间文本
            float peakTickTimeMilliseconds = peakTickTimeNanoseconds / 1_000_000f;
            String formattedMillis = format.format(peakTickTimeMilliseconds);
            ChatFormatting lagColor = getMillisecondColour(peakTickTimeMilliseconds);

            GlStateManager.enableTexture2D();
            GlStateManager.enableBlend();
            int textColor = 0xFFFFFF;
            if (lagColor != null) {
                textColor = Tools.toHex(lagColor);
            }

            fontRenderer.drawStringWithShadow(
                    MANAGER_GUI_PEAK_TICK_TIME_MS.getComponent(formattedMillis).getFormattedText(),
                    8,
                    20 + fontRenderer.FONT_HEIGHT,
                    textColor
            );
        }
        // Restore stuff
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    @MCVersionDependentBehaviour
    private void enableTexture() {
//        RenderSystem.enableTexture(); // 1.19.2
    }

    @MCVersionDependentBehaviour
    public float getBlitOffsetGood() {
        return 0F;
    }

//    @Override
//    protected void renderTooltip(
//            int pX,
//            int pY
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
//        drawChildTooltips(pGuiGraphics, pX, pY);
//        // render hovered item
//        super.renderTooltip(pGuiGraphics, pX, pY);
//    }

//    @SuppressWarnings("unused")
//    @MCVersionDependentBehaviour
//    private void drawChildTooltips(
//            GuiGraphics guiGraphics,
//            int mx,
//            int my
//    ) {
//        // 1.19.2: manually render button tooltips
////        this.renderables
////                .stream()
////                .filter(SFMExtendedButtonWithTooltip.class::isInstance)
////                .map(SFMExtendedButtonWithTooltip.class::cast)
////                .forEach(x -> x.renderToolTip(pose, mx, my));


    //    renderBg
    @Override
    protected void drawGuiContainerBackgroundLayer(
            float partialTicks,
            int mx,
            int my
    ) {
//        if (!menu.logLevel.equals(Level.OFF.name())) {
//            RenderSystem.setShaderColor(0.2f, 0.8f, 1f, 1f);
//        } else {
//            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
//        }
        if (!menu.logLevel.equals(Level.OFF.name())) {
            GlStateManager.color(0.2f, 0.8f, 1f, 1f);
        } else {
            GlStateManager.color(1f, 1f, 1f, 1f);
        }
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        this.mc.getTextureManager().bindTexture(BACKGROUND_TEXTURE_LOCATION);
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
//        graphics.blit(BACKGROUND_TEXTURE_LOCATION, i, j, 0, 0, this.imageWidth, this.imageHeight);
    }
}
