package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public class BoolNegation implements BoolExpr {
    private final BoolExpr inner;

    public BoolNegation(BoolExpr inner) {
        this.inner = inner;
    }

    @Override
    public boolean test(ProgramContext programContext) {
        return !inner.test(programContext);
    }

    @Override
    public String toString() {
        return "NOT " + inner;
    }

    // Getter
    public BoolExpr inner() { return inner; }
}
