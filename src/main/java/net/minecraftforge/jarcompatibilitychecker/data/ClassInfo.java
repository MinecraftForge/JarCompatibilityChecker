/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ClassInfo implements MemberInfo {
    public final String name;
    public final int access;
    public final String superName;
    private final List<String> interfaces;
    private final Map<String, MethodInfo> methods;
    private final Map<String, FieldInfo> fields;

    private static Map<String, MethodInfo> makeMap(List<MethodInfo> lst) {
        if (lst.isEmpty())
            return null;
        Map<String, MethodInfo> ret = new HashMap<>();
        lst.forEach(info -> ret.put(info.name + " " + info.desc, info));
        return ret;
    }

    public ClassInfo(ClassNode node) {
        this.name = node.name;
        this.access = node.access;
        this.superName = node.superName;
        this.interfaces = node.interfaces.isEmpty() ? null : node.interfaces;

        List<MethodInfo> lst = new ArrayList<>();
        if (!node.methods.isEmpty())
            node.methods.forEach(mn -> lst.add(new MethodInfo(this, mn)));
        this.methods = makeMap(lst);

        if (!node.fields.isEmpty())
            this.fields = node.fields.stream().map(FieldInfo::new).collect(Collectors.toMap(e -> e.name, e -> e));
        else
            this.fields = null;
    }

    public ClassInfo(Class<?> clazz) {
        this.name = clazz.getName().replace('.', '/');
        this.access = clazz.getModifiers();
        this.superName = clazz.getSuperclass() == null ? null : clazz.getSuperclass().getName().replace('.', '/');
        List<String> intfs = new ArrayList<>();
        for (Class<?> i : clazz.getInterfaces())
            intfs.add(i.getName().replace('.', '/'));
        this.interfaces = intfs.isEmpty() ? null : intfs;

        List<MethodInfo> mtds = new ArrayList<>();

        for (Constructor<?> ctr : clazz.getConstructors())
            mtds.add(new MethodInfo(this, ctr));

        for (Method mtd : clazz.getDeclaredMethods())
            mtds.add(new MethodInfo(this, mtd));

        this.methods = makeMap(mtds);

        Field[] flds = clazz.getDeclaredFields();
        if (flds.length > 0)
            this.fields = Arrays.stream(flds).map(FieldInfo::new).collect(Collectors.toMap(e -> e.name, e -> e));
        else
            this.fields = null;
    }

    @NotNull
    public List<String> getInterfaces() {
        return this.interfaces == null ? ImmutableList.of() : this.interfaces;
    }

    @NotNull
    public Map<String, MethodInfo> getMethods() {
        return this.methods == null ? ImmutableMap.of() : this.methods;
    }

    @Nullable
    public MethodInfo getMethod(String name, String desc) {
        return this.methods == null ? null : this.methods.get(name + " " + desc);
    }

    @NotNull
    public Map<String, FieldInfo> getFields() {
        return this.fields == null ? ImmutableMap.of() : this.fields;
    }

    @Nullable
    public FieldInfo getField(String name) {
        return this.fields == null ? null : this.fields.get(name);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Nullable
    @Override
    public String getDescriptor() {
        return null;
    }

    @Override
    public int getAccess() {
        return this.access;
    }
}
