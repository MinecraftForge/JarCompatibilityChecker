/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;

public class ClassIncompatibility extends BaseIncompatibility<ClassInfo> {
    public ClassIncompatibility(ClassInfo classInfo, String message, boolean isError) {
        super(classInfo, message, isError);
    }
}
