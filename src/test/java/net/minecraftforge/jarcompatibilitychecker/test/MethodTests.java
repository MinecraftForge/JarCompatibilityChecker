/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.IncompatibilityMessages;
import org.junit.Test;

import java.nio.file.Path;

public class MethodTests extends BaseCompatibilityTest {
    @Override
    protected Path getRoot() {
        return super.getRoot().resolve("Method");
    }

    @Test
    public void testApiLoweredMethodVisibility() {
        // Lowering visibility of a public method to private is API incompatible
        assertIncompatible(false, "LoweredMethodVisibility", "A", "foo", "()V", IncompatibilityMessages.METHOD_LOWERED_VISIBILITY);
    }

    @Test
    public void testLoweredMethodVisibility() {
        // Lowering visibility of a public method to private is binary incompatible
        assertIncompatible(true, "LoweredMethodVisibility", "A", "foo", "()V", IncompatibilityMessages.METHOD_LOWERED_VISIBILITY);
    }

    @Test
    public void testApiNewMethod() {
        // Adding a new method is API compatible
        assertCompatible(false, "NewMethod", "A");
    }

    @Test
    public void testNewMethod() {
        // Adding a new method is binary compatible
        assertCompatible(true, "NewMethod", "A");
    }

    @Test
    public void testApiRemovedPrivateMethod() {
        // Removing a private method is API compatible
        assertCompatible(false, "RemovedPrivateMethod", "A");
    }

    @Test
    public void testRemovedPrivateMethod() {
        // Removing a private method is binary incompatible
        assertIncompatible(true, "RemovedPrivateMethod", "A", "foo", "()V", IncompatibilityMessages.METHOD_REMOVED);
    }

    @Test
    public void testApiRemovedPublicMethod() {
        // Removing a public method is API incompatible
        assertIncompatible(false, "RemovedPublicMethod", "A", "foo", "()V", IncompatibilityMessages.API_METHOD_REMOVED);
    }

    @Test
    public void testRemovedPublicMethod() {
        // Removing a public method is binary incompatible
        assertIncompatible(true, "RemovedPublicMethod", "A", "foo", "()V", IncompatibilityMessages.METHOD_REMOVED);
    }
}
