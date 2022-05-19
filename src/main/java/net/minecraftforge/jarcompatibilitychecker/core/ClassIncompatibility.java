/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import org.jetbrains.annotations.NotNull;

public class ClassIncompatibility implements Incompatibility<ClassInfo> {
    private final ClassInfo classInfo;
    private final String message;

    public ClassIncompatibility(ClassInfo classInfo, String message) {
        this.classInfo = classInfo;
        this.message = message;
    }

    @NotNull
    @Override
    public ClassInfo getInfo() {
        return this.classInfo;
    }

    @NotNull
    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
