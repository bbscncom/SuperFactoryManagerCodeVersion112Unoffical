package ca.teamdman.sfm.client.gui.screen;

import ca.teamdman.sfm.common.program.LabelPositionHolder;

import java.util.Map;
import java.util.function.Consumer;

public class ExampleEditScreenOpenContext {
    private final String exampleProgramString;
    private final String diskProgramString;
    private final Map<String, String> templates;
    private final LabelPositionHolder labelPositionHolder;
    private final Consumer<String> saveCallback;

    public ExampleEditScreenOpenContext(
            String exampleProgramString,
            String diskProgramString,
            Map<String, String> templates,
            LabelPositionHolder labelPositionHolder,
            Consumer<String> saveCallback
    ) {
        this.exampleProgramString = exampleProgramString;
        this.diskProgramString = diskProgramString;
        this.templates = templates;
        this.labelPositionHolder = labelPositionHolder;
        this.saveCallback = saveCallback;
    }

    public String exampleProgramString() {
        return exampleProgramString;
    }

    public String diskProgramString() {
        return diskProgramString;
    }

    public Map<String, String> templates() {
        return templates;
    }

    public LabelPositionHolder labelPositionHolder() {
        return labelPositionHolder;
    }

    public Consumer<String> saveCallback() {
        return saveCallback;
    }

    public boolean equalsAnyTemplate(String content) {
        return templates().values().stream().anyMatch(content::equals);
    }

    /**
     * Check if it is safe to overwrite the disk with a new program.
     * If the disk is empty, it is safe to overwrite.
     * If the disk contains a template, it is safe to overwrite.
     * @return true if it is safe to overwrite the disk, false otherwise
     */
    public boolean isSafeToOverwriteDisk() {
        if (diskProgramString().isEmpty()) return true;
        return equalsAnyTemplate(this.diskProgramString());
    }
}
