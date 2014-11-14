package home.processor;

import home.antlr4.XMLParser;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.TreeSet;

public class FileProcessor
{
    final private File srcDir;
    final private File dstDir;
    final private Set<String> pruneFiles = new TreeSet<>();

    public FileProcessor(String fin, String fout)
    {
        this.srcDir = new File(fin);
        this.dstDir = new File(fout);
    }

    public boolean contains(String filePath)
    {
        return pruneFiles.contains(filePath);
    }

    public void prune(FileFilter filter)
    {
        relocate(srcDir, dstDir, filter);
    }

    private void relocate(File srcRoot, File dstRoot, FileFilter fileFilter)
    {
        if (fileFilter.accept(dstRoot)) {
            if (!dstRoot.exists()) {
                dstRoot.mkdir();
            }

            for (File file : srcRoot.listFiles()) {
                final String fn = file.getName();
                final File dst = new File(dstRoot, fn);

                if (file.isDirectory()) {
                    final File src = new File(srcRoot, fn);
                    relocate(src, dst, fileFilter);
                } else {
                    if (fileFilter.accept(dst)) {
                        try (
                            final InputStream fin = new FileInputStream(file);
                            final OutputStream fout = new FileOutputStream(dst)
                        ) {
                            final byte[] buffer = new byte[1024];

                            int byteCount;
                            while((byteCount = fin.read(buffer)) > 0) {
                                fout.write(buffer, 0, byteCount);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public void addPruneFiles(XMLParser.ElementContext ctx)
    {
        if ("types:interfaceType".equals(ctx.getChild(1).getText())) {
            ParseTree tree = getInterfaceImp(ctx);
            pruneFiles.add(tree.getText().replace('.', '/') + ".java");
        } else if ("types:component".equals(ctx.getChild(1).getText())) {
            ParseTree tree = getComponentImp(ctx);

            for (ParseTree child : ((XMLParser.ElementContext) tree).children) {
                if (child instanceof XMLParser.ContentContext) {
                    XMLParser.ContentContext cntCtx = (XMLParser.ContentContext) child;

                    for (ParseTree gChild : cntCtx.children) {
                        if (gChild instanceof XMLParser.ElementContext) {
                            String fn = gChild.getChild(4).getChild(1).getChild(4).getText();
                            pruneFiles.add(fn.substring(0, fn.lastIndexOf('.')).replace('.', '/'));
                        }
                    }
                }
            }
        }
    }

    private ParseTree getInterfaceImp(XMLParser.ElementContext ctx)
    {
        return ctx.getChild(5).getChild(3).getChild(4).getChild(1).getChild(4).getChild(1).getChild(4);
    }

    private ParseTree getComponentImp(XMLParser.ElementContext ctx)
    {
        for (ParseTree child : ctx.children) {
            if (child instanceof XMLParser.ContentContext) {
                XMLParser.ContentContext cntCtx = (XMLParser.ContentContext) child;

                for (ParseTree gChild : cntCtx.children) {
                    if (gChild instanceof XMLParser.ElementContext
                        && "implementationext:implementation".equals(gChild.getChild(1).getText())) {
                        return gChild;
                    }
                }
            }
        }

        return null;
    }
}
