/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.jarcompatibilitychecker.core;

/**
 * This enum determines the check mode for functional interfaces.
 * A functional interface is an interface that has a single abstract method (or SAM).
 * If enabled, all SAM-type interfaces will be checked to see if the abstract method was changed or removed.
 * <p>
 * As noted on {@linkplain FunctionalInterface}, declaring abstract methods that are inherited from {@linkplain Object java.lang.Object}
 * will not count towards the SAM.
 *
 * @see FunctionalInterface
 */
public enum FunctionalInterfaceCheckMode {
    /**
     * No checks specific to functional interfaces will be performed.
     */
    NONE,
    /**
     * If the SAM's descriptor is changed, removed, or made {@code default}, a warning will be reported.
     * <p>
     * This indicates a runtime error will occur using the new version if any consumers declare a lambda implementing the previously SAM-type interface.
     */
    WARN_CHANGED,
    /**
     * If the SAM's descriptor is changed, removed, or made {@code default}, an error will be reported.
     * <p>
     * This indicates a runtime error will occur using the new version if any consumers declare a lambda implementing the previously SAM-type interface.
     */
    ERROR_CHANGED
}
