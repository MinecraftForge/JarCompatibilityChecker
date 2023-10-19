/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.core;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.jarcompatibilitychecker.data.AnnotationInfo;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import net.minecraftforge.jarcompatibilitychecker.data.FieldInfo;
import net.minecraftforge.jarcompatibilitychecker.data.MemberInfo;
import net.minecraftforge.jarcompatibilitychecker.data.MethodInfo;

import java.util.ArrayList;
import java.util.List;

public class ClassInfoComparisonResults {
    public final ClassInfo classInfo;
    private List<Incompatibility<?>> incompatibilities;

    ClassInfoComparisonResults(ClassInfo classInfo) {
        this.classInfo = classInfo;
    }

    void addIncompatibility(Incompatibility<?> incompatibility) {
        if (this.incompatibilities == null) {
            this.incompatibilities = new ArrayList<>();
        }

        this.incompatibilities.add(incompatibility);
    }

    void addClassIncompatibility(ClassInfo classInfo, String message) {
        addClassIncompatibility(classInfo, message, true);
    }

    void addClassIncompatibility(ClassInfo classInfo, String message, boolean isError) {
        addIncompatibility(new ClassIncompatibility(classInfo, message, isError));
    }

    void addMethodIncompatibility(MethodInfo methodInfo, String message) {
        addMethodIncompatibility(methodInfo, message, true);
    }

    void addMethodIncompatibility(MethodInfo methodInfo, String message, boolean isError) {
        addIncompatibility(new MethodIncompatibility(methodInfo, message, isError));
    }

    void addFieldIncompatibility(FieldInfo fieldInfo, String message) {
        addFieldIncompatibility(fieldInfo, message, true);
    }

    void addFieldIncompatibility(FieldInfo fieldInfo, String message, boolean isError) {
        addIncompatibility(new FieldIncompatibility(fieldInfo, message, isError));
    }

    <I extends MemberInfo> void addAnnotationIncompatibility(AnnotationCheckMode mode, I memberInfo, AnnotationInfo annotationInfo, String message) {
        addAnnotationIncompatibility(mode, memberInfo, annotationInfo, message, true);
    }

    <I extends MemberInfo> void addAnnotationIncompatibility(AnnotationCheckMode mode, I memberInfo, AnnotationInfo annotationInfo, String message, boolean isError) {
        addIncompatibility(new AnnotationIncompatibility<>(memberInfo, annotationInfo, message, isError && mode.shouldError()));
    }

    /**
     * @return {@code true} if there are no incompatibilities, including both errors and warnings
     */
    public boolean isCompatible() {
        return this.incompatibilities == null || this.incompatibilities.isEmpty();
    }

    /**
     * @return {@code true} if there are any incompatibilities, including both errors and warnings
     */
    public boolean isIncompatible() {
        return this.incompatibilities != null && !this.incompatibilities.isEmpty();
    }

    public List<Incompatibility<?>> getIncompatibilities() {
        return this.incompatibilities == null ? ImmutableList.of() : this.incompatibilities;
    }

    @Override
    public String toString() {
        if (this.incompatibilities == null || this.incompatibilities.isEmpty())
            return "[]";

        return this.incompatibilities.toString();
    }
}
