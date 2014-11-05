package home.processor;

import home.annotation.FeatureOpt;
import home.antlr4.JavaLexer;
import home.antlr4.JavaParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class JavaAnnotationListenerTest
{
    public static void main(String[] args)
    {
        process(
            "AnnotationProcessor/src/main/resources/home/com/pla/chatsys/client/ClientImp.java",
            "AnnotationProcessor/src/test/resources/home/out/ClientImp.java"
        );

        process(
            "AnnotationProcessor/src/main/resources/home/com/pla/chatsys/server/ServerImp.java",
            "AnnotationProcessor/src/test/resources/home/out/ServerImp.java"
        );
    }

    private static void process(String in, String out) {
        final String fileName = in;

        try (final InputStream fileStream = new FileInputStream(fileName)) {
            final ANTLRInputStream antIS = new ANTLRInputStream(fileStream);

            final JavaLexer lexer = new JavaLexer(antIS);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);

            final JavaParser parser = new JavaParser(tokens);
            final ParseTree tree = parser.compilationUnit();

            final ParseTreeWalker walker = new ParseTreeWalker();
            final JavaProcessor pruner = new JavaProcessor(tokens, FeatureOpt.CHAT_HISTORY);
            walker.walk(pruner, tree);

            try (FileWriter writer = new FileWriter(out)) {
                writer.write(pruner.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
