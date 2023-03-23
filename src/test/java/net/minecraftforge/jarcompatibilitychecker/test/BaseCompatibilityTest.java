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

    protected void assertIncompatible(boolean checkBinary, String folder, String className, IncompatibilityData... testIncompatibilities) {
        if (testIncompatibilities.length == 0)
            throw new IllegalArgumentException("Must provide at least one incompatibility to test");

        ClassInfoComparisonResults comparisonResults = getComparisonResults(checkBinary, folder, className);
        Assert.assertFalse(className + " was compatible when incompatibilities were expected", comparisonResults.isCompatible());

        List<Incompatibility<?>> incompatibilities = comparisonResults.getIncompatibilities();
        Assert.assertEquals(className + " had the wrong number of incompatibilities: " + comparisonResults, testIncompatibilities.length, incompatibilities.size());

        for (int i = 0; i < testIncompatibilities.length; i++) {
            IncompatibilityData testData = testIncompatibilities[i];
            Incompatibility<?> incompatibility = incompatibilities.get(i);
            Assert.assertEquals(className + " had an incompatibility with the wrong name: " + incompatibility, testData.getName(), incompatibility.getInfo().getName());
            Assert.assertEquals(className + " had an incompatibility with the wrong descriptor: " + incompatibility, testData.getDesc(), incompatibility.getInfo().getDescriptor());
            Assert.assertEquals(className + " had an incompatibility with the wrong message: " + incompatibility, testData.getMessage(), incompatibility.getMessage());
            Assert.assertEquals(className + " had an incompatibility with mismatch error vs. warning: " + incompatibility, testData.isError(), incompatibility.isError());
        }
    }

    protected void assertClassIncompatible(boolean checkBinary, String folder, String className, String message, Object... formatArgs) {
        assertClassIncompatible(checkBinary, folder, className, true, message, formatArgs);
    }

    protected void assertClassIncompatible(boolean checkBinary, String folder, String className, boolean isError, String message, Object... formatArgs) {
        assertIncompatible(checkBinary, folder, className, className, null, isError, message, formatArgs);
    }

    protected void assertIncompatible(boolean checkBinary, String folder, String className, String name, @Nullable String desc, String message, Object... formatArgs) {
        assertIncompatible(checkBinary, folder, className, name, desc, true, message, formatArgs);
    }

    protected void assertIncompatible(boolean checkBinary, String folder, String className, String name, @Nullable String desc, boolean isError, String message, Object... formatArgs) {
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
        Assert.assertEquals(className + " had an incompatibility with mismatch error vs. warning: " + incompatibility, isError, incompatibility.isError());
    }

    protected void assertCompatible(boolean checkBinary, String folder, String className) {
        ClassInfoComparisonResults comparisonResults = getComparisonResults(checkBinary, folder, className);
        Assert.assertTrue(className + " had incompatibilities when none were expected: " + comparisonResults, comparisonResults.isCompatible());
    }

    protected ClassInfoComparisonResults getComparisonResults(boolean checkBinary, String folderName, String className) {
        try {
            Path folder = getRoot().resolve(folderName);
            if (!folder.toRealPath().toString().equals(folder.toAbsolutePath().toString()))
                throw new IllegalArgumentException("Folder \"" + folderName + "\" does not match the real path \"" + folder.toRealPath().getFileName().toString() + "\"");

            Path baseFolder = folder.resolve("base");
            Assert.assertTrue(baseFolder + " not found", Files.exists(baseFolder));
            Assert.assertEquals("Base folder in " + folderName + " has invalid casing", baseFolder.toAbsolutePath(), baseFolder.toRealPath());

            Path inputFolder = folder.resolve("input");
            boolean inputExists = Files.exists(inputFolder); // If it doesn't exist, all base classes got deleted, which is technically valid.
            if (inputExists)
                Assert.assertEquals("Input folder in " + folderName + " has invalid casing", inputFolder.toAbsolutePath(), inputFolder.toRealPath());

            ClassInfoCache baseCache = ClassInfoCache.fromFolder(baseFolder);
            ClassInfoCache inputCache = inputExists ? ClassInfoCache.fromFolder(inputFolder) : ClassInfoCache.empty();

            ClassInfo baseClassInfo = baseCache.getMainClassInfo(className);
            Assert.assertNotNull("Class with name " + className + " not found in " + baseFolder, baseClassInfo);

            return ClassInfoComparer.compare(checkBinary, baseCache, baseClassInfo, inputCache, inputCache.getMainClassInfo(className));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static class IncompatibilityData {
        private final String name;
        @Nullable
        private final String desc;
        private final String message;
        private final boolean isError;

        protected IncompatibilityData(String name, @Nullable String desc, String message, Object... formatArgs) {
            this(name, desc, true, message, formatArgs);
        }

        protected IncompatibilityData(String name, @Nullable String desc, boolean isError, String message, Object... formatArgs) {
            this.name = name;
            this.desc = desc;
            this.isError = isError;
            this.message = formatArgs.length > 0 ? String.format(Locale.ROOT, message, formatArgs) : message;
        }

        protected String getName() {
            return this.name;
        }

        @Nullable
        protected String getDesc() {
            return this.desc;
        }

        protected String getMessage() {
            return this.message;
        }

        protected boolean isError() {
            return this.isError;
        }
    }
}
