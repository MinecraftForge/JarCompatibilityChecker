/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker;

import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoCache;
import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoComparer;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class JarCompatibilityChecker {
    private final File baseJar;
    private final File inputJar;
    private final boolean checkBinary;
    private final List<File> commonLibs;
    private final List<File> baseLibs;
    private final List<File> concreteLibs;
    private final Consumer<String> stdLogger;
    private final Consumer<String> errLogger;

    /**
     * Constructs a new JarCompatibilityChecker.
     *
     * @param checkBinary if {@code true}, all members of the base jar including package-private and private will be checked for a match in the input jar.
     * Otherwise, only public and protected members of the base jar will be checked for a match in the input jar.
     */
    public JarCompatibilityChecker(File baseJar, File inputJar, boolean checkBinary, List<File> commonLibs, List<File> baseLibs, List<File> concreteLibs, Consumer<String> stdLogger,
            Consumer<String> errLogger) {
        this.baseJar = baseJar;
        this.inputJar = inputJar;
        this.checkBinary = checkBinary;
        this.commonLibs = commonLibs;
        this.baseLibs = baseLibs;
        this.concreteLibs = concreteLibs;
        this.stdLogger = stdLogger;
        this.errLogger = errLogger;
    }

    private void log(String message) {
        this.stdLogger.accept(message);
    }

    private void logError(String message) {
        this.errLogger.accept(message);
    }

    /**
     * Loads the base jar and input jar and compares them for compatibility based on the current mode, API or binary.
     * Any incompatibilities will be logged to the error logger.
     *
     * @return the number of incompatibilities detected based on the current mode
     */
    public int check() throws IOException {
        log("Compatibility mode: " + (this.checkBinary ? "Binary" : "API"));
        log("Base JAR: " + this.baseJar.getAbsolutePath());
        log("Input JAR: " + this.inputJar.getAbsolutePath());
        for (File baseLib : this.baseLibs) {
            log("Base Library: " + baseLib.getAbsolutePath());
        }
        for (File concreteLib : this.concreteLibs) {
            log("Concrete Library: " + concreteLib.getAbsolutePath());
        }
        for (File commonLib : this.commonLibs) {
            log("Common Library: " + commonLib.getAbsolutePath());
        }

        List<File> baseFiles = new ArrayList<>(this.baseLibs);
        baseFiles.addAll(this.commonLibs);
        ClassInfoCache baseCache = ClassInfoCache.fromJarFile(this.baseJar, baseFiles);
        List<File> concreteFiles = new ArrayList<>(this.concreteLibs);
        concreteFiles.addAll(this.commonLibs);
        ClassInfoCache concreteCache = ClassInfoCache.fromJarFile(this.inputJar, concreteFiles);
        List<ClassInfoComparer.Results> classIncompatibilities = new ArrayList<>();

        for (Map.Entry<String, ClassInfo> baseEntry : baseCache.getMainClasses().entrySet()) {
            String baseClassName = baseEntry.getKey();
            ClassInfo baseClassInfo = baseEntry.getValue();
            ClassInfo concreteClassInfo = concreteCache.getMainClassInfo(baseClassName);

            // log("Comparing " + baseClassName);
            ClassInfoComparer.Results results = ClassInfoComparer.compare(this.checkBinary, baseCache, baseClassInfo, concreteCache, concreteClassInfo);
            if (results.isIncompatible())
                classIncompatibilities.add(results);
        }

        if (!classIncompatibilities.isEmpty()) {
            int count = 0;
            for (ClassInfoComparer.Results compareResults : classIncompatibilities) {
                count += compareResults.getIncompatibilities().size();
            }
            logError("Incompatibilities found: " + count);
            for (ClassInfoComparer.Results compareResults : classIncompatibilities) {
                logError(compareResults.className + ":");
                for (String incompatibility : compareResults.getIncompatibilities()) {
                    logError("- " + incompatibility);
                }
            }

            return count;
        }

        return 0;
    }
}
