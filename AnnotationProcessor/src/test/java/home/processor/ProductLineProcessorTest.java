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
import java.util.LinkedList;
import java.util.List;

public class ProductLineProcessorTest
{
    private static final String SRC_DIR = "AnnotationProcessor/src/main/resources/home/";
    private static final String DST_DIR = "AnnotationProcessor/src/test/resources/home/";

    private static final String ANNOTATED_ARCH = "chat_pla.xml";

    public static void main(String[] args)
    {
        pruneFeatures();
    }

    private static void pruneFeatures(FeatureOpt... opts)
    {
        pruneArch(SRC_DIR, DST_DIR, opts);

        for (File srcFile : srcFiles(new File(DST_DIR), new LinkedList<File>())) {
            pruneSource(srcFile, opts);
        }
    }

    private static List<File> srcFiles(File srcDir, List<File> srcFiles)
    {
        for (File file : srcDir.listFiles()) {
            if (file.isDirectory()) {
                srcFiles(file, srcFiles);
            } else if (file.getName().endsWith(".java")) {
                srcFiles.add(file);
            }
        }

        return srcFiles;
    }

    private static void pruneSource(File srcFile, FeatureOpt... opts)
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

    private static void pruneArch(final String srcDir, final String dstDir, FeatureOpt... opts)
    {
        try (final InputStream fileStream = new FileInputStream(srcDir + ANNOTATED_ARCH)) {
            final ANTLRInputStream antIS = new ANTLRInputStream(fileStream);

            final XMLLexer lexer = new XMLLexer(antIS);
            final CommonTokenStream tokens = new CommonTokenStream(lexer);

            final XMLParser parser = new XMLParser(tokens);
            final ParseTree tree = parser.document();

            final ParseTreeWalker walker = new ParseTreeWalker();

            final FileProcessor filePruner = new FileProcessor(srcDir, dstDir);
            final XMLProcessor pruner = new XMLProcessor(filePruner, tokens, opts);

            walker.walk(pruner, tree);

            try (FileWriter writer = new java.io.FileWriter(dstDir + ANNOTATED_ARCH)) {
                writer.write(pruner.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }

            filePruner.prune(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return !file.isHidden()
                        && !file.getName().endsWith("xml")
                        && !filePruner.contains(file.getPath().replace(dstDir, ""));
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
