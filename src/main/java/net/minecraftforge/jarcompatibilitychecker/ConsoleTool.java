/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker;

import com.google.common.collect.ImmutableList;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ConsoleTool {
    public static void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        OptionSpec<Void> apiO = parser.accepts("api", "Enables the API compatibility checking mode");
        OptionSpec<Void> binaryO = parser.accepts("binary", "Enables the binary compatibility checking mode. This option will override the API compatibility flag. Defaults to true.");
        OptionSpec<File> baseJarO  = parser.accepts("base-jar",  "Base JAR file that will be matched against for compatibility").withRequiredArg().ofType(File.class).required();
        OptionSpec<File> inputJarO  = parser.accepts("input-jar",  "JAR file to validate against the base JAR").withRequiredArg().ofType(File.class).required();
        OptionSpec<File> libO = parser.acceptsAll(ImmutableList.of("lib", "library"), "Libraries that the base JAR and input JAR both use").withRequiredArg().ofType(File.class);
        OptionSpec<File> baseLibO = parser.acceptsAll(ImmutableList.of("base-lib", "base-library"), "Libraries that only the base JAR uses").withRequiredArg().ofType(File.class);
        OptionSpec<File> concreteLibO = parser.acceptsAll(ImmutableList.of("concrete-lib", "concrete-library"), "Libraries that only the input JAR uses").withRequiredArg().ofType(File.class);

        OptionSet options;
        try {
            options = parser.parse(args);
        } catch (OptionException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.err.println();
            parser.printHelpOn(System.err);
            System.exit(1);
            return;
        }

        File baseJar = options.valueOf(baseJarO);
        File inputJar = options.valueOf(inputJarO);
        List<File> commonLibs = options.valuesOf(libO);
        List<File> baseLibs = options.valuesOf(baseLibO);
        List<File> concreteLibs = options.valuesOf(concreteLibO);
        boolean checkBinary = !options.has(apiO) || options.has(binaryO);

        // TODO allow logging to a file
        JarCompatibilityChecker checker = new JarCompatibilityChecker(baseJar, inputJar, checkBinary, commonLibs, baseLibs, concreteLibs, System.out::println, System.err::println);

        System.exit(checker.check());
    }
}
