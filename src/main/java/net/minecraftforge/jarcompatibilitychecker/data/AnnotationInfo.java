/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.data;

import com.google.common.collect.ImmutableList;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class AnnotationInfo {
    public final String desc;
    public final List<Object> values;
    private String cachedToString;

    @SafeVarargs
    public static List<AnnotationInfo> create(List<AnnotationNode>... annotationLists) {
        List<AnnotationInfo> annotationInfos = new ArrayList<>();

        for (List<AnnotationNode> annotations : annotationLists) {
            if (annotations == null)
                continue;
            for (AnnotationNode annotationNode : annotations) {
                annotationInfos.add(new AnnotationInfo(annotationNode.desc, annotationNode.values));
            }
        }

        return annotationInfos;
    }

    public AnnotationInfo(String desc, List<Object> values) {
        this.desc = desc;
        this.values = values == null ? ImmutableList.of() : values;
    }

    @Override
    public String toString() {
        if (this.cachedToString == null) {
            StringBuilder builder = new StringBuilder();
            builder.append('@').append(desc.replace('/', '.'), 1, desc.length() - 1).append('(');

            boolean name = true;
            int size = values.size();
            for (int i = 0; i < size; i++) {
                Object value = values.get(i);
                if (name) {
                    builder.append(value).append('=');
                } else if (value instanceof long[]) {
                    builder.append(Arrays.toString((long[]) value));
                } else if (value instanceof int[]) {
                    builder.append(Arrays.toString((int[]) value));
                } else if (value instanceof short[]) {
                    builder.append(Arrays.toString((short[]) value));
                } else if (value instanceof char[]) {
                    builder.append(Arrays.toString((char[]) value));
                } else if (value instanceof byte[]) {
                    builder.append(Arrays.toString((byte[]) value));
                } else if (value instanceof boolean[]) {
                    builder.append(Arrays.toString((boolean[]) value));
                } else if (value instanceof float[]) {
                    builder.append(Arrays.toString((float[]) value));
                } else if (value instanceof double[]) {
                    builder.append(Arrays.toString((double[]) value));
                } else if (value instanceof Object[]) {
                    builder.append(Arrays.toString((Object[]) value));
                } else {
                    builder.append(value);
                }

                if (!name && i < size - 1)
                    builder.append(", ");

                name = !name;
            }

            builder.append(')');
            this.cachedToString = builder.toString();
        }

        return this.cachedToString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        AnnotationInfo that = (AnnotationInfo) o;
        return desc.equals(that.desc) && values.equals(that.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(desc, values);
    }
}
