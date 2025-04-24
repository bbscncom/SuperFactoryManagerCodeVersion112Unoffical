package ca.teamdman.sfml.ast;

import java.util.Objects;

public class StringHolder implements ASTNode {
    private final String value;

    public StringHolder(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StringHolder that = (StringHolder) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "StringHolder[" +
                "value=" + value +
                ']';
    }
}
