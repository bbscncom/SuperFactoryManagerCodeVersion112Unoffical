package ca.teamdman.sfml.ast;

public class Number implements ASTNode {
    private final long value;

    public Number(long value) {
        this.value = value;
    }

    public long value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public Number add(Number number) {
        return new Number(value + number.value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Number)) return false;
        Number number = (Number) o;
        return value == number.value;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value);
    }
}
