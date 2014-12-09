package home.processor;

import home.annotation.FeatureOpt;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

public class ProductLineProcessorProxy
{
    public static void process(String srcDst, String targetDst, String arch, String[] features)
    {
        File archFile = new File(arch);
        File srcDir = new File(srcDst);
        File dstDir = new File(targetDst);

        List<FeatureOpt> featureList = new ArrayList<FeatureOpt>(features.length);
        for (String feature : features) {
            featureList.add(FeatureOpt.valueOf(feature));
        }

        ProductLineProcessor plProcessor = new ProductLineProcessor(srcDir, archFile, dstDir);
        plProcessor.pruneFeatures(featureList.toArray(new FeatureOpt[featureList.size()]));
    }
}
