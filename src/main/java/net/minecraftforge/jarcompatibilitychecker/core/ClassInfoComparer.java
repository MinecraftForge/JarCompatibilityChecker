/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import net.minecraftforge.jarcompatibilitychecker.data.AnnotationInfo;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import net.minecraftforge.jarcompatibilitychecker.data.FieldInfo;
import net.minecraftforge.jarcompatibilitychecker.data.MemberInfo;
import net.minecraftforge.jarcompatibilitychecker.data.MethodInfo;
import net.minecraftforge.jarcompatibilitychecker.sort.TopologicalSort;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;

public class ClassInfoComparer {
    public static ClassInfoComparisonResults compare(boolean checkBinary, ClassInfoCache baseCache, ClassInfo baseClassInfo, ClassInfoCache concreteCache,
            @Nullable ClassInfo concreteClassInfo) {
        return compare(checkBinary, null, baseCache, baseClassInfo, concreteCache, concreteClassInfo);
    }

    public static ClassInfoComparisonResults compare(boolean checkBinary, @Nullable AnnotationCheckMode annotationCheckMode,
            ClassInfoCache baseCache, ClassInfo baseClassInfo, ClassInfoCache concreteCache, @Nullable ClassInfo concreteClassInfo) {
        return compare(checkBinary, annotationCheckMode, InternalAnnotationCheckMode.DEFAULT_INTERNAL_ANNOTATIONS, InternalAnnotationCheckMode.DEFAULT_MODE,
                baseCache, baseClassInfo, concreteCache, concreteClassInfo);
    }

