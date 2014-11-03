package home.processor;

import home.antlr4.XMLLexer;
import home.antlr4.XMLParser;
import home.antlr4.XMLParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.List;

public class XMLProcessor extends XMLParserBaseListener
{
    private TokenStream tokens;
    private TokenStreamRewriter rewriter;

    public XMLProcessor(TokenStream tokens)
    {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
    }

    @Override
    public String toString()
    {
        return rewriter.getText();
    }

    @Override
    public void exitElement(XMLParser.ElementContext ctx)
    {
        List<Token> tokenList =  ((CommonTokenStream) tokens).getHiddenTokensToLeft(
            ctx.getStart().getTokenIndex(),
            XMLLexer.COMMENT_CHANNEL
        );

        if (tokenList != null) {
            rewriter.delete(tokenList.get(0).getTokenIndex(), ctx.getStop().getTokenIndex());
        }
    }
}
