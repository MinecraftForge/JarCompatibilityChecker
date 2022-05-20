/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.IncompatibilityMessages;
import org.junit.Test;

import java.nio.file.Path;

public class FieldTests extends BaseCompatibilityTest {
    @Override
    protected Path getRoot() {
        return super.getRoot().resolve("Field");
    }

    @Test
    public void testApiLoweredFieldVisibility() {
        // Lowering visibility of a public field to private is API incompatible
        assertIncompatible(false, "LoweredFieldVisibility", "A", "buzz", "Z", IncompatibilityMessages.FIELD_LOWERED_VISIBILITY);
    }

    @Test
    public void testLoweredFieldVisibility() {
        // Lowering visibility of a public field to private is binary incompatible
        assertIncompatible(true, "LoweredFieldVisibility", "A", "buzz", "Z", IncompatibilityMessages.FIELD_LOWERED_VISIBILITY);
    }

    @Test
    public void testApiNewField() {
        // Adding a new field is API compatible
        assertCompatible(false, "NewField", "A");
    }

    @Test
    public void testNewField() {
        // Adding a new field is binary compatible
        assertCompatible(true, "NewField", "A");
    }

    @Test
    public void testApiRemovedPrivateField() {
        // Removing a private field is API compatible
        assertCompatible(false, "RemovedPrivateField", "A");
    }

    @Test
    public void testRemovedPrivateField() {
        // Removing a private field is binary incompatible
        assertIncompatible(true, "RemovedPrivateField", "A", "buzz", "Z", IncompatibilityMessages.FIELD_REMOVED);
    }

    @Test
    public void testApiRemovedPublicField() {
        // Removing a public field is API incompatible
        assertIncompatible(false, "RemovedPublicField", "A", "buzz", "Z", IncompatibilityMessages.API_FIELD_REMOVED);
    }

    @Test
    public void testRemovedPublicField() {
        // Removing a public field is binary incompatible
        assertIncompatible(true, "RemovedPublicField", "A", "buzz", "Z", IncompatibilityMessages.FIELD_REMOVED);
    }
}
