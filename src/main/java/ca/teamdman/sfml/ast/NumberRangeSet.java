package ca.teamdman.sfml.ast;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NumberRangeSet implements ASTNode {
    public static final NumberRangeSet MAX_RANGE = new NumberRangeSet(new NumberRange[]{NumberRange.MAX_RANGE});
    private final NumberRange[] ranges;

    public NumberRangeSet(NumberRange[] ranges) {
        this.ranges = ranges;
    }

    public NumberRange[] ranges() {
        return ranges;
    }

    public boolean contains(int value) {
        for (NumberRange range : ranges) {
            if (range.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[" + (this.equals(MAX_RANGE) ? "ALL" : Arrays.stream(ranges).map(NumberRange::toString).collect(Collectors.joining(","))) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NumberRangeSet)) return false;
        NumberRangeSet that = (NumberRangeSet) o;
        return Arrays.equals(ranges, that.ranges);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ranges);
    }
}
