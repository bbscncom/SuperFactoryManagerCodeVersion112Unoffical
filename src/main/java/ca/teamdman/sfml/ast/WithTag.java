package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.resourcetype.ResourceType;

public class WithTag implements ASTNode, WithClause, ToStringPretty {
    private final TagMatcher tagMatcher;

    public WithTag(TagMatcher tagMatcher) {
        this.tagMatcher = tagMatcher;
    }

    @Override
    public <STACK> boolean matchesStack(ResourceType<STACK, ?, ?> resourceType, STACK stack) {
        return resourceType.getTagsForStack(stack).anyMatch(tagMatcher::testResourceLocation);
    }

    @Override
    public String toString() {
        return "TAG " + tagMatcher;
    }
}
