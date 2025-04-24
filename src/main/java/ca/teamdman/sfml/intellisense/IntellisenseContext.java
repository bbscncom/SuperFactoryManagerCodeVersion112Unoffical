package ca.teamdman.sfml.intellisense;

import ca.teamdman.sfm.common.config.SFMClientProgramEditorConfig;
import ca.teamdman.sfm.common.program.LabelPositionHolder;
import ca.teamdman.sfml.program_builder.ProgramBuildResult;

public class IntellisenseContext {
    private final ProgramBuildResult programBuildResult;
    private final int cursorPosition;
    private final int selectionCursorPosition;
    private final LabelPositionHolder labelPositionHolder;
    private final SFMClientProgramEditorConfig.IntellisenseLevel intellisenseLevel;

    public IntellisenseContext(
            ProgramBuildResult programBuildResult,
            int cursorPosition,
            int selectionCursorPosition,
            LabelPositionHolder labelPositionHolder,
            SFMClientProgramEditorConfig.IntellisenseLevel intellisenseLevel
    ) {
        this.programBuildResult = programBuildResult;
        this.cursorPosition = cursorPosition;
        this.selectionCursorPosition = selectionCursorPosition;
        this.labelPositionHolder = labelPositionHolder;
        this.intellisenseLevel = intellisenseLevel;
    }

    public MutableProgramString createMutableProgramString() {
        return new MutableProgramString(
                programBuildResult.metadata.programString,
                cursorPosition,
                selectionCursorPosition
        );
    }

    // Getters
    public ProgramBuildResult getProgramBuildResult() {
        return programBuildResult;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public int getSelectionCursorPosition() {
        return selectionCursorPosition;
    }

    public LabelPositionHolder getLabelPositionHolder() {
        return labelPositionHolder;
    }

    public SFMClientProgramEditorConfig.IntellisenseLevel getIntellisenseLevel() {
        return intellisenseLevel;
    }
}
