package ca.teamdman.sfml.intellisense;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.sfml.manipulation.ManipulationResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.antlr.v4.runtime.Vocabulary;

import java.util.List;

public class SuggestedTokensIntellisenseAction implements IntellisenseAction {
    private final Integer nextTokenType;
    private final List<Integer> followingTokenTypes;
    private final Vocabulary vocabulary;

    public SuggestedTokensIntellisenseAction(
            Integer nextTokenType,
            List<Integer> followingTokenTypes,
            Vocabulary vocabulary
    ) {
        this.nextTokenType = nextTokenType;
        this.followingTokenTypes = followingTokenTypes;
        this.vocabulary = vocabulary;
    }

    @Override
    public ITextComponent getComponent() {
        return new TextComponentString(getDisplay());
    }

    @Override
    public ManipulationResult perform(IntellisenseContext context) {
        MutableProgramString programStringMut = context.createMutableProgramString();
        String word = programStringMut.getWord();
        int cursorInWord = programStringMut.cursorInWord();
        switch (nextTokenType) {
            case SFMLLexer.NAME:
                programStringMut.replaceWordAndMoveCursorsToEnd(vocabulary.getSymbolicName(nextTokenType) + " \"\"");
                programStringMut.offsetCursors(-1);

            case SFMLLexer.STRING:
                if (word.equals("\"\"")) {
                    if (cursorInWord != 2) {
                        programStringMut.offsetCursors(1);
                    } else {
                        programStringMut.insertTextWithoutMovingCursors("\n");
                        programStringMut.offsetCursors(1);
                    }
                } else if (word.isEmpty()) {
                    programStringMut.replaceWordAndMoveCursorsToEnd("\"\"");
                    programStringMut.offsetCursors(-1);
                } else if (word.contains("\"")) {
                    int offset = word.length() - cursorInWord;
                    if (offset > 0) {
                        programStringMut.offsetCursors(offset);
                    } else {
                        programStringMut.insertTextWithoutMovingCursors("\n");
                        programStringMut.offsetCursors(1);
                    }
                } else {
                    programStringMut.insertTextWithoutMovingCursors("\"");
                    programStringMut.offsetCursors(1);
                }
            case SFMLLexer.DO:
                programStringMut.replaceWordAndMoveCursorsToEnd("DO\n    \nEND");
                programStringMut.offsetCursors(-4);

            default:
                programStringMut.replaceWordAndMoveCursorsToEnd(getDisplay() + " ");
        }
        return new ManipulationResult(
                programStringMut.getContent(),
                programStringMut.getCursorPosition(),
                programStringMut.getSelectionCursorPosition()
        );
    }

    private String getDisplay() {
        StringBuilder display = new StringBuilder();
        display.append(vocabulary.getSymbolicName(nextTokenType));
        for (Integer type : followingTokenTypes) {
            display.append(" ");
            display.append(vocabulary.getSymbolicName(type));
        }
        return display.toString();
    }
}
