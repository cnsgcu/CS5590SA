package home.processor;

import home.antlr4.XMLLexer;
import home.antlr4.XMLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XMLAnnotationListenerTest
{
    public static void main(String[] args) throws IOException
    {
        final String file = "AnnotationProcessorLab/src/test/resources/home/sample/arch.xml";
        final InputStream fileInput = new FileInputStream(file);

        final ANTLRInputStream antIS = new ANTLRInputStream(fileInput);

        final XMLLexer lexer = new XMLLexer(antIS);
        final CommonTokenStream tokens = new CommonTokenStream(lexer);

        final XMLParser parser = new XMLParser(tokens);
        final ParseTree tree = parser.document();

        final ParseTreeWalker walker = new ParseTreeWalker();

        final XMLAnnotationListener pruner = new XMLAnnotationListener(tokens);
        walker.walk(pruner, tree);

        System.out.println(pruner);
    }
}
