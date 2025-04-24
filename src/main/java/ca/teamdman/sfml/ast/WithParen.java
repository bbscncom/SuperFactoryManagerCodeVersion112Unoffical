package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.resourcetype.ResourceType;

public class WithParen implements ASTNode, WithClause, ToStringPretty {
    private final WithClause inner;

    public WithParen(WithClause inner) {
        this.inner = inner;
    }

    @Override
    public <STACK> boolean matchesStack(ResourceType<STACK, ?, ?> resourceType, STACK stack) {
        return inner.matchesStack(resourceType, stack);
    }

    @Override
    public String toString() {
        return "(" + inner + ")";
    }
}
