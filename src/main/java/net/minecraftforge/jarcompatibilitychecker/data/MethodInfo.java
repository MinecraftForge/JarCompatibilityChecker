/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.data;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class MethodInfo implements MemberInfo {
    public final String name;
    public final String desc;
    public final int access;
    public final List<String> exceptions;
    public final ClassInfo parent;
    public final Bouncer bouncer;
    private String override = null;

    public MethodInfo(ClassInfo parent, MethodNode node) {
        this.name = node.name;
        this.desc = node.desc;
        this.access = node.access;
        this.exceptions = node.exceptions.isEmpty() ? null : new ArrayList<>(node.exceptions);
        this.parent = parent;
        this.bouncer = getBouncer(parent, node);
    }

    private static Bouncer getBouncer(ClassInfo parent, MethodNode node) {
        if ((node.access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) == 0 || (node.access & Opcodes.ACC_STATIC) != 0)
            return null;

        AbstractInsnNode start = node.instructions.getFirst();
        if (start instanceof LabelNode && start.getNext() instanceof LineNumberNode)
            start = start.getNext().getNext();

        if (start instanceof VarInsnNode && ((VarInsnNode) start).var == 0 && start.getOpcode() == Opcodes.ALOAD) {
            AbstractInsnNode end = node.instructions.getLast();
            if (end instanceof LabelNode)
                end = end.getPrevious();

            if (end.getOpcode() >= Opcodes.IRETURN && end.getOpcode() <= Opcodes.RETURN)
                end = end.getPrevious();

            if (end instanceof MethodInsnNode) {
                MethodInsnNode mtd = (MethodInsnNode) end;
                while (start != end) {
                    if (!(start instanceof VarInsnNode) && start.getOpcode() != Opcodes.INSTANCEOF && start.getOpcode() != Opcodes.CHECKCAST) {
                        end = null;
                        break;
                            /* We're in a lambda. so lets exit.
                            System.out.println("Bounce? " + parent.name + "/" + name + desc);
                            for (AbstractInsnNode asn : node.instructions.toArray())
                                System.out.println("  " + asn);
                            */
                    }
                    start = start.getNext();
                }

                if (end != null && mtd.owner.equals(parent.name) &&
                        Type.getArgumentsAndReturnSizes(node.desc) == Type.getArgumentsAndReturnSizes(mtd.desc)) {
                    return new Bouncer(mtd.name, mtd.desc);
                }
            }
        }

        return null;
    }

    public MethodInfo(ClassInfo parent, Method method) {
        this.name = method.getName();
        this.desc = Type.getMethodDescriptor(method);
        this.access = method.getModifiers();
        List<String> execs = new ArrayList<>();
        for (Class<?> e : method.getExceptionTypes())
            execs.add(e.getName().replace('.', '/'));
        this.exceptions = execs.isEmpty() ? null : execs;
        this.parent = parent;
        this.bouncer = null;
    }

    public MethodInfo(ClassInfo parent, Constructor<?> constructor) {
        this.name = "<init>";
        this.desc = Type.getConstructorDescriptor(constructor);
        this.access = constructor.getModifiers();
        List<String> execs = new ArrayList<>();
        for (Class<?> e : constructor.getExceptionTypes())
            execs.add(e.getName().replace('.', '/'));
        this.exceptions = execs.isEmpty() ? null : execs;
        this.parent = parent;
        this.bouncer = null;
    }

    public void setOverride(String override) {
        this.override = override;
    }

    public String getOverride() {
        return this.override;
    }

    public boolean hasOverride() {
        return this.override != null;
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
        return this.name + this.desc;
    }

    @Override
    public String toString() {
        return this.parent.name + "/" + this.name + this.desc;
    }
}
