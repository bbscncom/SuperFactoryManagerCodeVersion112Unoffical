package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.blockentity.ManagerBlockEntity;
import ca.teamdman.sfm.common.program.ProgramContext;
import net.minecraft.world.World;

public class BoolRedstone implements BoolExpr {
    private final ComparisonOperator operator;
    private final long number;

    public BoolRedstone(ComparisonOperator operator, long number) {
        this.operator = operator;
        this.number = number;
    }

    @Override
    public boolean test(ProgramContext programContext) {
        ManagerBlockEntity manager = programContext.getManager();
        World world = manager.getWorld();
        assert world != null;
        // 1.12 version uses getStrongPower instead of getBestNeighborSignal
        long lhs = world.getStrongPower(manager.getPos());
        long rhs = number;
        return operator.test(lhs, rhs);
    }

    @Override
    public String toString() {
        return "REDSTONE " + operator + " " + number;
    }
}
