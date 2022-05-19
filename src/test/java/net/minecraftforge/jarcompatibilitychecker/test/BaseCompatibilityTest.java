package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoCache;
import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoComparer;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class BaseCompatibilityTest {
    protected Path getRoot() {
        URL url = this.getClass().getResource("/test.marker");
        Assert.assertNotNull("Could not find test.marker", url);

        try {
            return new File(url.toURI()).getParentFile().toPath();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertCompatible(boolean checkBinary, String folder, String className) {
        ClassInfoComparer.Results comparisonResults = getComparisonResults(checkBinary, folder, className);
        Assert.assertTrue(className + " had incompatibilities when none were expected: " + comparisonResults, comparisonResults.isCompatible());
    }

    protected ClassInfoComparer.Results getComparisonResults(boolean checkBinary, String folderName, String className) {
        try {
            Path folder = getRoot().resolve(folderName);
            Path baseFolder = folder.resolve("base");
            Assert.assertTrue(baseFolder + " not found", Files.exists(baseFolder));

            Path inputFolder = folder.resolve("input");
            Assert.assertTrue(inputFolder + " not found", Files.exists(inputFolder));

            ClassInfoCache baseCache = ClassInfoCache.fromFolder(baseFolder);
            ClassInfoCache inputCache = ClassInfoCache.fromFolder(inputFolder);

            ClassInfo baseClassInfo = baseCache.getClassInfo(className);
            Assert.assertNotNull("Class with name " + className + " not found in " + baseFolder, baseClassInfo);

            return ClassInfoComparer.compare(checkBinary, baseCache, baseClassInfo, inputCache, inputCache.getClassInfo(className));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
