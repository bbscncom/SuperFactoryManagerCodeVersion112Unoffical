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
import java.util.ResourceBundle;

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
        for (Renderable renderable : this.renderables) {
            renderable.render(pMouseX,pMouseY,pPartialTick);
        }

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
                        .setOnPress((button) -> this.onDone())
                        .build());

        updateButtonStates();
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
