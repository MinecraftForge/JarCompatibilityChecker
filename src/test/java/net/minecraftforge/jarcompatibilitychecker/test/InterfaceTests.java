/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.IncompatibilityMessages;
import org.junit.Test;

import java.nio.file.Path;

public class InterfaceTests extends BaseCompatibilityTest {
    @Override
    protected Path getRoot() {
        return super.getRoot().resolve("Interface");
    }

    @Test
    public void testApiInterfaceOrder() {
        // Interface order should not affect API compatibility
        assertCompatible(false, "InterfaceOrder", "A");
    }

    @Test
    public void testInterfaceOrder() {
        // Interface order should not affect binary compatibility
        assertCompatible(true, "InterfaceOrder", "A");
    }

    @Test
    public void testApiMissingPackagePrivateInterface() {
        // A missing package-private interface is API compatible
        assertCompatible(false, "MissingPackagePrivateInterface", "A");
    }

    @Test
    public void testMissingPackagePrivateInterface() {
        // A missing package-private interface is binary incompatible
        assertClassIncompatible(true, "MissingPackagePrivateInterface", "A", IncompatibilityMessages.CLASS_MISSING_INTERFACE, "B");
    }

    @Test
    public void testApiMissingPublicInterface() {
        // A missing public interface is API incompatible
        assertClassIncompatible(false, "MissingPublicInterface", "A", IncompatibilityMessages.CLASS_MISSING_INTERFACE, "B");
    }

    @Test
    public void testMissingPublicInterface() {
        // A missing public interface is binary incompatible
        assertClassIncompatible(true, "MissingPublicInterface", "A", IncompatibilityMessages.CLASS_MISSING_INTERFACE, "B");
    }

    @Test
    public void testApiNewInterface() {
        // A new interface is API compatible
        assertCompatible(false, "NewInterface", "A");
    }

    @Test
    public void testNewInterface() {
        // A new interface is binary compatible
        assertCompatible(true, "NewInterface", "A");
    }
}
