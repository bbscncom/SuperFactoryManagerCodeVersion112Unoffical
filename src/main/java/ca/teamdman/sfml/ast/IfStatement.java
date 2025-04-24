package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.SFM;
import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.SimulateExploreAllPathsProgramBehaviour;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class IfStatement implements ASTNode, Statement, ToStringCondensed {
    public final BoolExpr condition;
    public final Block trueBlock;
    public final Block falseBlock;

    public IfStatement(BoolExpr condition, Block trueBlock, Block falseBlock) {
        this.condition = condition;
        this.trueBlock = trueBlock;
        this.falseBlock = falseBlock;
    }

    @Override
    public void tick(ProgramContext context) {
        Predicate<ProgramContext> condition = this.condition;
        boolean test;
        if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour) {
            SimulateExploreAllPathsProgramBehaviour simulation = (SimulateExploreAllPathsProgramBehaviour) context.getBehaviour();
            condition = ctx -> {
                int conditionIndex = ctx.getProgram().getConditionIndex(this);
                if (conditionIndex == -1) {
                    SFM.LOGGER.warn("Condition index not found for {}", this);
                }
                return simulation.getTriggerPathCount().testBit(conditionIndex);
            };
            test = condition.test(context);
            simulation.pushPathElement(new SimulateExploreAllPathsProgramBehaviour.Branch(this, test));
        } else {
            test = condition.test(context);
        }

        if (test) {
            tickTrueBlock(context);
        } else {
            tickFalseBlock(context);
        }
    }

    @Override
    public String toString() {
        String rtn = "IF " + condition + " THEN\n" + trueBlock.toString().trim();
        if (!falseBlock.getStatements().isEmpty()) {
            rtn += "\nELSE\n" + falseBlock.toString().trim();
        }
        rtn += "\nEND";
        return rtn.trim();
    }

    @Override
    public List<Statement> getStatements() {
        return Arrays.asList(trueBlock, falseBlock);
    }

    @Override
    public String toStringCondensed() {
        return condition.toString();
    }

    private void tickFalseBlock(ProgramContext context) {
        context.getLogger().debug(x -> x.accept(
                LocalizationKeys.LOG_PROGRAM_TICK_IF_STATEMENT_WAS_FALSE.get(this.condition.toStringPretty())));
        falseBlock.tick(context);
    }

    private void tickTrueBlock(ProgramContext context) {
        context.getLogger().debug(x -> x.accept(
                LocalizationKeys.LOG_PROGRAM_TICK_IF_STATEMENT_WAS_TRUE.get(this.condition.toStringPretty())));
        trueBlock.tick(context);
    }
}
