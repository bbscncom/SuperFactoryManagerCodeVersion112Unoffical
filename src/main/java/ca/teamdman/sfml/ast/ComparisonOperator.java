package ca.teamdman.sfml.ast;

import java.util.Locale;
import java.util.function.BiPredicate;

public enum ComparisonOperator implements ASTNode, BiPredicate<Long, Long>, ToStringPretty {
    GREATER((a, b) -> a > b),
    LESSER((a, b) -> a < b),
    EQUALS(Long::equals),
    LESSER_OR_EQUAL((a, b) -> a <= b),
    GREATER_OR_EQUAL((a, b) -> a >= b);

    private final BiPredicate<Long, Long> PRED;

    ComparisonOperator(BiPredicate<Long, Long> pred) {
        this.PRED = pred;
    }

    public static ComparisonOperator from(String text) {
        String upper = text.toUpperCase(Locale.ROOT);
        if (upper.equals("GT") || upper.equals(">")) return GREATER;
        if (upper.equals("LT") || upper.equals("<")) return LESSER;
        if (upper.equals("EQ") || upper.equals("=")) return EQUALS;
        if (upper.equals("LE") || upper.equals("<=")) return LESSER_OR_EQUAL;
        if (upper.equals("GE") || upper.equals(">=")) return GREATER_OR_EQUAL;
        throw new IllegalArgumentException("Invalid comparison operator: " + text);
    }

    @Override
    public String toString() {
        switch (this) {
            case GREATER: return ">";
            case LESSER: return "<";
            case EQUALS: return "=";
            case LESSER_OR_EQUAL: return "<=";
            case GREATER_OR_EQUAL: return ">=";
            default: throw new IllegalStateException("Unexpected value: " + this);
        }
    }

    @Override
    public boolean test(Long a, Long b) {
        return PRED.test(a, b);
    }
}
