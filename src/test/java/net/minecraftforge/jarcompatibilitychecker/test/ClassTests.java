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
    public void testInternalClassDeleted() {
        // Removing a class marked with an internal annotation should produce a warning by default but still be considered API/binary compatible
        assertClassIncompatible(true, "InternalClassDeleted", "A", false, IncompatibilityMessages.CLASS_MISSING);
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

    @Test
    public void testApiPackagePrivateClassMadeAbstract() {
        // Making a package-private class abstract is API compatible
        assertCompatible(false, "PackagePrivateClassMadeAbstract", "A");
    }

    @Test
    public void testPackagePrivateClassMadeAbstract() {
        // Making a package-private class abstract is binary incompatible
        assertClassIncompatible(true, "PackagePrivateClassMadeAbstract", "A", IncompatibilityMessages.CLASS_MADE_ABSTRACT);
    }

    @Test
    public void testApiPackagePrivateClassMadeFinal() {
        // Making a package-private class final is API compatible
        assertCompatible(false, "PackagePrivateClassMadeFinal", "A");
    }

    @Test
    public void testPackagePrivateClassMadeFinal() {
        // Making a package-private class final is binary incompatible
        assertClassIncompatible(true, "PackagePrivateClassMadeFinal", "A", IncompatibilityMessages.CLASS_MADE_FINAL);
    }

    @Test
    public void testApiPublicClassMadeAbstract() {
        // Making a public class abstract is API incompatible
        assertClassIncompatible(false, "PublicClassMadeAbstract", "A", IncompatibilityMessages.CLASS_MADE_ABSTRACT);
    }

    @Test
    public void testPublicClassMadeAbstract() {
        // Making a public class abstract is binary incompatible
        assertClassIncompatible(true, "PublicClassMadeAbstract", "A", IncompatibilityMessages.CLASS_MADE_ABSTRACT);
    }

    @Test
    public void testApiPublicClassMadeFinal() {
        // Making a public class final is API incompatible
        assertClassIncompatible(false, "PublicClassMadeFinal", "A", IncompatibilityMessages.CLASS_MADE_FINAL);
    }

    @Test
    public void testPublicClassMadeFinal() {
        // Making a public class final is binary incompatible
        assertClassIncompatible(true, "PublicClassMadeFinal", "A", IncompatibilityMessages.CLASS_MADE_FINAL);
    }
}
