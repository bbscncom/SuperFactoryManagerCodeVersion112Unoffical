package ca.teamdman.sfml.program_builder;

import ca.teamdman.langs.SFMLLexer;
import ca.teamdman.langs.SFMLParser;
import ca.teamdman.sfml.ast.ASTBuilder;
import net.minecraft.util.text.TextComponentTranslation;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.List;

public class ProgramMetadata {
    public final String programString;
    public final SFMLLexer lexer;
    public final CommonTokenStream tokens;
    public final SFMLParser parser;
    public final ASTBuilder astBuilder;
    public final List<TextComponentTranslation> errors;

    public ProgramMetadata(
            String programString,
            SFMLLexer lexer,
            CommonTokenStream tokens,
            SFMLParser parser,
            ASTBuilder astBuilder,
            List<TextComponentTranslation> errors
    ) {
        this.programString = programString;
        this.lexer = lexer;
        this.tokens = tokens;
        this.parser = parser;
        this.astBuilder = astBuilder;
        this.errors = errors;
    }

    public String getProgramString() {
        return programString;
    }

    public SFMLLexer getLexer() {
        return lexer;
    }

    public CommonTokenStream getTokens() {
        return tokens;
    }

    public SFMLParser getParser() {
        return parser;
    }

    public ASTBuilder getAstBuilder() {
        return astBuilder;
    }

    public List<TextComponentTranslation> getErrors() {
        return errors;
    }
}
