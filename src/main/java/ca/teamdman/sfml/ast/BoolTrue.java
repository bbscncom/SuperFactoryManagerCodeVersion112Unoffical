package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

public class BoolTrue implements BoolExpr {
    public BoolTrue() {}

    @Override
    public boolean test(ProgramContext programContext) {
        return true;
    }

    @Override
    public String toString() {
        return "TRUE";
    }
}
