package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public class BoolDisjunction implements BoolExpr {
    private final BoolExpr left;
    private final BoolExpr right;

    public BoolDisjunction(BoolExpr left, BoolExpr right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public boolean test(ProgramContext programContext) {
        return left.test(programContext) || right.test(programContext);
    }

    @Override
    public String toString() {
        return left + " OR " + right;
    }
}
