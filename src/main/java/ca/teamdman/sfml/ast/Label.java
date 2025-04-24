package ca.teamdman.sfml.ast;

public class Label implements ASTNode {
    private final String name;

    public Label(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    public static boolean needsQuotes(String label) {
        return !label.matches("[a-zA-Z_][a-zA-Z0-9_]*");
    }

    // 可选：添加equals和hashCode方法
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Label label = (Label) o;
        return name.equals(label.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}

