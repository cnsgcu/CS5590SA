package home.processor;

import home.annotation.FeatureOpt;
import home.antlr4.JavaLexer;
import home.antlr4.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class JavaAnnotationListenerTest
{
    public static void main(String[] args) throws IOException
    {
        final String file = "AnnotationProcessor/src/test/resources/home/sample/ClientArch.java";
        final InputStream fileInput = new FileInputStream(file);

        final ANTLRInputStream antIS = new ANTLRInputStream(fileInput);

        final JavaLexer lexer = new JavaLexer(antIS);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final JavaParser parser = new JavaParser(tokens);
        final ParseTree tree = parser.compilationUnit();

        final ParseTreeWalker walker = new ParseTreeWalker();
        final JavaAnnotationListener pruner = new JavaAnnotationListener(tokens, FeatureOpt.CHATTING);
        walker.walk(pruner, tree);

        System.out.println(pruner);
    }
}
