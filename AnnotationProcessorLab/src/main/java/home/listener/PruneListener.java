package home.listener;

import home.annotation.Feature;
import home.annotation.FeatureOpt;
import home.grammar.JavaBaseListener;
import home.grammar.JavaLexer;
import home.grammar.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.TokenStreamRewriter;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

public class PruneListener extends JavaBaseListener
{
    private TokenStream tokens;
    private TokenStreamRewriter rewriter;
    private Set<FeatureOpt> featureOptSet;

    public PruneListener(TokenStream tokens, FeatureOpt... featureOpts)
    {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
        this.featureOptSet = new TreeSet<>(Arrays.asList(featureOpts));
    }

    @Override
    public String toString()
    {
        return this.rewriter.getText();
    }

    @Override
    public void exitImportDeclaration(JavaParser.ImportDeclarationContext ctx)
    {
        pruneBlock(ctx, token -> rewriter.delete(token, ctx.getStop()));
    }

    @Override
    public void exitStatement(JavaParser.StatementContext ctx)
    {
        pruneBlock(ctx, token -> rewriter.delete(token, ctx.getStop()));
    }

    @Override
    public void exitSuperclass(JavaParser.SuperclassContext ctx)
    {
        pruneBlock((ParserRuleContext) ctx.getChild(1), token -> rewriter.delete(ctx.getStart(), ctx.getStop()));
    }

    @Override
    public void exitSuperinterfaces(JavaParser.SuperinterfacesContext ctx)
    {
        List<ParseTree> keepInterfaceTypes = new LinkedList<>();

        for (ParseTree child : ((ParserRuleContext) ctx.getChild(1)).children) {
            if (child instanceof ParserRuleContext) {
                final ParserRuleContext ruleContext = (ParserRuleContext) child;

                final List<Token> leftHiddenTokens = ((CommonTokenStream) tokens).getHiddenTokensToLeft(
                    ruleContext.getStart().getTokenIndex(),
                    JavaLexer.COMMENT_CHANNEL
                );

                if (leftHiddenTokens != null) {
                    for (Token hiddenToken : leftHiddenTokens) {
                        final String htText = hiddenToken.getText();
                        final ANTLRInputStream htInputStream = new ANTLRInputStream(
                                htText.substring(2, htText.length() - 2)
                        );

                        final JavaLexer htLexer = new JavaLexer(htInputStream);
                        final CommonTokenStream hiddenTokenStream = new CommonTokenStream(htLexer);

                        final JavaParser htParser = new JavaParser(hiddenTokenStream);
                        final JavaParser.AnnotationContext annotationCtx = htParser.annotation();

                        if (!isPruneAnnotation(annotationCtx)) {
                            keepInterfaceTypes.add(child);
                            break;
                        }
                    }
                } else {
                    keepInterfaceTypes.add(child);
                }
            }
        }

        if (keepInterfaceTypes.isEmpty()) {
            rewriter.delete(ctx.getStart(), ctx.getStop());
        } else {
            String interfaceList = keepInterfaceTypes.stream()
                .map(ParseTree::getText)
                .reduce((il, ir) -> il + ", " + ir)
                .orElse("");

            rewriter.replace(ctx.getStart(), ctx.getStop(), ctx.children.get(0).getText() + " " + interfaceList);
        }
    }

    @Override
    public void exitFieldDeclaration(JavaParser.FieldDeclarationContext ctx)
    {
        prune(ctx);
    }

    @Override
    public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx)
    {
        prune(ctx);
    }

    private void prune(final ParserRuleContext prCtx)
    {
        final ParserRuleContext classBodyDeclaration = prCtx.getParent().getParent();

        for (ParseTree child : classBodyDeclaration.children) {
            if (child instanceof JavaParser.ModifierContext) {
                final JavaParser.AnnotationContext annotationCtx = ((JavaParser.ModifierContext) child).classOrInterfaceModifier().annotation();

                if (isPruneAnnotation(annotationCtx)) {
                    rewriter.delete(classBodyDeclaration.getStart(), classBodyDeclaration.getStop());
                    break;
                } else if (isFeatureAnnotation(annotationCtx)) {
                    rewriter.delete(annotationCtx.getStart(), annotationCtx.getStop());
                    break;
                }
            }
        }
    }

    private void pruneBlock(final ParserRuleContext prCtx, final Consumer<Token> action)
    {
        final List<Token> leftHiddenTokens = ((CommonTokenStream) tokens).getHiddenTokensToLeft(
            prCtx.getStart().getTokenIndex(),
            JavaLexer.COMMENT_CHANNEL
        );

        if (leftHiddenTokens != null) {
            for (Token hiddenToken : leftHiddenTokens) {
                final String htText = hiddenToken.getText();
                final ANTLRInputStream htInputStream = new ANTLRInputStream(htText.substring(2, htText.length() - 2));

                final JavaLexer htLexer = new JavaLexer(htInputStream);
                final CommonTokenStream htStream = new CommonTokenStream(htLexer);

                final JavaParser htParser = new JavaParser(htStream);
                final JavaParser.AnnotationContext annotationCtx = htParser.annotation();

                if (isPruneAnnotation(annotationCtx)) {
                    action.accept(hiddenToken);
                    break;
                } else if (isFeatureAnnotation(annotationCtx)) {
                    rewriter.delete(hiddenToken);
                    break;
                }
            }
        }
    }

    private boolean isPruneAnnotation(final JavaParser.AnnotationContext ctx)
    {
        if (isFeatureAnnotation(ctx)) {
            final JavaParser.ElementValueArrayInitializerContext arrayInitializerCtx = ctx.elementValue().elementValueArrayInitializer();

            if (arrayInitializerCtx != null) {
                for (ParseTree child : arrayInitializerCtx.children) {
                    if (child instanceof JavaParser.ElementValueContext) {
                        final String name = child.getText().replace(FeatureOpt.class.getSimpleName() + ".", "");

                        if (!featureOptSet.contains(FeatureOpt.valueOf(name))) {
                            return false;
                        }
                    }
                }
            } else {
                final String name = ctx.elementValue().getText().replace(FeatureOpt.class.getSimpleName() + ".", "");

                if (!featureOptSet.contains(FeatureOpt.valueOf(name))) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private boolean isFeatureAnnotation(final JavaParser.AnnotationContext annotationContext)
    {
        return annotationContext != null
            && Feature.class.getSimpleName().equals(annotationContext.annotationName().getText());
    }
}
