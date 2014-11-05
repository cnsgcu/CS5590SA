package home.processor;

import home.annotation.FeatureOpt;
import home.antlr4.XMLLexer;
import home.antlr4.XMLParser;
import home.antlr4.XMLParserBaseListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class XMLProcessor extends XMLParserBaseListener
{
    final private TokenStream tokens;
    final private TokenStreamRewriter rewriter;
    final private Set<FeatureOpt> featureOptSet;

    public XMLProcessor(TokenStream tokens, FeatureOpt... featureOpts)
    {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
        this.featureOptSet = new TreeSet<>(Arrays.asList(featureOpts));
    }

    @Override
    public String toString()
    {
        return rewriter.getText();
    }

    @Override
    public void exitElement(XMLParser.ElementContext ctx)
    {
        final List<Token> tokenList =  ((CommonTokenStream) tokens).getHiddenTokensToLeft(
            ctx.getStart().getTokenIndex(),
            XMLLexer.COMMENT_CHANNEL
        );

        if (tokenList != null) {
            final Token token = tokenList.get(0);
            String featureName = token.getText().trim();
            featureName = featureName.substring(4, featureName.length() - 3).trim();

            for (FeatureOpt opt : featureOptSet) {
                if (opt.name().toLowerCase().equals(featureName)) {
                    rewriter.delete(token.getTokenIndex(), ctx.getStop().getTokenIndex());
                    return;
                }
            }

            rewriter.delete(token.getTokenIndex());
        }
    }
}
