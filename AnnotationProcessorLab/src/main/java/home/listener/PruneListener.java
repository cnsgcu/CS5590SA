package home.listener;

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

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PruneListener extends JavaBaseListener
{
    private TokenStream tokens;
    private TokenStreamRewriter rewriter;

    public PruneListener(TokenStream tokens)
    {
        this.tokens = tokens;
        this.rewriter = new TokenStreamRewriter(tokens);
    }

    @Override
    public String toString()
    {
        return this.rewriter.getText();
    }

    @Override
    public void exitImportDeclaration(JavaParser.ImportDeclarationContext ctx)
    {
        prune(ctx, token -> rewriter.delete(token, ctx.getStop()));
    }

    @Override
    public void exitStatement(JavaParser.StatementContext ctx)
    {
        prune(ctx, token -> rewriter.delete(token, ctx.getStop()));
    }

    @Override
    public void exitSuperclass(JavaParser.SuperclassContext ctx)
    {
        prune((ParserRuleContext) ctx.getChild(1), token -> rewriter.delete(ctx.getStart(), ctx.getStop()));
    }

    @Override
    public void exitSuperinterfaces(JavaParser.SuperinterfacesContext ctx)
    {
        List<ParseTree> keepInterfaceTypes = ((ParserRuleContext) ctx.getChild(1)).children.stream()
            .filter(child -> child instanceof ParserRuleContext)
            .filter(child -> {
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

                        if (annotationCtx != null) {
                            return false;
                        }
                    }
                }

                return true;
            })
            .collect(Collectors.toList());

        if (keepInterfaceTypes.isEmpty()) {
            rewriter.delete(ctx.getStart(), ctx.getStop());
        } else {
            String interfaceList = keepInterfaceTypes.stream()
                .map(ParseTree::getText)
                .reduce((il, ir) -> il + "," + ir)
                .orElse("");

            rewriter.replace(ctx.getStart(), ctx.getStop(), ctx.children.get(0).getText() + " " + interfaceList);
        }
    }

    @Override
    public void exitFieldDeclaration(JavaParser.FieldDeclarationContext ctx)
    {
        final ParserRuleContext classBodyDeclaration = ctx.getParent().getParent();

        for (ParseTree child : classBodyDeclaration.children) {
            if (child instanceof JavaParser.ModifierContext) {
                final JavaParser.AnnotationContext annotationContext = ((JavaParser.ModifierContext) child).classOrInterfaceModifier().annotation();

                if (isPrune(annotationContext)) {
                    rewriter.delete(classBodyDeclaration.getStart(), classBodyDeclaration.getStop());
                    break;
                }
            }
        }
    }

    @Override
    public void exitMethodDeclaration(JavaParser.MethodDeclarationContext ctx)
    {
        final ParserRuleContext classBodyDeclaration = ctx.getParent().getParent();

        for (ParseTree child : classBodyDeclaration.children) {
            if (child instanceof JavaParser.ModifierContext) {
                final JavaParser.AnnotationContext annotationContext = ((JavaParser.ModifierContext) child).classOrInterfaceModifier().annotation();

                if (isPrune(annotationContext)) {
                    rewriter.delete(classBodyDeclaration.getStart(), classBodyDeclaration.getStop());
                    break;
                }
            }
        }
    }

    private void prune(final ParserRuleContext prCtx, final Consumer<Token> action)
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

                if (annotationCtx != null) {
                    action.accept(hiddenToken);
                    break;
                }
            }
        }
    }

    private boolean isPrune(final JavaParser.AnnotationContext annotationContext)
    {
        return annotationContext != null
            && "Feature".equals(tokens.getText(annotationContext.annotationName()))
            && "\"chatting\"".equals(tokens.getText(annotationContext.elementValue()));
    }
}
