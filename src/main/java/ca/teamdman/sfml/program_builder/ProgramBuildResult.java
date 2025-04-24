package ca.teamdman.sfml.program_builder;

import ca.teamdman.sfml.ast.Program;
import org.antlr.v4.runtime.Token;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("UnusedReturnValue")
public class ProgramBuildResult {
    public final Program program;
    public final ProgramMetadata metadata;

    public ProgramBuildResult(@Nullable Program program, ProgramMetadata metadata) {
        this.program = program;
        this.metadata = metadata;
    }

    public Program getProgram() {
        return program;
    }

    public ProgramMetadata getMetadata() {
        return metadata;
    }

    public boolean isBuildSuccessful() {
        return program != null && metadata.getErrors().isEmpty();
    }

    public ProgramBuildResult caseSuccess(BiConsumer<Program, ProgramMetadata> callback) {
        if (isBuildSuccessful()) {
            callback.accept(this.program, this.metadata);
        }
        return this;
    }

    public ProgramBuildResult caseFailure(Consumer<ProgramBuildResult> callback) {
        if (!isBuildSuccessful()) {
            callback.accept(this);
        }
        return this;
    }

    public @Nullable Token getTokenAtCursorPosition(int cursorPos) {
        for (Token token : metadata.getTokens().getTokens()) {
            if (token.getStartIndex() <= cursorPos && token.getStopIndex() + 1 >= cursorPos) {
                return token;
            }
        }
        return null;
    }

    public String getWordAtCursorPosition(int cursorPos) {
        StringBuilder word = new StringBuilder();
        for (int i = cursorPos - 1; i >= 0; i--) {
            char c = this.metadata.getProgramString().charAt(i);
            if (Character.isWhitespace(c)) {
                break;
            }
            word.insert(0, c);
        }
        return word.toString();
    }
}
