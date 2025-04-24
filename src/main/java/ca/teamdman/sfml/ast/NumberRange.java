package ca.teamdman.sfml.ast;

import java.util.Objects;

public class NumberRange implements ASTNode {
    public static final NumberRange MAX_RANGE = new NumberRange(Long.MIN_VALUE, Long.MAX_VALUE);
    private final long start;
    private final long end;

    public NumberRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long start() {
        return start;
    }

    public long end() {
        return end;
    }

    public boolean contains(int value) {
        return value >= start && value <= end;
    }

    @Override
    public String toString() {
        if (start == end) return String.valueOf(start);
        return start + "-" + end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberRange)) return false;
        NumberRange that = (NumberRange) o;
        return start == that.start && end == that.end;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
