package ca.teamdman.sfml.manipulation;

public class ManipulationResult {
    private final String content;
    private final int cursorPosition;
    private final int selectionCursorPosition;

    public ManipulationResult(String content, int cursorPosition, int selectionCursorPosition) {
        this.content = content;
        this.cursorPosition = cursorPosition;
        this.selectionCursorPosition = selectionCursorPosition;
    }

    public String content() {
        return content;
    }

    public int cursorPosition() {
        return cursorPosition;
    }

    public int selectionCursorPosition() {
        return selectionCursorPosition;
    }
}
