/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.core;

public enum AnnotationCheckMode {
    ERROR_ADDED(true, true),
    WARN_ADDED(false, true);

    private final boolean error;
    private final boolean addition;

    AnnotationCheckMode(boolean error, boolean addition) {
        this.error = error;
        this.addition = addition;
    }

    public boolean shouldError() {
        return this.error;
    }

    public boolean shouldWarn() {
        return !this.error;
    }

    public boolean checkAddition() {
        return this.addition;
    }
}
