package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.ProgramContext;

import java.util.Objects;

public class Interval implements ASTNode {
    private final int ticks;
    private final IntervalAlignment alignment;
    private final int offset;

    public Interval(int ticks, IntervalAlignment alignment, int offset) {
        this.ticks = ticks;
        this.alignment = alignment;
        this.offset = offset;
    }

    public boolean shouldTick(ProgramContext context) {
        switch (alignment) {
            case LOCAL:
                return context.getManager().getTick() % ticks == offset;
            case GLOBAL:
                return Objects.requireNonNull(context.getManager().getWorld()).getWorldTime() % ticks == offset;
            default:
                throw new IllegalStateException("Unknown alignment: " + alignment);
        }
    }

    @Override
    public String toString() {
        return ticks + " TICKS";
    }

    public enum IntervalAlignment {
        LOCAL,
        GLOBAL
    }

    // Getters
    public int ticks() { return ticks; }
    public IntervalAlignment alignment() { return alignment; }
    public int offset() { return offset; }
}
