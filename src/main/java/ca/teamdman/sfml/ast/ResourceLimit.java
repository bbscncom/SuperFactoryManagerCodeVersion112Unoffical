package ca.teamdman.sfml.ast;

import ca.teamdman.sfm.common.program.*;
import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfml.ast.ResourceQuantity.IdExpansionBehaviour;

import java.util.Objects;

public class ResourceLimit implements ASTNode {
    public static final ResourceLimit TAKE_ALL_LEAVE_NONE = new ResourceLimit(
            ResourceIdSet.MATCH_ALL,
            Limit.MAX_QUANTITY_NO_RETENTION,
            With.ALWAYS_TRUE
    );
    public static final ResourceLimit ACCEPT_ALL_WITHOUT_RESTRAINT = new ResourceLimit(
            ResourceIdSet.MATCH_ALL,
            Limit.MAX_QUANTITY_MAX_RETENTION,
            With.ALWAYS_TRUE
    );

    private final ResourceIdSet resourceIds;
    private final Limit limit;
    private final With with;

    public ResourceLimit(ResourceIdSet resourceIds, Limit limit, With with) {
        this.resourceIds = resourceIds;
        this.limit = limit;
        this.with = with;
    }

    public ResourceIdSet resourceIds() {
        return resourceIds;
    }

    public Limit limit() {
        return limit;
    }

    public With with() {
        return with;
    }

    public ResourceLimit withDefaultLimit(Limit defaults) {
        return new ResourceLimit(resourceIds, limit.withDefaults(defaults), with);
    }

    public ResourceLimit withLimit(Limit limit) {
        return new ResourceLimit(resourceIds, limit, with);
    }

    public IInputResourceTracker createInputTracker(ResourceIdSet exclusions) {
        if (limit.quantity().idExpansionBehaviour() == IdExpansionBehaviour.EXPAND) {
            if (limit.retention().idExpansionBehaviour() == IdExpansionBehaviour.EXPAND) {
                return new ExpandedQuantityExpandedRetentionInputResourceTracker(this, exclusions);
            } else {
                return new ExpandedQuantitySharedRetentionInputResourceTracker(this, exclusions);
            }
        } else {
            if (limit.retention().idExpansionBehaviour() == IdExpansionBehaviour.EXPAND) {
                return new SharedQuantityExpandedRetentionInputResourceTracker(this, exclusions);
            } else {
                return new SharedQuantitySharedRetentionInputResourceTracker(this, exclusions);
            }
        }
    }

    public IOutputResourceTracker createOutputTracker(ResourceIdSet exclusions) {
        if (limit.quantity().idExpansionBehaviour() == IdExpansionBehaviour.EXPAND) {
            if (limit.retention().idExpansionBehaviour() == IdExpansionBehaviour.EXPAND) {
                return new ExpandedQuantityExpandedRetentionOutputResourceTracker(this, exclusions);
            } else {
                return new ExpandedQuantitySharedRetentionOutputResourceTracker(this, exclusions);
            }
        } else {
            if (limit.retention().idExpansionBehaviour() == IdExpansionBehaviour.EXPAND) {
                return new SharedQuantityExpandedRetentionOutputResourceTracker(this, exclusions);
            } else {
                return new SharedQuantitySharedRetentionOutputResourceTracker(this, exclusions);
            }
        }
    }

    public boolean matchesStack(Object stack) {
        ResourceIdentifier<?, ?, ?> matchingIdPattern = resourceIds.getMatchingFromStack(stack);
        if (matchingIdPattern == null) {
            return false;
        }
        @SuppressWarnings("unchecked")
        ResourceType<Object, ?, ?> resourceType = (ResourceType<Object, ?, ?>) matchingIdPattern.getResourceType();
        if (resourceType == null) {
            return false;
        }
        return with.matchesStack(resourceType, stack);
    }

    @Override
    public String toString() {
        return limit + " " + resourceIds + (with == With.ALWAYS_TRUE ? "" : " WITH " + with);
    }

    public String toStringCondensed(Limit defaults) {
        return (
                limit.toStringCondensed(defaults) + " " + resourceIds.toStringCondensed() + (
                        with == With.ALWAYS_TRUE
                        ? ""
                        : " WITH " + with
                )
        ).trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResourceLimit)) return false;
        ResourceLimit that = (ResourceLimit) o;
        return Objects.equals(resourceIds, that.resourceIds) &&
               Objects.equals(limit, that.limit) &&
               Objects.equals(with, that.with);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceIds, limit, with);
    }
}
