package home.listener;

import home.grammar.JavaLexer;
import home.grammar.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class PruneListenerTest
{
    public static void main(String[] args) throws IOException
    {
        final String file = "AnnotationProcessorLab/src/test/resources/home/sample/ClientImp.java";
        final InputStream fileInput = new FileInputStream(file);

        final ANTLRInputStream antIS = new ANTLRInputStream(fileInput);

        final JavaLexer lexer = new JavaLexer(antIS);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final JavaParser parser = new JavaParser(tokens);
        final ParseTree tree = parser.compilationUnit();

        final ParseTreeWalker walker = new ParseTreeWalker();
        final PruneListener pruner = new PruneListener(tokens);
        walker.walk(pruner, tree);

        System.out.println(pruner);
    }
}
