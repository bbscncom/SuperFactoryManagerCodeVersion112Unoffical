package ca.teamdman.sfm.common.util;

import ca.teamdman.sfm.common.program.LimitedInputSlot;
import ca.teamdman.sfm.common.registry.SFMResourceTypes;
import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfml.ast.Number;
import ca.teamdman.sfml.ast.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

public class SFMASTUtils {
    public static <STACK, ITEM, CAP> Optional<InputStatement> getInputStatementForSlot(
            LimitedInputSlot<STACK, ITEM, CAP> slot,
            LabelAccess labelAccess
    ) {
        STACK potential = slot.peekExtractPotential();
        ResourceType<STACK, ITEM, CAP> resourceType = slot.type;
        if (resourceType.isEmpty(potential)) return Optional.empty();
        long toMove = resourceType.getAmount(potential);
        toMove = Long.min(toMove, slot.tracker.getResourceLimit().limit().quantity().number().value());
        long remainingObligation = slot.tracker.getRemainingRetentionObligation(resourceType, potential);
        toMove -= Long.min(toMove, remainingObligation);
        potential = resourceType.withCount(potential, toMove);
        STACK stack = potential;

        return Optional.of(SFMResourceTypes.registry().getKey(resourceType))
                .map(key -> getInputStatementForStack(
                        key,
                        resourceType,
                        stack,
                        "temp",
                        slot.slot,
                        false,
                        null
                ))
                // update the labels
                .map(inputStatement -> new InputStatement(new LabelAccess(
                        labelAccess.labels(),
                        labelAccess.directions(),
                        inputStatement.labelAccess()
                                .slots(),
                        RoundRobin.disabled()
                ), inputStatement.resourceLimits(), inputStatement.each()));
    }

    public static <STACK, ITEM, CAP> InputStatement getInputStatementForStack(
            ResourceLocation key,
            ResourceType<STACK, ITEM, CAP> resourceType,
            STACK stack,
            String label,
            int slot,
            boolean each,
            @Nullable EnumFacing direction
    ) {
        LabelAccess labelAccess = new LabelAccess(
               Collections.singletonList(new Label(label)),
                new DirectionQualifier(
                        direction == null
                        ? EnumSet.noneOf(EnumFacing.class)
                        : EnumSet.of(direction)),
                new NumberRangeSet(
                        new NumberRange[]{new NumberRange(slot, slot)}
                ),
                RoundRobin.disabled()
        );
        Limit limit = new Limit(
                new ResourceQuantity(
                        new ca.teamdman.sfml.ast.Number(resourceType.getAmount(stack)),
                        ResourceQuantity.IdExpansionBehaviour.NO_EXPAND
                ),
                new ResourceQuantity(
                        new Number(0),
                        ResourceQuantity.IdExpansionBehaviour.NO_EXPAND
                )
        );
        ResourceLocation stackId = resourceType.getRegistryKeyForStack(stack);
        ResourceIdentifier<STACK, ITEM, CAP> resourceIdentifier = new ResourceIdentifier<>(
                key,
                stackId
        );
        ResourceLimit resourceLimit = new ResourceLimit(
                new ResourceIdSet(Collections.singletonList(resourceIdentifier)),
                limit,
                With.ALWAYS_TRUE
        );
        ResourceLimits resourceLimits = new ResourceLimits(
                Collections.singletonList(resourceLimit),
                ResourceIdSet.EMPTY
        );

        // todo: add WITH logic here to also build code to match any item/block tags present
        return new InputStatement(
                labelAccess,
                resourceLimits,
                each
        );
    }
}
