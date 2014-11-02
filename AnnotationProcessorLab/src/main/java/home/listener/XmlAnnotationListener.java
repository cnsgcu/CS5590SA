package home.listener;

import home.grammar.XMLLexer;
import home.grammar.XMLParser;
import home.grammar.XMLParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.List;

public class XMLAnnotationListener extends XMLParserBaseListener
{
    private TokenStream tokens;
    private TokenStreamRewriter rewriter;

    public XMLAnnotationListener(TokenStream tokens)
    {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
    }

    @Override
    public void enterElement(XMLParser.ElementContext ctx)
    {
        List<Token> tokenList =  ((CommonTokenStream) tokens).getHiddenTokensToLeft(
            ctx.getStart().getTokenIndex(),
            XMLLexer.COMMENT_CHANNEL
        );

        if (tokenList != null) {
            for (Token token : tokenList) {
                System.out.println(token.getText());
            }
        }
    }
}
