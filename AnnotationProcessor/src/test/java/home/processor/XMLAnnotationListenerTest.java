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
    public static void main(String[] args)
    {
        final String fileName = "AnnotationProcessor/src/test/resources/home/sample/arch.xml";

        try (final InputStream fileStream = new FileInputStream(fileName)) {
            final ANTLRInputStream antIS = new ANTLRInputStream(fileStream);

            final XMLLexer lexer = new XMLLexer(antIS);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);

            final XMLParser parser = new XMLParser(tokens);
            final ParseTree tree = parser.document();

            final ParseTreeWalker walker = new ParseTreeWalker();

            final XMLProcessor pruner = new XMLProcessor(tokens);
            walker.walk(pruner, tree);

            System.out.println(pruner);
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
    }
}
