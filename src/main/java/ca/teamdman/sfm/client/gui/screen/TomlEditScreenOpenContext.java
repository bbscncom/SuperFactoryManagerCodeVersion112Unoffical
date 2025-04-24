package ca.teamdman.sfm.client.gui.screen;

import java.util.function.Consumer;

public class TomlEditScreenOpenContext {
    private final String textContents;
    private final Consumer<String> saveCallback;

    public TomlEditScreenOpenContext(String textContents, Consumer<String> saveCallback) {
        this.textContents = textContents;
        this.saveCallback = saveCallback;
    }

    public String textContents() {
        return textContents;
    }

    public Consumer<String> saveCallback() {
        return saveCallback;
    }
}
