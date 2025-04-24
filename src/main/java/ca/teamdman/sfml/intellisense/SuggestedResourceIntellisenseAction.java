package ca.teamdman.sfml.intellisense;

import ca.teamdman.sfm.common.registry.SFMResourceTypes;
import ca.teamdman.sfm.common.resourcetype.ResourceType;
import ca.teamdman.sfml.ast.ResourceIdentifier;
import ca.teamdman.sfml.manipulation.ManipulationResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import java.util.Objects;

public class SuggestedResourceIntellisenseAction<STACK, ITEM, CAP> implements IntellisenseAction {
    private final ResourceType<STACK, ITEM, CAP> resourceType;
    private final ITEM item;
    private final ITextComponent display;

    public SuggestedResourceIntellisenseAction(
            ResourceType<STACK, ITEM, CAP> resourceType,
            ITEM item
    ) {
        this(
                resourceType,
                item,
                new TextComponentString(
                        new ResourceIdentifier<>(
                                Objects.requireNonNull(SFMResourceTypes.registry().getKey(resourceType)),
                                resourceType.getRegistryKeyForItem(item)
                        ).toStringCondensed()
                )
        );
    }

    public SuggestedResourceIntellisenseAction(
            ResourceType<STACK, ITEM, CAP> resourceType,
            ITEM item,
            ITextComponent display
    ) {
        this.resourceType = resourceType;
        this.item = item;
        this.display = display;
    }

    @Override
    public ITextComponent getComponent() {
        return display;
    }

    @Override
    public ManipulationResult perform(IntellisenseContext context) {
        return context
                .createMutableProgramString()
                .replaceWordAndMoveCursorsToEnd(String.format("%s ",display.getUnformattedText()))
                .intoResult();
    }
}
