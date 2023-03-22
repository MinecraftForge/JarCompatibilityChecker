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
    private final boolean isError;

    public MethodIncompatibility(MethodInfo methodInfo, String message) {
        this(methodInfo, message, true);
    }

    public MethodIncompatibility(MethodInfo methodInfo, String message, boolean isError) {
        this.methodInfo = methodInfo;
        this.message = message;
        this.isError = isError;
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
    public boolean isError() {
        return this.isError;
    }

    @Override
    public String toString() {
        return this.methodInfo + " - " + this.message;
    }
}
