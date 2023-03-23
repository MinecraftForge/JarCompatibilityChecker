/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.MemberInfo;
import org.jetbrains.annotations.NotNull;

class BaseIncompatibility<I extends MemberInfo> implements Incompatibility<I> {
    final I memberInfo;
    final String message;
    final boolean isError;

    public BaseIncompatibility(I memberInfo, String message, boolean isError) {
        this.memberInfo = memberInfo;
        this.message = message;
        this.isError = isError;
    }

    @NotNull
    @Override
    public I getInfo() {
        return this.memberInfo;
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
        return this.message;
    }
}
