package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;
import ca.teamdman.sfm.common.program.SimulateExploreAllPathsProgramBehaviour;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static ca.teamdman.sfm.common.localization.LocalizationKeys.LOG_PROGRAM_TICK_FORGET_STATEMENT;

public class ForgetStatement implements Statement {
    private final Set<Label> labelToForget;

    public ForgetStatement(Set<Label> labelToForget) {
        this.labelToForget = labelToForget;
    }

    public Set<Label> labelToForget() {
        return labelToForget;
    }

    @Override
    public void tick(ProgramContext context) {
        List<InputStatement> newInputs = new ArrayList<>();
        for (InputStatement oldInputStatement : context.getInputs()) {
            List<Label> newLabels = oldInputStatement.labelAccess().labels().stream()
                    .filter(label -> !this.labelToForget.contains(label))
                    .collect(Collectors.toList());

            // always fire event from old to new, even if new has no labels
            InputStatement newInputStatement = new InputStatement(
                    new LabelAccess(
                            newLabels,
                            oldInputStatement.labelAccess().directions(),
                            oldInputStatement.labelAccess().slots(),
                            oldInputStatement.labelAccess().roundRobin()
                    ),
                    oldInputStatement.resourceLimits(),
                    oldInputStatement.each()
            );
            context.getProgram().astBuilder.setLocationFromOtherNode(newInputStatement, oldInputStatement);
            if (context.getBehaviour() instanceof SimulateExploreAllPathsProgramBehaviour) {
                SimulateExploreAllPathsProgramBehaviour simulation = (SimulateExploreAllPathsProgramBehaviour) context.getBehaviour();
                simulation.onInputStatementForgetTransform(context, oldInputStatement, newInputStatement);
            }
            // this could be a set instead of list contains check, but whatever. Should be small
            oldInputStatement.freeSlotsIf(slot -> labelToForget.contains(slot.label));
            oldInputStatement.transferSlotsTo(newInputStatement);

            if (newLabels.isEmpty()) {
                oldInputStatement.freeSlots();
            } else {
                newInputs.add(newInputStatement);
            }
        }
        context.getInputs().clear();
        context.getInputs().addAll(newInputs);
        context.getLogger().debug(x -> x.accept(LOG_PROGRAM_TICK_FORGET_STATEMENT.get(
                labelToForget.stream().map(Objects::toString).collect(Collectors.joining(", "))
        )));
    }

    @Override
    public String toString() {
        return "FORGET " + labelToForget.stream().map(Objects::toString).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForgetStatement)) return false;
        ForgetStatement that = (ForgetStatement) o;
        return Objects.equals(labelToForget, that.labelToForget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labelToForget);
    }
}
