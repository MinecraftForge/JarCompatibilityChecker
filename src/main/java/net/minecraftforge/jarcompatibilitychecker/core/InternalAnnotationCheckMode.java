/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * This enum determines the check mode for all elements marked with internal annotations.
 * An internal annotation can be used to mark an element as internal API, meaning binary breaking changes are allowed for that element.
 * Note that adding an internal annotation to an element while changing it is still considered a binary breaking change.
 * <p>
 * By default, the {@linkplain #WARN} mode is used.
 */
public enum InternalAnnotationCheckMode {
    /**
     * No checks will be performed on elements annotated with an internal annotation.
     */
    SKIP,
    /**
     * All error-level incompatibilities will be lowered to warnings for elements annotated with an internal annotation.
     */
    WARN,
    /**
     * Error-level incompatibilities will be raised for elements regardless of being marked with an internal annotation.
     */
    ERROR;

    public static final InternalAnnotationCheckMode DEFAULT_MODE = WARN;
    public static final List<String> DEFAULT_INTERNAL_ANNOTATIONS = ImmutableList.of("Lorg/jetbrains/annotations/ApiStatus$Internal;");
}
