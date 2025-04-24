package ca.teamdman.sfml.ast;


public class ProgramName implements ASTNode {
    private final StringHolder value;

    public ProgramName(StringHolder value) {
        this.value = value;
    }

    public StringHolder getValue() {
        return value;
    }
}
