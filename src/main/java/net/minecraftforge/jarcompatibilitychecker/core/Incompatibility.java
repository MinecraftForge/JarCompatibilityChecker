/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.MemberInfo;
import org.jetbrains.annotations.NotNull;

public interface Incompatibility<I extends MemberInfo> {
    @NotNull
    I getInfo();

    @NotNull
    String getMessage();

    default boolean isError() {
        return true;
    }
}
