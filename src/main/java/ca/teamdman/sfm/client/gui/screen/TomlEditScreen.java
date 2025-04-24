package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.common.program.LabelPositionHolder;

public class TomlEditScreen extends ProgramEditorScreen {
    public TomlEditScreen(
            TomlEditScreenOpenContext openContext
    ) {
        super(new ProgramEditScreenOpenContext(
                openContext.textContents(),
                LabelPositionHolder.empty(),
                openContext.saveCallback()
        ));
    }
}
