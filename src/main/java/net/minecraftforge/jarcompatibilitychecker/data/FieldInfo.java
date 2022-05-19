/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.data;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;

public class FieldInfo implements MemberInfo {
    public final String name;
    public final String desc;
    public final int access;

    public FieldInfo(FieldNode node) {
        this.name = node.name;
        this.desc = node.desc;
        this.access = node.access;
    }

    public FieldInfo(Field node) {
        this.name = node.getName();
        this.desc = Type.getType(node.getType()).getDescriptor();
        this.access = node.getModifiers();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescriptor() {
        return this.desc;
    }

    @Override
    public int getAccess() {
        return this.access;
    }

    public String getNameDesc() {
        return this.name + ':' + this.desc;
    }
}
