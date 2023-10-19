/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker;

import net.minecraftforge.jarcompatibilitychecker.core.AnnotationCheckMode;
import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoCache;
import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoComparer;
import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoComparisonResults;
import net.minecraftforge.jarcompatibilitychecker.core.Incompatibility;
import net.minecraftforge.jarcompatibilitychecker.core.InternalAnnotationCheckMode;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JarCompatibilityChecker {
    private final File baseJar;
    private final File inputJar;
    private final boolean checkBinary;
    @Nullable
    private final AnnotationCheckMode annotationCheckMode;
    private final List<String> internalAnnotations;
    private final InternalAnnotationCheckMode internalAnnotationCheckMode;
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
    public JarCompatibilityChecker(File baseJar, File inputJar, boolean checkBinary, List<File> commonLibs, List<File> baseLibs, List<File> concreteLibs,
            Consumer<String> stdLogger, Consumer<String> errLogger) {
        this(baseJar, inputJar, checkBinary, null, commonLibs, baseLibs, concreteLibs, stdLogger, errLogger);
    }

    /**
     * Constructs a new JarCompatibilityChecker.
     *
     * @param checkBinary if {@code true}, all members of the base jar including package-private and private will be checked for a match in the input jar.
     * Otherwise, only public and protected members of the base jar will be checked for a match in the input jar.
     * @param annotationCheckMode determines whether annotations will be checked and if a mismatch is an error condition
     */
    public JarCompatibilityChecker(File baseJar, File inputJar, boolean checkBinary, @Nullable AnnotationCheckMode annotationCheckMode,
            List<File> commonLibs, List<File> baseLibs, List<File> concreteLibs, Consumer<String> stdLogger, Consumer<String> errLogger) {
        this(baseJar, inputJar, checkBinary, annotationCheckMode, InternalAnnotationCheckMode.DEFAULT_INTERNAL_ANNOTATIONS, InternalAnnotationCheckMode.DEFAULT_MODE,
                commonLibs, baseLibs, concreteLibs, stdLogger, errLogger);
    }

    /**
     * Constructs a new JarCompatibilityChecker.
     *
     * @param checkBinary if {@code true}, all members of the base jar including package-private and private will be checked for a match in the input jar.
     * Otherwise, only public and protected members of the base jar will be checked for a match in the input jar.
     * @param annotationCheckMode determines whether annotations will be checked and if a mismatch is an error condition
     * @param internalAnnotations a list of fully resolved classnames for annotations that can be used to mark elements as internal API
     * @param internalAnnotationCheckMode determines how internally-marked elements will be checked
     */
    public JarCompatibilityChecker(File baseJar, File inputJar, boolean checkBinary, @Nullable AnnotationCheckMode annotationCheckMode, List<String> internalAnnotations,
            InternalAnnotationCheckMode internalAnnotationCheckMode, List<File> commonLibs, List<File> baseLibs, List<File> concreteLibs, Consumer<String> stdLogger, Consumer<String> errLogger) {
        this.baseJar = baseJar;
        this.inputJar = inputJar;
        this.checkBinary = checkBinary;
        this.annotationCheckMode = annotationCheckMode;
        this.internalAnnotations = internalAnnotations == InternalAnnotationCheckMode.DEFAULT_INTERNAL_ANNOTATIONS ? internalAnnotations : internalAnnotations.stream().map(s -> {
            boolean inDescForm = s.indexOf(';') == s.length() - 1;
            return inDescForm ? s.replace('.', '/') : 'L' + s.replace('.', '/') + ';';
        }).collect(Collectors.toList());
        this.internalAnnotationCheckMode = internalAnnotationCheckMode;
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
        log("Annotation check mode: " + (this.annotationCheckMode == null ? "NONE" : this.annotationCheckMode));
        log("Internal API annotation check mode: " + this.internalAnnotationCheckMode);
        log("Internal API annotations: " + this.internalAnnotations);
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
        List<ClassInfoComparisonResults> classIncompatibilities = new ArrayList<>();

        for (Map.Entry<String, ClassInfo> baseEntry : baseCache.getMainClasses().entrySet()) {
            String baseClassName = baseEntry.getKey();
            ClassInfo baseClassInfo = baseEntry.getValue();
            ClassInfo concreteClassInfo = concreteCache.getMainClassInfo(baseClassName);

            // log("Comparing " + baseClassName);
            ClassInfoComparisonResults results = ClassInfoComparer.compare(this.checkBinary, this.annotationCheckMode, this.internalAnnotations, this.internalAnnotationCheckMode,
                    baseCache, baseClassInfo, concreteCache, concreteClassInfo);
            if (results.isIncompatible())
                classIncompatibilities.add(results);
        }

        if (!classIncompatibilities.isEmpty()) {
            int errorCount = 0;
            int warningCount = 0;
            for (ClassInfoComparisonResults compareResults : classIncompatibilities) {
                for (Incompatibility<?> incompatibility : compareResults.getIncompatibilities()) {
                    if (incompatibility.isError()) {
                        errorCount++;
                    } else {
                        warningCount++;
                    }
                }
            }

            logError("Incompatibilities found: " + errorCount + " errors, " + warningCount + " warnings");

            for (ClassInfoComparisonResults compareResults : classIncompatibilities) {
                logError(compareResults.classInfo.name + ":");
                for (Incompatibility<?> incompatibility : compareResults.getIncompatibilities()) {
                    logError("- " + (incompatibility.isError() ? "error: " : "warning: ") + incompatibility);
                }
            }

            return errorCount;
        } else {
            log("No incompatibilities found");
        }

        return 0;
    }
}
