package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.LabelPositionHolder;
import my.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class LabelAccess implements ASTNode {
    private final List<Label> labels;
    private final DirectionQualifier directions;
    private final NumberRangeSet slots;
    private final RoundRobin roundRobin;

    public LabelAccess(List<Label> labels, DirectionQualifier directions, NumberRangeSet slots, RoundRobin roundRobin) {
        this.labels = labels;
        this.directions = directions;
        this.slots = slots;
        this.roundRobin = roundRobin;
    }

    public List<Label> labels() {
        return labels;
    }

    public DirectionQualifier directions() {
        return directions;
    }

    public NumberRangeSet slots() {
        return slots;
    }

    public RoundRobin roundRobin() {
        return roundRobin;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(labels.stream().map(Objects::toString).collect(Collectors.joining(", ")));
        if (roundRobin.isEnabled()) {
            builder.append(" ").append(roundRobin);
        }
        if (!directions.directions().isEmpty()) {
            builder.append(" ");
            builder
                    .append(directions
                                    .stream()
                                    .map(DirectionQualifier::directionToString)
                                    .collect(Collectors.joining(", ")))
                    .append(" SIDE");
        }
        if (slots.ranges().length > 0) {
            if (slots.ranges().length != 1 || !slots.ranges()[0].equals(NumberRange.MAX_RANGE)) {
                builder.append(" SLOTS");
                for (NumberRange range : slots.ranges()) {
                    builder.append(" ").append(range);
                }
            }
        }
        return builder.toString();
    }

    public ArrayList<Pair<Label, BlockPos>> getLabelledPositions(LabelPositionHolder labelPositionHolder) {
        return roundRobin().getPositionsForLabels(labels(), labelPositionHolder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabelAccess that = (LabelAccess) o;
        return labels.equals(that.labels) &&
               directions.equals(that.directions) &&
               slots.equals(that.slots) &&
               roundRobin.equals(that.roundRobin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(labels, directions, slots, roundRobin);
    }
}
