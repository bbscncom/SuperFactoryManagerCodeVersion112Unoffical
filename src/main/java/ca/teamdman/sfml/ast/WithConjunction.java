package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.resourcetype.ResourceType;

public class WithConjunction implements ASTNode, WithClause, ToStringPretty {
    private final WithClause left;
    private final WithClause right;

    public WithConjunction(WithClause left, WithClause right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public <STACK> boolean matchesStack(ResourceType<STACK, ?, ?> resourceType, STACK stack) {
        return left.matchesStack(resourceType, stack) && right.matchesStack(resourceType, stack);
    }

    @Override
    public String toString() {
        return left + " AND " + right;
    }
}
