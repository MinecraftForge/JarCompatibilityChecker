/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.data;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;

import java.lang.reflect.Field;

public class FieldInfo {
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

    public String getNameDesc() {
        return this.name + ':' + this.desc;
    }
}
