/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MemberInfo {
    @NotNull
    String getName();

    @Nullable
    String getDescriptor();

    int getAccess();

    @NotNull
    List<AnnotationInfo> getAnnotations();
}
