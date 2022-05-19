/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.MethodInfo;
import org.jetbrains.annotations.NotNull;

public class MethodIncompatibility implements Incompatibility<MethodInfo> {
    private final MethodInfo methodInfo;
    private final String message;

    public MethodIncompatibility(MethodInfo methodInfo, String message) {
        this.methodInfo = methodInfo;
        this.message = message;
    }

    @NotNull
    @Override
    public MethodInfo getInfo() {
        return this.methodInfo;
    }

    @NotNull
    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.methodInfo.getNameDesc() + " - " + this.message;
    }
}
