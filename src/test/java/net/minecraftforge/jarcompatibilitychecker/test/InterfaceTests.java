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
    public void testInterfaceOrder() {
        assertCompatible(true, "InterfaceOrder", "A");
    }

    @Test
    public void testMissingInterface() {
        assertIncompatible(true, "MissingInterface", "A", "A", null, IncompatibilityMessages.CLASS_MISSING_INTERFACE, "C");
    }
}
