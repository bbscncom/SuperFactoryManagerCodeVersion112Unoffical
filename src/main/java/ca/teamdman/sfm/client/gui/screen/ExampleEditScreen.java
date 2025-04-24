package ca.teamdman.sfm.client.gui.screen;


import net.minecraft.client.gui.GuiYesNo;

public class ExampleEditScreen extends ProgramEditorScreen {
    private final ExampleEditScreenOpenContext openContext;

    public ExampleEditScreen(
            ExampleEditScreenOpenContext openContext
    ) {
        super(new ProgramEditScreenOpenContext(
                openContext.exampleProgramString(),
                openContext.labelPositionHolder(),
                openContext.saveCallback()
        ));
        this.openContext = openContext;
    }

    @Override
    public void saveAndClose() {
        if (openContext.isSafeToOverwriteDisk()) {
            super.saveAndClose();
        } else {
            // The disk contains non-template code, ask before overwriting
            GuiYesNo saveConfirmScreen = getSaveConfirmScreen(super::saveAndClose);
            SFMScreenChangeHelpers.setOrPushScreen(saveConfirmScreen);
            saveConfirmScreen.setButtonDelay(20);
        }
    }

    @Override
    public void onClose() {
        // The user has requested to close the screen
        // If the content has changed, ask to save before discarding
        if (!openContext.equalsAnyTemplate(textarea.getValue())) {
            GuiYesNo exitWithoutSavingConfirmScreen = getExitWithoutSavingConfirmScreen();
            SFMScreenChangeHelpers.setOrPushScreen(exitWithoutSavingConfirmScreen);
            exitWithoutSavingConfirmScreen.setButtonDelay(20);
        } else {
            super.onClose();
        }
    }
}
