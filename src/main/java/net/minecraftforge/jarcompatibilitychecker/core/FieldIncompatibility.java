/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.FieldInfo;

public class FieldIncompatibility extends BaseIncompatibility<FieldInfo> {
    public FieldIncompatibility(FieldInfo fieldInfo, String message, boolean isError) {
        super(fieldInfo, message, isError);
    }

    @Override
    public String toString() {
        return this.memberInfo + " - " + this.message;
    }
}
