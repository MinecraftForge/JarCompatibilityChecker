/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.core;

import net.minecraftforge.jarcompatibilitychecker.data.AnnotationInfo;
import net.minecraftforge.jarcompatibilitychecker.data.MemberInfo;
import org.jetbrains.annotations.NotNull;

public class AnnotationIncompatibility<I extends MemberInfo> implements Incompatibility<I> {
    private final I memberInfo;
    private final AnnotationInfo annotationInfo;
    private final String message;
    private final boolean isError;

    public AnnotationIncompatibility(I memberInfo, AnnotationInfo annotationInfo, String message, boolean isError) {
        this.memberInfo = memberInfo;
        this.annotationInfo = annotationInfo;
        this.message = message;
        this.isError = isError;
    }

    @NotNull
    @Override
    public I getInfo() {
        return this.memberInfo;
    }

    @NotNull
    public AnnotationInfo getAnnotationInfo() {
        return this.annotationInfo;
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
        return "Member " + this.memberInfo + " annotated with " + this.annotationInfo + " - " + this.message;
    }
}
