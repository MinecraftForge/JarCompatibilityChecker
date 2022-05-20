/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.IncompatibilityMessages;
import org.junit.Test;

import java.nio.file.Path;

public class ClassTests extends BaseCompatibilityTest {
    @Override
    protected Path getRoot() {
        return super.getRoot().resolve("Class");
    }

    @Test
    public void testApiLoweredClassVisibility() {
        // Lowering visibility of a public class to package-private is API incompatible
        assertClassIncompatible(false, "LoweredClassVisibility", "A", IncompatibilityMessages.CLASS_LOWERED_VISIBILITY);
    }

    @Test
    public void testLoweredClassVisibility() {
        // Lowering visibility of a public class to package-private is binary incompatible
        assertClassIncompatible(true, "LoweredClassVisibility", "A", IncompatibilityMessages.CLASS_LOWERED_VISIBILITY);
    }

    @Test
    public void testApiMissingPackagePrivateClass() {
        // Removing a package-private class is API compatible
        assertCompatible(false, "MissingPackagePrivateClass", "A");
    }

    @Test
    public void testMissingPackagePrivateClass() {
        // Removing a package-private class is binary incompatible
        assertClassIncompatible(true, "MissingPackagePrivateClass", "A", IncompatibilityMessages.CLASS_MISSING);
    }

    @Test
    public void testApiMissingPublicClass() {
        // Removing a public class is API incompatible
        assertClassIncompatible(false, "MissingPublicClass", "A", IncompatibilityMessages.API_CLASS_MISSING);
    }

    @Test
    public void testMissingPublicClass() {
        // Removing a public class is binary incompatible
        assertClassIncompatible(true, "MissingPublicClass", "A", IncompatibilityMessages.CLASS_MISSING);
    }
}
