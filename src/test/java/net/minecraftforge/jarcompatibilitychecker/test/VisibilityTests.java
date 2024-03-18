/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.test;

import net.minecraftforge.jarcompatibilitychecker.core.ClassInfoComparer;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

import static org.junit.jupiter.api.Assertions.*;

public class VisibilityTests {
    @Test
    public void testBinaryPublicPublic() {
        // Public -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PUBLIC, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testBinaryProtectedPublic() {
        // Protected -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PROTECTED, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testBinaryPackagePrivatePublic() {
        // Package-private -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, 0, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testBinaryPrivatePublic() {
        // Private -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PRIVATE, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testBinaryPublicProtected() {
        // Public -> protected is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PUBLIC, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testBinaryProtectedProtected() {
        // Protected -> protected is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PROTECTED, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testBinaryPackagePrivateProtected() {
        // Package-private -> protected is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, 0, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testBinaryPrivateProtected() {
        // Private -> protected is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PRIVATE, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testBinaryPublicPackagePrivate() {
        // Public -> package-private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PUBLIC, 0));
    }

    @Test
    public void testBinaryProtectedPackagePrivate() {
        // Protected -> package-private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PROTECTED, 0));
    }

    @Test
    public void testBinaryPackagePrivatePackagePrivate() {
        // Package-private -> package-private is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, 0, 0));
    }

    @Test
    public void testBinaryPrivatePackagePrivate() {
        // Private -> package-private is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PRIVATE, 0));
    }

    @Test
    public void testBinaryPublicPrivate() {
        // Public -> private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PUBLIC, Opcodes.ACC_PRIVATE));
    }

    @Test
    public void testBinaryProtectedPrivate() {
        // Protected -> private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PROTECTED, Opcodes.ACC_PRIVATE));
    }

    @Test
    public void testBinaryPackagePrivatePrivate() {
        // Package-private -> private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(true, 0, Opcodes.ACC_PRIVATE));
    }

    @Test
    public void testBinaryPrivatePrivate() {
        // Private -> private is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(true, Opcodes.ACC_PRIVATE, Opcodes.ACC_PRIVATE));
    }

    @Test
    public void testApiPublicPublic() {
        // Public -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PUBLIC, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testApiProtectedPublic() {
        // Protected -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PROTECTED, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testApiPackagePrivatePublic() {
        // Package-private -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, 0, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testApiPrivatePublic() {
        // Private -> public is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PRIVATE, Opcodes.ACC_PUBLIC));
    }

    @Test
    public void testApiPublicProtected() {
        // Public -> protected is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PUBLIC, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testApiProtectedProtected() {
        // Protected -> protected is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PROTECTED, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testApiPackagePrivateProtected() {
        // Package-private -> protected is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, 0, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testApiPrivateProtected() {
        // Private -> protected is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PRIVATE, Opcodes.ACC_PROTECTED));
    }

    @Test
    public void testApiPublicPackagePrivate() {
        // Public -> package-private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PUBLIC, 0));
    }

    @Test
    public void testApiProtectedPackagePrivate() {
        // Protected -> package-private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PROTECTED, 0));
    }

    @Test
    public void testApiPackagePrivatePackagePrivate() {
        // Package-private -> package-private is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, 0, 0));
    }

    @Test
    public void testApiPrivatePackagePrivate() {
        // Private -> package-private is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PRIVATE, 0));
    }

    @Test
    public void testApiPublicPrivate() {
        // Public -> private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PUBLIC, Opcodes.ACC_PRIVATE));
    }

    @Test
    public void testApiProtectedPrivate() {
        // Protected -> private is lowered
        assertTrue(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PROTECTED, Opcodes.ACC_PRIVATE));
    }

    @Test
    public void testApiPackagePrivatePrivate() {
        // Package-private -> private is not lowered for API compatibility
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, 0, Opcodes.ACC_PRIVATE));
    }

    @Test
    public void testApiPrivatePrivate() {
        // Private -> private is not lowered
        assertFalse(ClassInfoComparer.isVisibilityLowered(false, Opcodes.ACC_PRIVATE, Opcodes.ACC_PRIVATE));
    }
}
