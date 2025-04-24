package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.resourcetype.ResourceType;

import java.util.Objects;

public class With implements WithClause, ToStringPretty {
    public static final With ALWAYS_TRUE = new With(
            new WithAlwaysTrue(),
            WithMode.WITH
    );

    private final WithClause condition;
    private final WithMode mode;

    public With(WithClause condition, WithMode mode) {
        this.condition = condition;
        this.mode = mode;
    }

    public WithClause condition() {
        return condition;
    }

    public WithMode mode() {
        return mode;
    }

    @Override
    public <STACK> boolean matchesStack(
            ResourceType<STACK, ?, ?> resourceType,
            STACK stack
    ) {
        boolean matches = condition.matchesStack(resourceType, stack);
        switch (mode) {
            case WITH: return matches;
            case WITHOUT: return !matches;
            default: throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    @Override
    public String toString() {
        switch (mode) {
            case WITH: return "WITH " + condition.toStringPretty();
            case WITHOUT: return "WITHOUT " + condition.toStringPretty();
            default: throw new IllegalStateException("Unknown mode: " + mode);
        }
    }

    public enum WithMode {
        WITH,
        WITHOUT
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof With)) return false;
        With with = (With) o;
        return Objects.equals(condition, with.condition) &&
               mode == with.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, mode);
    }
}
