/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.IncompatibilityMessages;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

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
        // Adding a new non-abstract method is API compatible
        assertCompatible(false, "NewMethod", "A");
    }

    @Test
    public void testNewMethod() {
        // Adding a new non-abstract method is binary compatible
        assertCompatible(true, "NewMethod", "A");
    }

    @Test
    public void testApiPackagePrivateMethodMadeAbstract() {
        // Making a package-private method abstract in a public abstract class is API incompatible due to potential issues with external implementations of that class
        assertIncompatible(false, "PackagePrivateMethodMadeAbstract", "A", "thing", "()V", IncompatibilityMessages.METHOD_MADE_ABSTRACT);
    }

    @Test
    public void testPackagePrivateMethodMadeAbstract() {
        // Making a package-private method abstract in a public abstract class is binary incompatible
        assertIncompatible(true, "PackagePrivateMethodMadeAbstract", "A", "thing", "()V", IncompatibilityMessages.METHOD_MADE_ABSTRACT);
    }

    @Test
    public void testApiPrivateMethodMadeFinal() {
        // Making a private method final is API compatible
        assertCompatible(false, "PrivateMethodMadeFinal", "A");
    }

    @Test
    public void testPrivateMethodMadeFinal() {
        // Making a private method final is binary incompatible
        assertIncompatible(true, "PrivateMethodMadeFinal", "A", "thing", "()V", IncompatibilityMessages.METHOD_MADE_FINAL);
    }

    @Test
    public void testApiPublicMethodMadeAbstract() {
        // Making a public method abstract in a public abstract class is API incompatible
        assertIncompatible(false, "PublicMethodMadeAbstract", "A", "thing", "()V", IncompatibilityMessages.METHOD_MADE_ABSTRACT);
    }

    @Test
    public void testPublicMethodMadeAbstract() {
        // Making a public method abstract in a public abstract class is binary incompatible
        assertIncompatible(true, "PublicMethodMadeAbstract", "A", "thing", "()V", IncompatibilityMessages.METHOD_MADE_ABSTRACT);
    }

    @Test
    public void testApiPublicMethodMadeFinal() {
        // Making a public method final is API incompatible
        assertIncompatible(false, "PublicMethodMadeFinal", "A", "thing", "()V", IncompatibilityMessages.METHOD_MADE_FINAL);
    }

    @Test
    public void testPublicMethodMadeFinal() {
        // Making a public method final is binary incompatible
        assertIncompatible(true, "PublicMethodMadeFinal", "A", "thing", "()V", IncompatibilityMessages.METHOD_MADE_FINAL);
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
