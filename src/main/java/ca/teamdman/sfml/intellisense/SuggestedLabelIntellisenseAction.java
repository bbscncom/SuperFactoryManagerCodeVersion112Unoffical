package ca.teamdman.sfml.intellisense;

import ca.teamdman.sfml.ast.Label;
import ca.teamdman.sfml.manipulation.ManipulationResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class SuggestedLabelIntellisenseAction implements IntellisenseAction {
    private final String label;
    private final int numBlocks;

    public SuggestedLabelIntellisenseAction(String label, int numBlocks) {
        this.label = label;
        this.numBlocks = numBlocks;
    }

    @Override
    public ITextComponent getComponent() {
        return new TextComponentString(String.format("%s (%d)", label, numBlocks));
    }

    @Override
    public ManipulationResult perform(IntellisenseContext context) {
        if (Label.needsQuotes(label)) {
            return context.createMutableProgramString()
                    .replaceWordAndMoveCursorsToEnd(String.format("\"%s\" ", label))
                    .intoResult();
        } else {
            return context.createMutableProgramString()
                    .replaceWordAndMoveCursorsToEnd(String.format("%s ", label))
                    .intoResult();
        }
    }
}
