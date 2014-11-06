package home.processor;

import home.annotation.FeatureOpt;
import home.antlr4.XMLLexer;
import home.antlr4.XMLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class XMLAnnotationListenerTest
{
    public static void main(String[] args)
    {
        final String fileName = "AnnotationProcessor/src/main/resources/home/chat_pla.xml";

        try (final InputStream fileStream = new FileInputStream(fileName)) {
            final ANTLRInputStream antIS = new ANTLRInputStream(fileStream);

            final XMLLexer lexer = new XMLLexer(antIS);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);

            final XMLParser parser = new XMLParser(tokens);
            final ParseTree tree = parser.document();

            final ParseTreeWalker walker = new ParseTreeWalker();

            final XMLProcessor pruner = new XMLProcessor(tokens, FeatureOpt.IMAGE_SHARING);
            walker.walk(pruner, tree);

            try (FileWriter writer = new java.io.FileWriter("AnnotationProcessor/src/test/resources/home/out/chat_pla.xml")) {
                writer.write(pruner.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
