package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.localization.LocalizationKeys;
import ca.teamdman.sfm.common.program.ProgramContext;

import java.util.List;

public class Block implements Statement {
    private final List<Statement> statements;

    public Block(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public void tick(ProgramContext context) {
        for (Statement statement : statements) {
            long start = System.nanoTime();
            statement.tick(context);
            float elapsed = (System.nanoTime() - start) / 1_000_000f;
            if (statement instanceof ToStringPretty) {
                context.getLogger().info(x -> x.accept(LocalizationKeys.PROGRAM_TICK_STATEMENT_TIME_MS.get(
                        elapsed,
                        ((ToStringPretty) statement).toStringPretty()
                )));
            } else {
                context.getLogger().info(x -> x.accept(LocalizationKeys.PROGRAM_TICK_STATEMENT_TIME_MS.get(
                        elapsed,
                        statement.toString()
                )));
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder rtn = new StringBuilder();
        for (Statement statement : statements) {
            if (statement instanceof InputStatement) {
                InputStatement ins = (InputStatement) statement;
                rtn.append(ins.toStringPretty().trim());
            } else if (statement instanceof OutputStatement) {
                OutputStatement outs = (OutputStatement) statement;
                rtn.append(outs.toStringPretty().trim());
            } else {
                rtn.append(statement.toString().trim());
            }
            rtn.append("\n");
        }
        return rtn.toString().trim();
    }

    @Override
    public List<Statement> getStatements() {
        return statements;
    }
}
