/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.data;

import org.jetbrains.annotations.Nullable;

public interface MemberInfo {
    String getName();

    @Nullable
    String getDescriptor();

    int getAccess();
}
