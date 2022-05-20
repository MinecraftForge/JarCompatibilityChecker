/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoCache;
import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoComparer;
import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoComparisonResults;
import net.minecraftforge.jarcompatibilitychecker.core.Incompatibility;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

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

    protected void assertClassIncompatible(boolean checkBinary, String folder, String className, String message, Object... formatArgs) {
        assertIncompatible(checkBinary, folder, className, className, null, message, formatArgs);
    }

    protected void assertIncompatible(boolean checkBinary, String folder, String className, String name, @Nullable String desc, String message, Object... formatArgs) {
        if (formatArgs.length > 0)
            message = String.format(Locale.ROOT, message, formatArgs);
        ClassInfoComparisonResults comparisonResults = getComparisonResults(checkBinary, folder, className);
        Assert.assertFalse(className + " was compatible when incompatibilities were expected", comparisonResults.isCompatible());

        List<Incompatibility<?>> incompatibilities = comparisonResults.getIncompatibilities();
        Assert.assertEquals(className + " had more than one incompatibility when one was expected: " + comparisonResults, 1, incompatibilities.size());

        Incompatibility<?> incompatibility = incompatibilities.get(0);
        Assert.assertEquals(className + " had an incompatibility with the wrong name: " + incompatibility, name, incompatibility.getInfo().getName());
        Assert.assertEquals(className + " had an incompatibility with the wrong descriptor: " + incompatibility, desc, incompatibility.getInfo().getDescriptor());
        Assert.assertEquals(className + " had an incompatibility with the wrong message: " + incompatibility, message, incompatibility.getMessage());
    }

    protected void assertCompatible(boolean checkBinary, String folder, String className) {
        ClassInfoComparisonResults comparisonResults = getComparisonResults(checkBinary, folder, className);
        Assert.assertTrue(className + " had incompatibilities when none were expected: " + comparisonResults, comparisonResults.isCompatible());
    }

    protected ClassInfoComparisonResults getComparisonResults(boolean checkBinary, String folderName, String className) {
        try {
            Path folder = getRoot().resolve(folderName);
            Path baseFolder = folder.resolve("base");
            Assert.assertTrue(baseFolder + " not found", Files.exists(baseFolder));

            Path inputFolder = folder.resolve("input");
            Assert.assertTrue(inputFolder + " not found", Files.exists(inputFolder));

            ClassInfoCache baseCache = ClassInfoCache.fromFolder(baseFolder);
            ClassInfoCache inputCache = ClassInfoCache.fromFolder(inputFolder);

            ClassInfo baseClassInfo = baseCache.getMainClassInfo(className);
            Assert.assertNotNull("Class with name " + className + " not found in " + baseFolder, baseClassInfo);

            return ClassInfoComparer.compare(checkBinary, baseCache, baseClassInfo, inputCache, inputCache.getMainClassInfo(className));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
