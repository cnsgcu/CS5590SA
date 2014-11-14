package home.processor;

import home.annotation.FeatureOpt;
import home.antlr4.JavaLexer;
import home.antlr4.JavaParser;
import home.antlr4.XMLLexer;
import home.antlr4.XMLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

public class ProductLineProcessorTest
{
    private static final String IN_SRC  = "AnnotationProcessor/src/main/resources/home/";
    private static final String OUT_SRC = "AnnotationProcessor/src/test/resources/home/";
    private static final String ANNOTATED_CLIENT = "AnnotationProcessor/src/test/resources/home/com/pla/chatsys/client/ClientImp.java";
    private static final String ANNOTATED_SERVER = "AnnotationProcessor/src/test/resources/home/com/pla/chatsys/server/ServerImp.java";

    public static void main(String[] args)
    {
        pruneFeatures(FeatureOpt.CHAT_HISTORY, FeatureOpt.TEMPLATE, FeatureOpt.GAME);
    }

    private static void pruneFeatures(FeatureOpt... opts)
    {
        pruneArch(IN_SRC, OUT_SRC, opts);

        pruneSource(ANNOTATED_CLIENT, opts);
        pruneSource(ANNOTATED_SERVER, opts);
    }

    private static void pruneSource(String srcFile, FeatureOpt... opts)
    {
        try (final InputStream fileStream = new FileInputStream(srcFile)) {
            final ANTLRInputStream antIS = new ANTLRInputStream(fileStream);

            final JavaLexer lexer = new JavaLexer(antIS);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);

            final JavaParser parser = new JavaParser(tokens);
            final ParseTree tree = parser.compilationUnit();

            final ParseTreeWalker walker = new ParseTreeWalker();
            final JavaProcessor pruner = new JavaProcessor(tokens, opts);

            walker.walk(pruner, tree);

            try (FileWriter writer = new FileWriter(srcFile)) {
                writer.write(pruner.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void pruneArch(final String fin, final String fout, FeatureOpt... opts)
    {
        final String archFileName = "chat_pla.xml";

        try (final InputStream fileStream = new FileInputStream(fin + archFileName)) {
            final ANTLRInputStream antIS = new ANTLRInputStream(fileStream);

            final XMLLexer lexer = new XMLLexer(antIS);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);

            final XMLParser parser = new XMLParser(tokens);
            final ParseTree tree = parser.document();

            final ParseTreeWalker walker = new ParseTreeWalker();

            final FileProcessor filePruner = new FileProcessor(fin, fout);
            final XMLProcessor pruner = new XMLProcessor(filePruner, tokens, opts);

            walker.walk(pruner, tree);

            try (FileWriter writer = new java.io.FileWriter(fout + archFileName)) {
                writer.write(pruner.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            filePruner.prune(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.isHidden()
                            && !file.getName().endsWith("xml")
                            && !filePruner.contains(file.getPath().replace(fout, ""));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
