/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import net.minecraftforge.jarcompatibilitychecker.data.FieldInfo;
import net.minecraftforge.jarcompatibilitychecker.data.MethodInfo;
import net.minecraftforge.jarcompatibilitychecker.sort.TopologicalSort;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ClassInfoComparer {
    public static Results compare(boolean checkBinary, ClassInfoCache baseCache, ClassInfo baseClassInfo, ClassInfoCache concreteCache, @Nullable ClassInfo concreteClassInfo) {
        Results results = new Results(baseClassInfo.name);

        if (concreteClassInfo == null) {
            if (checkBinary) {
                results.addIncompatibility("Class no longer exists");
            } else if ((baseClassInfo.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0) {
                results.addIncompatibility("API class no longer exists");
            }

            // This is as far as we can get if the input class doesn't exist
            return results;
        }

        if (isVisibilityLowered(checkBinary, baseClassInfo.access, concreteClassInfo.access)) {
            results.addIncompatibility("Class was lowered in visibility");
        }

        if (baseClassInfo.superName != null && !hasSuperClass(concreteCache, concreteClassInfo, baseClassInfo.superName)) {
            results.addIncompatibility("Class missing superclass of " + baseClassInfo.superName);
        }

        Set<String> baseInterfaces = new HashSet<>(getParentClassNames(baseCache, baseClassInfo, false));
        Set<String> concreteInterfaces = new HashSet<>(getParentClassNames(concreteCache, concreteClassInfo, false));

        Set<String> missingInterfaces = Sets.difference(baseInterfaces, concreteInterfaces);
        if (!missingInterfaces.isEmpty()) {
            if (missingInterfaces.size() == 1) {
                results.addIncompatibility("Class missing interface: " + missingInterfaces.iterator().next());
            } else {
                results.addIncompatibility("Class missing interfaces: " + missingInterfaces);
            }
        }

        List<ClassInfo> concreteParents = getParentClassInfos(concreteCache, concreteClassInfo, true);

        for (MethodInfo baseInfo : baseClassInfo.getMethods().values()) {
            boolean isStatic = (baseInfo.access & Opcodes.ACC_STATIC) != 0;
            MethodInfo inputInfo = getMethodInfo(concreteClassInfo, concreteParents, isStatic, baseInfo.name, baseInfo.desc);
            boolean basePublic = (baseInfo.access & Opcodes.ACC_PUBLIC) != 0;
            boolean baseProtected = (baseInfo.access & Opcodes.ACC_PROTECTED) != 0;

            if (inputInfo == null) {
                if (checkBinary) {
                    results.addIncompatibility(baseInfo, "Method was removed");
                } else if (basePublic || baseProtected) {
                    results.addIncompatibility(baseInfo, "API method was removed");
                }

                // This is as far as we can get without any info on the concrete method
                continue;
            }

            if (isVisibilityLowered(checkBinary, baseInfo.access, inputInfo.access)) {
                results.addIncompatibility(baseInfo, "Method was lowered in visibility");
            }
        }

        for (FieldInfo baseInfo : baseClassInfo.getFields().values()) {
            boolean isStatic = (baseInfo.access & Opcodes.ACC_STATIC) != 0;
            FieldInfo inputInfo = getFieldInfo(concreteClassInfo, concreteParents, isStatic, baseInfo.name);
            boolean basePublic = (baseInfo.access & Opcodes.ACC_PUBLIC) != 0;
            boolean baseProtected = (baseInfo.access & Opcodes.ACC_PROTECTED) != 0;

            if (inputInfo == null) {
                if (checkBinary) {
                    results.addIncompatibility(baseInfo, "Field was removed");
                } else if (basePublic || baseProtected) {
                    results.addIncompatibility(baseInfo, "API field was removed");
                }

                // This is as far as we can get without any info on the concrete field
                continue;
            }

            if (isVisibilityLowered(checkBinary, baseInfo.access, inputInfo.access)) {
                results.addIncompatibility(baseInfo, "Field was lowered in visibility");
            }
        }

        return results;
    }

    public static boolean isVisibilityLowered(boolean checkBinary, int baseAccess, int inputAccess) {
        boolean basePublic = (baseAccess & Opcodes.ACC_PUBLIC) != 0;
        boolean baseProtected = (baseAccess & Opcodes.ACC_PROTECTED) != 0;
        boolean basePrivate = (baseAccess & Opcodes.ACC_PRIVATE) != 0;

        boolean inputPublic = (inputAccess & Opcodes.ACC_PUBLIC) != 0;
        boolean inputProtected = (inputAccess & Opcodes.ACC_PROTECTED) != 0;
        boolean inputPrivate = (inputAccess & Opcodes.ACC_PRIVATE) != 0;

        return (basePublic && !inputPublic) || (baseProtected && !inputProtected && !inputPublic) || (checkBinary && !basePrivate && inputPrivate);
    }

    public static boolean hasSuperClass(ClassInfoCache cache, ClassInfo classInfo, String superClass) {
        if (classInfo.superName == null)
            return false;

        do {
            if (superClass.equals(classInfo.superName))
                return true;
            classInfo = cache.getClassInfo(classInfo.superName);
        } while (classInfo.superName != null);

        return false;
    }

    @Nullable
    public static MethodInfo getMethodInfo(ClassInfo classInfo, List<ClassInfo> parents, boolean isStatic, String methodName, String methodDesc) {
        MethodInfo methodInfo = classInfo.getMethod(methodName, methodDesc);
        // Only return this method info if the staticness matches
        if (methodInfo != null && (methodInfo.access & Opcodes.ACC_STATIC) == (isStatic ? Opcodes.ACC_STATIC : 0))
            return methodInfo;

        for (ClassInfo parent : parents) {
            methodInfo = parent.getMethod(methodName, methodDesc);
            // Don't return a private method info from a parent class and only return this parent method info if the staticness matches
            if (methodInfo != null && (methodInfo.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) == (isStatic ? Opcodes.ACC_STATIC : 0))
                return methodInfo;
        }

        return null;
    }

    @Nullable
    public static FieldInfo getFieldInfo(ClassInfo classInfo, List<ClassInfo> parents, boolean isStatic, String fieldName) {
        FieldInfo fieldInfo = classInfo.getField(fieldName);
        // Only return this field info if the staticness matches
        if (fieldInfo != null && (fieldInfo.access & Opcodes.ACC_STATIC) == (isStatic ? Opcodes.ACC_STATIC : 0))
            return fieldInfo;

        for (ClassInfo parent : parents) {
            fieldInfo = parent.getField(fieldName);
            // Don't return a private field info from a parent class and only return this parent field info if the staticness matches
            if (fieldInfo != null && (fieldInfo.access & (Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE)) == (isStatic ? Opcodes.ACC_STATIC : 0))
                return fieldInfo;
        }

        return null;
    }

    /**
     * Returns a list of parent class names, both super classes and interfaces.
     * The list is sorted based on the topological order of the class hierarchy for each parent.
     *
     * @param includeSuper if {@code true}, super classnames will be included.
     * Otherwise, only interfaces will be, including those present on super classes.
     * @return a list of parent class names, both super classes and interfaces
     */
    @SuppressWarnings("UnstableApiUsage")
    public static List<String> getParentClassNames(ClassInfoCache cache, ClassInfo classInfo, boolean includeSuper) {
        List<String> interfaces = classInfo.getInterfaces();
        if (interfaces.isEmpty() && classInfo.superName == null)
            return ImmutableList.of();

        MutableGraph<String> parentGraph = GraphBuilder.directed().allowsSelfLoops(false).build();
        Queue<String> interfaceQueue = new ArrayDeque<>();
        for (String interfaceName : interfaces) {
            parentGraph.putEdge(classInfo.name, interfaceName);
            interfaceQueue.add(interfaceName);
        }

        ClassInfo superInfo = classInfo;
        while (superInfo.superName != null) {
            ClassInfo currentInfo = superInfo;
            superInfo = cache.getClassInfo(superInfo.superName);
            if (includeSuper)
                parentGraph.putEdge(currentInfo.name, superInfo.name);
            for (String parentInterfaceName : superInfo.getInterfaces()) {
                parentGraph.putEdge(includeSuper ? superInfo.name : classInfo.name, parentInterfaceName);
                interfaceQueue.add(parentInterfaceName);
            }
        }

        Set<String> seenInterfaces = interfaceQueue.isEmpty() ? null : new HashSet<>();
        while (!interfaceQueue.isEmpty()) {
            String interfaceName = interfaceQueue.remove();
            if (!seenInterfaces.add(interfaceName))
                continue;

            ClassInfo interfaceInfo = cache.getClassInfo(interfaceName);
            for (String parentInterfaceName : interfaceInfo.getInterfaces()) {
                interfaceQueue.add(parentInterfaceName);
                parentGraph.putEdge(interfaceName, parentInterfaceName);
            }
        }

        // TODO Should this use a secondary comparator which sorts by declaration order?
        List<String> parents = TopologicalSort.topologicalSort(parentGraph, null);
        parents.remove(0); // Always remove the class itself, which is always first
        return parents;
    }

    /**
     * Returns a list of parent class infos, both super classes and interfaces.
     * The list is sorted based on the topological order of the class hierarchy for each parent.
     *
     * @param includeSuper if {@code true}, super class infos will be included.
     * Otherwise, only interfaces will be, including those present on super classes.
     * @return a list of parent class infos, both super classes and interfaces
     */
    public static List<ClassInfo> getParentClassInfos(ClassInfoCache cache, ClassInfo classInfo, boolean includeSuper) {
        List<ClassInfo> parents = new ArrayList<>();

        for (String parentName : getParentClassNames(cache, classInfo, includeSuper)) {
            parents.add(cache.getClassInfo(parentName));
        }

        return parents;
    }

    public static class Results {
        public final String className;
        private List<String> incompatibilities;
        private List<MethodIncompatibility> methodIncompatibilities;
        private List<FieldIncompatibility> fieldIncompatibilities;

        Results(String className) {
            this.className = className;
        }

        void addIncompatibility(String incompatibility) {
            if (this.incompatibilities == null) {
                this.incompatibilities = new ArrayList<>();
            }

            this.incompatibilities.add(incompatibility);
        }

        void addIncompatibility(MethodInfo methodInfo, String incompatibility) {
            if (this.methodIncompatibilities == null) {
                this.methodIncompatibilities = new ArrayList<>();
            }

            this.methodIncompatibilities.add(new MethodIncompatibility(methodInfo, incompatibility));
            addIncompatibility(methodInfo.getNameDesc() + " - " + incompatibility);
        }

        void addIncompatibility(FieldInfo fieldInfo, String incompatibility) {
            if (this.fieldIncompatibilities == null) {
                this.fieldIncompatibilities = new ArrayList<>();
            }

            this.fieldIncompatibilities.add(new FieldIncompatibility(fieldInfo, incompatibility));
            addIncompatibility(fieldInfo.getNameDesc() + " - " + incompatibility);
        }

        public boolean isCompatible() {
            return this.incompatibilities == null || this.incompatibilities.isEmpty();
        }

        public boolean isIncompatible() {
            return this.incompatibilities != null && !this.incompatibilities.isEmpty();
        }

        public List<String> getIncompatibilities() {
            return this.incompatibilities == null ? ImmutableList.of() : this.incompatibilities;
        }

        public List<MethodIncompatibility> getMethodIncompatibilities() {
            return this.methodIncompatibilities == null ? ImmutableList.of() : this.methodIncompatibilities;
        }

        public List<FieldIncompatibility> getFieldIncompatibilities() {
            return this.fieldIncompatibilities == null ? ImmutableList.of() : this.fieldIncompatibilities;
        }

        @Override
        public String toString() {
            return this.incompatibilities == null ? "[]" : this.incompatibilities.toString();
        }
    }

    public static class MethodIncompatibility {
        public final MethodInfo methodInfo;
        public final String message;

        public MethodIncompatibility(MethodInfo methodInfo, String message) {
            this.methodInfo = methodInfo;
            this.message = message;
        }
    }

    public static class FieldIncompatibility {
        public final FieldInfo fieldInfo;
        public final String message;

        public FieldIncompatibility(FieldInfo fieldInfo, String message) {
            this.fieldInfo = fieldInfo;
            this.message = message;
        }
    }
}
