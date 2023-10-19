/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.core;

public class IncompatibilityMessages {
    // Class
    public static final String CLASS_MISSING = "Class no longer exists";
    public static final String API_CLASS_MISSING = "API class no longer exists";
    public static final String CLASS_LOWERED_VISIBILITY = "Class was lowered in visibility";
    public static final String CLASS_MISSING_SUPERCLASS = "Class missing superclass of %s";
    public static final String CLASS_MISSING_INTERFACE = "Class missing interface: %s";
    public static final String CLASS_MISSING_INTERFACES = "Class missing interfaces: %s";
    public static final String CLASS_MADE_ABSTRACT = "Class was made abstract";
    public static final String CLASS_MADE_FINAL = "Class was made final";

    // Method
    public static final String METHOD_REMOVED = "Method was removed";
    public static final String API_METHOD_REMOVED = "API method was removed";
    public static final String METHOD_LOWERED_VISIBILITY = "Method was lowered in visibility";
    public static final String METHOD_MADE_ABSTRACT = "Method was made abstract";
    public static final String METHOD_MADE_FINAL = "Method was made final";

    // Field
    public static final String FIELD_REMOVED = "Field was removed";
    public static final String API_FIELD_REMOVED = "API field was removed";
    public static final String FIELD_LOWERED_VISIBILITY = "Field was lowered in visibility";
    public static final String FIELD_MADE_FINAL = "Field was made final";

    // Annotation
    public static final String ANNOTATION_ADDED = "Annotation was added";
    public static final String ANNOTATION_REMOVED = "Annotation was removed";
    public static final String ANNOTATION_CHANGED = "Annotation was changed to %s";
}
