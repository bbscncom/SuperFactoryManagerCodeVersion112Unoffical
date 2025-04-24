package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.LabelPositionHolder;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import my.datafixers.util.Pair;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RoundRobin implements ASTNode {
    private final Behaviour behaviour;
    private int nextIndex = 0;

    public RoundRobin(Behaviour behaviour) {
        this.behaviour = behaviour;
    }

    public static RoundRobin disabled() {
        return new RoundRobin(Behaviour.UNMODIFIED);
    }

    public Behaviour getBehaviour() {
        return behaviour;
    }

    public int next(int length) {
        // this never exists long enough to roll over
        return nextIndex++ % length;
    }

    @Override
    public String toString() {
        switch (behaviour) {
            case UNMODIFIED: return "NOT ROUND ROBIN";
            case BY_BLOCK: return "ROUND ROBIN BY BLOCK";
            case BY_LABEL: return "ROUND ROBIN BY LABEL";
            default: throw new IllegalStateException("Unknown behaviour: " + behaviour);
        }
    }

    public boolean isEnabled() {
        return behaviour != Behaviour.UNMODIFIED;
    }

    public ArrayList<Pair<Label, BlockPos>> getPositionsForLabels(
            List<Label> labels,
            LabelPositionHolder labelPositionHolder
    ) {
        ArrayList<Pair<Label, BlockPos>> positions = new ArrayList<>();
        switch (getBehaviour()) {
            case BY_LABEL:
                int index = next(labels.size());
                Label labe = labels.get(index);
                Set<BlockPos> labelPositions = labelPositionHolder.getPositions(labe.name());
                positions.ensureCapacity(labelPositions.size());
                for (BlockPos pos : labelPositions) {
                    positions.add(Pair.of(labe, pos));
                }
                break;
            case BY_BLOCK:
                List<Pair<Label, BlockPos>> candidates = new ArrayList<>();
                LongOpenHashSet seen = new LongOpenHashSet();
                for (Label label : labels) {
                    for (BlockPos pos : labelPositionHolder.getPositions(label.name())) {
                        if (!seen.add(pos.toLong())) continue;
                        candidates.add(Pair.of(label, pos));
                    }
                }
                if (!candidates.isEmpty()) {
                    positions.add(candidates.get(next(candidates.size())));
                }
                break;
            case UNMODIFIED:
                for (Label label : labels) {
                    labelPositions = labelPositionHolder.getPositions(label.name());
                    positions.ensureCapacity(labelPositions.size());
                    for (BlockPos pos : labelPositions) {
                        positions.add(Pair.of(label, pos));
                    }
                }
                break;
        }
        return positions;
    }

    public enum Behaviour {
        UNMODIFIED,
        BY_BLOCK,
        BY_LABEL
    }
}
