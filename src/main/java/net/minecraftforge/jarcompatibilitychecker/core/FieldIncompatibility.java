/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.FieldInfo;
import org.jetbrains.annotations.NotNull;

public class FieldIncompatibility implements Incompatibility<FieldInfo> {
    private final FieldInfo fieldInfo;
    private final String message;

    public FieldIncompatibility(FieldInfo fieldInfo, String message) {
        this.fieldInfo = fieldInfo;
        this.message = message;
    }

    @NotNull
    @Override
    public FieldInfo getInfo() {
        return this.fieldInfo;
    }

    @NotNull
    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public String toString() {
        return this.fieldInfo + " - " + this.message;
    }
}
