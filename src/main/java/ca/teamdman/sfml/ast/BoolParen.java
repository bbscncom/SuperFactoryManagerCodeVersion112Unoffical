package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public class BoolParen implements BoolExpr {
    private final BoolExpr inner;

    public BoolParen(BoolExpr inner) {
        this.inner = inner;
    }

    @Override
    public boolean test(ProgramContext programContext) {
        return inner.test(programContext);
    }

    @Override
    public String toString() {
        return "(" + inner + ")";
    }
}
