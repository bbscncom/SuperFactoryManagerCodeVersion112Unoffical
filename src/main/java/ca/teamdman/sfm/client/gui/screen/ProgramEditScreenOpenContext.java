package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.common.program.LabelPositionHolder;

import java.util.function.Consumer;

public class ProgramEditScreenOpenContext {
    private final String programString;
    private final LabelPositionHolder labelPositionHolder;
    private final Consumer<String> saveCallback;

    public ProgramEditScreenOpenContext(String programString, LabelPositionHolder labelPositionHolder, Consumer<String> saveCallback) {
        this.programString = programString;
        this.labelPositionHolder = labelPositionHolder;
        this.saveCallback = saveCallback;
    }

    public String getProgramString() {
        return programString;
    }

    public LabelPositionHolder getLabelPositionHolder() {
        return labelPositionHolder;
    }

    public Consumer<String> getSaveCallback() {
        return saveCallback;
    }
}