    public static ClassInfoComparisonResults compare(boolean checkBinary, @Nullable AnnotationCheckMode annotationCheckMode,
            List<String> internalAnnotations, InternalAnnotationCheckMode internalAnnotationCheckMode, ClassInfoCache baseCache, ClassInfo baseClassInfo,
            ClassInfoCache concreteCache, @Nullable ClassInfo concreteClassInfo) {
        ClassInfoComparisonResults results = new ClassInfoComparisonResults(baseClassInfo);
        boolean classInternal = isInternalApi(baseClassInfo, internalAnnotations, internalAnnotationCheckMode);

        if (classInternal && internalAnnotationCheckMode == InternalAnnotationCheckMode.SKIP)
            return results;

        boolean isClassError = !classInternal || internalAnnotationCheckMode == InternalAnnotationCheckMode.ERROR;
        boolean classVisible = isVisible(checkBinary, baseClassInfo.access);

        if (concreteClassInfo == null) {
            if (checkBinary) {
                results.addClassIncompatibility(baseClassInfo, IncompatibilityMessages.CLASS_MISSING, isClassError);
            } else if (classVisible) {
                results.addClassIncompatibility(baseClassInfo, IncompatibilityMessages.API_CLASS_MISSING, isClassError);
            }

            // This is as far as we can get if the input class doesn't exist
            return results;
        }

        if (isVisibilityLowered(checkBinary, baseClassInfo.access, concreteClassInfo.access)) {
            results.addClassIncompatibility(baseClassInfo, IncompatibilityMessages.CLASS_LOWERED_VISIBILITY, isClassError);
        }

        boolean classFinal = (baseClassInfo.access & Opcodes.ACC_FINAL) != 0;
        if (isMadeAbstract(classVisible, baseClassInfo.access, concreteClassInfo.access)) {
            results.addClassIncompatibility(baseClassInfo, IncompatibilityMessages.CLASS_MADE_ABSTRACT, isClassError);
        }

        if (isMadeFinal(checkBinary, baseClassInfo.access, concreteClassInfo.access)) {
            results.addClassIncompatibility(baseClassInfo, IncompatibilityMessages.CLASS_MADE_FINAL, isClassError);
        }

        checkAnnotations(annotationCheckMode, results, baseClassInfo, isClassError, baseClassInfo.annotations, concreteClassInfo.annotations);

        if (baseClassInfo.superName != null) {
            ClassInfo superClassInfo = baseCache.getClassInfo(baseClassInfo.superName);
            // A missing superclass is always important to binary compatibility but only important to API compatibility if the superclass is public or protected
            boolean shouldCheckSuper = isVisible(checkBinary, superClassInfo.access);
            if (shouldCheckSuper && !hasSuperClass(concreteCache, concreteClassInfo, baseClassInfo.superName)) {
                results.addClassIncompatibility(baseClassInfo, String.format(Locale.ROOT, IncompatibilityMessages.CLASS_MISSING_SUPERCLASS, baseClassInfo.superName), isClassError);
            }
        }

        Set<String> baseInterfaces = new HashSet<>(getParentClassNames(checkBinary, baseCache, baseClassInfo, false));
        Set<String> concreteInterfaces = new HashSet<>(getParentClassNames(checkBinary, concreteCache, concreteClassInfo, false));

        Set<String> missingInterfaces = Sets.difference(baseInterfaces, concreteInterfaces);
        if (!missingInterfaces.isEmpty() && !checkBinary) {
            missingInterfaces = new HashSet<>(missingInterfaces);
            missingInterfaces.removeIf(interfaceName -> {
                ClassInfo interfaceInfo = baseCache.getClassInfo(interfaceName);
                // A missing interface is only important to API compatibility if the interface is public or protected, so we get rid of any that aren't
                return (interfaceInfo.access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) == 0;
            });
        }
        if (!missingInterfaces.isEmpty()) {
            if (missingInterfaces.size() == 1) {
                results.addClassIncompatibility(baseClassInfo, String.format(Locale.ROOT, IncompatibilityMessages.CLASS_MISSING_INTERFACE, missingInterfaces.iterator().next()), isClassError);
            } else {
                results.addClassIncompatibility(baseClassInfo, String.format(Locale.ROOT, IncompatibilityMessages.CLASS_MISSING_INTERFACES, missingInterfaces), isClassError);
            }
        }

        List<ClassInfo> concreteParents = getParentClassInfos(checkBinary, concreteCache, concreteClassInfo, true);

        Set<MethodInfo> seenMethods = new HashSet<>();

        for (MethodInfo baseInfo : baseClassInfo.getMethods().values()) {
            boolean isStatic = (baseInfo.access & Opcodes.ACC_STATIC) != 0;
            MethodInfo inputInfo = getMethodInfo(concreteClassInfo, concreteParents, isStatic, baseInfo.name, baseInfo.desc);
            boolean methodInternal = isInternalApi(baseInfo, internalAnnotations, internalAnnotationCheckMode);
            if (methodInternal && internalAnnotationCheckMode == InternalAnnotationCheckMode.SKIP)
                continue;

            boolean isMethodError = !methodInternal || internalAnnotationCheckMode == InternalAnnotationCheckMode.ERROR;
            boolean methodVisible = isVisible(checkBinary, baseInfo.access);

            if (inputInfo == null) {
                if (checkBinary) {
                    results.addMethodIncompatibility(baseInfo, IncompatibilityMessages.METHOD_REMOVED, isMethodError);
                } else if (methodVisible) {
                    results.addMethodIncompatibility(baseInfo, IncompatibilityMessages.API_METHOD_REMOVED, isMethodError);
                }

                // This is as far as we can get without any info on the concrete method
                continue;
            }

            seenMethods.add(inputInfo);

            if (isVisibilityLowered(checkBinary, baseInfo.access, inputInfo.access)) {
                results.addMethodIncompatibility(baseInfo, IncompatibilityMessages.METHOD_LOWERED_VISIBILITY, isMethodError);
            }

            if (isMadeAbstract(classVisible, baseInfo.access, inputInfo.access)) {
                results.addMethodIncompatibility(baseInfo, IncompatibilityMessages.METHOD_MADE_ABSTRACT, isMethodError);
            }

            if (!classFinal && isMadeFinal(checkBinary, baseInfo.access, inputInfo.access)) {
                results.addMethodIncompatibility(baseInfo, IncompatibilityMessages.METHOD_MADE_FINAL, isMethodError);
            }

            checkAnnotations(annotationCheckMode, results, baseInfo, isMethodError, baseInfo.annotations, inputInfo.annotations);
        }

        for (MethodInfo concreteInfo : concreteClassInfo.getMethods().values()) {
            if (seenMethods.contains(concreteInfo))
                continue;

            if (classVisible && (concreteInfo.access & Opcodes.ACC_ABSTRACT) != 0) {
                results.addMethodIncompatibility(concreteInfo, IncompatibilityMessages.METHOD_MADE_ABSTRACT);
            }
        }

        for (FieldInfo baseInfo : baseClassInfo.getFields().values()) {
            boolean isStatic = (baseInfo.access & Opcodes.ACC_STATIC) != 0;
            FieldInfo inputInfo = getFieldInfo(concreteClassInfo, concreteParents, isStatic, baseInfo.name);
            boolean fieldInternal = isInternalApi(baseInfo, internalAnnotations, internalAnnotationCheckMode);
            if (fieldInternal && internalAnnotationCheckMode == InternalAnnotationCheckMode.SKIP)
                continue;

            boolean isFieldError = !fieldInternal || internalAnnotationCheckMode == InternalAnnotationCheckMode.ERROR;
            boolean fieldVisible = isVisible(checkBinary, baseInfo.access);

            if (inputInfo == null) {
                if (checkBinary) {
                    results.addFieldIncompatibility(baseInfo, IncompatibilityMessages.FIELD_REMOVED, isFieldError);
                } else if (fieldVisible) {
                    results.addFieldIncompatibility(baseInfo, IncompatibilityMessages.API_FIELD_REMOVED, isFieldError);
                }

                // This is as far as we can get without any info on the concrete field
                continue;
            }

            if (isVisibilityLowered(checkBinary, baseInfo.access, inputInfo.access)) {
                results.addFieldIncompatibility(baseInfo, IncompatibilityMessages.FIELD_LOWERED_VISIBILITY, isFieldError);
            }

            if (!classFinal && isMadeFinal(checkBinary, baseInfo.access, inputInfo.access)) {
                results.addFieldIncompatibility(baseInfo, IncompatibilityMessages.FIELD_MADE_FINAL, isFieldError);
            }

            checkAnnotations(annotationCheckMode, results, baseInfo, isFieldError, baseInfo.annotations, inputInfo.annotations);
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

    public static boolean isMadeAbstract(boolean classVisible, int baseAccess, int inputAccess) {
        // Even if this is a method which is not visible from outside the JAR, issues can still appear at runtime due to an implementation class not being able to implement the package-private method.
        return classVisible && (baseAccess & Opcodes.ACC_ABSTRACT) == 0 && (inputAccess & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static boolean isVisible(boolean checkBinary, int access) {
        return checkBinary || (access & (Opcodes.ACC_PUBLIC | Opcodes.ACC_PROTECTED)) != 0;
    }

    public static boolean isMadeFinal(boolean checkBinary, int baseAccess, int inputAccess) {
        return isVisible(checkBinary, baseAccess) && (baseAccess & Opcodes.ACC_FINAL) == 0 && (inputAccess & Opcodes.ACC_FINAL) != 0;
    }

    public static boolean isInternalApi(MemberInfo memberInfo, List<String> internalAnnotations, InternalAnnotationCheckMode checkMode) {
        if (checkMode == InternalAnnotationCheckMode.ERROR)
            return false; // Even if internal, we want to handle internal members like normal for ERROR check mode

        for (String internalAnnotation : internalAnnotations) {
            if (memberInfo.hasAnnotation(internalAnnotation))
                return true;
        }

        return false;
    }

    public static <I extends MemberInfo> void checkAnnotations(@Nullable AnnotationCheckMode mode, ClassInfoComparisonResults results, I memberInfo,
            List<AnnotationInfo> baseAnnotations, List<AnnotationInfo> concreteAnnotations) {
        checkAnnotations(mode, results, memberInfo, true, baseAnnotations, concreteAnnotations);
    }

    public static <I extends MemberInfo> void checkAnnotations(@Nullable AnnotationCheckMode mode, ClassInfoComparisonResults results, I memberInfo, boolean isError,
            List<AnnotationInfo> baseAnnotations, List<AnnotationInfo> concreteAnnotations) {
        if (mode == null || (baseAnnotations.isEmpty() && concreteAnnotations.isEmpty()))
            return;

        // boolean requiresExact = mode.requiresExact();
        // List<AnnotationInfo> concreteCopy = new ArrayList<>(concreteAnnotations);
        //
        // for (AnnotationInfo baseAnnotation : baseAnnotations) {
        //     AnnotationInfo descMatch = null;
        //     boolean foundMatch = false;
        //     for (Iterator<AnnotationInfo> iterator = concreteCopy.iterator(); iterator.hasNext(); ) {
        //         AnnotationInfo concreteAnnotation = iterator.next();
        //         if (baseAnnotation.equals(concreteAnnotation)) {
        //             iterator.remove();
        //             descMatch = concreteAnnotation;
        //             foundMatch = true;
        //         } else if (baseAnnotation.desc.equals(concreteAnnotation.desc)) {
        //             if (!requiresExact) {
        //                 iterator.remove();
        //                 foundMatch = true;
        //             }
        //             descMatch = concreteAnnotation;
        //         }
        //     }
        //
        //     if (!foundMatch) {
        //         results.addAnnotationIncompatibility(mode, memberInfo, baseAnnotation, descMatch != null
        //                 ? String.format(Locale.ROOT, IncompatibilityMessages.ANNOTATION_CHANGED, descMatch)
        //                 : IncompatibilityMessages.ANNOTATION_REMOVED);
        //     }
        // }

        if (mode.checkAddition()) {
            List<AnnotationInfo> baseCopy = new ArrayList<>(baseAnnotations);

            for (AnnotationInfo concreteAnnotation : concreteAnnotations) {
                AnnotationInfo match = null;
                for (Iterator<AnnotationInfo> iterator = baseCopy.iterator(); iterator.hasNext(); ) {
                    AnnotationInfo baseAnnotation = iterator.next();
                    if (concreteAnnotation.equals(baseAnnotation) || concreteAnnotation.desc.equals(baseAnnotation.desc)) {
                        iterator.remove();
                        match = baseAnnotation;
                        break;
                    }
                }

                if (match == null) {
                    // No match found for concrete annotation in base JAR; this means a new annotation was found
                    results.addAnnotationIncompatibility(mode, memberInfo, concreteAnnotation, IncompatibilityMessages.ANNOTATION_ADDED, isError);
                }
            }
        }
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
     * @param checkBinary if {@code true}, super classes of all visibilities will be included.
     * Otherwise, only public and protected super classes will be included.
     * @param includeSuper if {@code true}, super classnames will be included.
     * Otherwise, only interfaces will be, including those present on super classes.
     * @return a list of parent class names, both super classes and interfaces
     */
    @SuppressWarnings("UnstableApiUsage")
    public static List<String> getParentClassNames(boolean checkBinary, ClassInfoCache cache, ClassInfo classInfo, boolean includeSuper) {
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
            boolean include = includeSuper && isVisible(checkBinary, currentInfo.access);
            if (include)
                parentGraph.putEdge(currentInfo.name, superInfo.name);
            for (String parentInterfaceName : superInfo.getInterfaces()) {
                parentGraph.putEdge(include ? superInfo.name : classInfo.name, parentInterfaceName);
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
        // If the list of parents is non-empty, the first element is the class itself which should be removed
        if (!parents.isEmpty())
            parents.remove(0);
        return parents;
    }

    /**
     * Returns a list of parent class infos, both super classes and interfaces.
     * The list is sorted based on the topological order of the class hierarchy for each parent.
     *
     * @param checkBinary if {@code true}, super classes of all visibilities will be included.
     * Otherwise, only public and protected super classes will be included.
     * @param includeSuper if {@code true}, super class infos will be included.
     * Otherwise, only interfaces will be, including those present on super classes.
     * @return a list of parent class infos, both super classes and interfaces
     */
    public static List<ClassInfo> getParentClassInfos(boolean checkBinary, ClassInfoCache cache, ClassInfo classInfo, boolean includeSuper) {
        List<ClassInfo> parents = new ArrayList<>();

        for (String parentName : getParentClassNames(checkBinary, cache, classInfo, includeSuper)) {
            parents.add(cache.getClassInfo(parentName));
        }

        return parents;
    }

}
