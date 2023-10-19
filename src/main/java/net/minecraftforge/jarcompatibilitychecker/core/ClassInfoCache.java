/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.jarcompatibilitychecker.core;

import com.google.common.io.ByteStreams;
import net.minecraftforge.jarcompatibilitychecker.data.ClassInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassInfoCache {
    private final Map<String, ClassInfo> mainClasses = new HashMap<>();
    private final Map<String, ClassInfo> libClasses = new HashMap<>();
    private final Set<String> failedClasses = new HashSet<>();

    public static ClassInfoCache fromJarFile(File jarFile, List<File> libraries) throws IOException {
        ClassInfoCache cache = new ClassInfoCache();

        readJar(jarFile, cache.mainClasses);
        for (File libFile : libraries) {
            readJar(libFile, cache.libClasses);
        }

        return cache;
    }

    public static ClassInfoCache fromJarFile(File jarFile) throws IOException {
        ClassInfoCache cache = new ClassInfoCache();

        readJar(jarFile, cache.mainClasses);

        return cache;
    }

    public static ClassInfoCache fromJarPath(Path jarPath) throws IOException {
        ClassInfoCache cache = new ClassInfoCache();

        readJar(jarPath, cache.mainClasses);

        return cache;
    }

    public static ClassInfoCache fromJarPath(Path jarPath, List<Path> libraries) throws IOException {
        ClassInfoCache cache = new ClassInfoCache();

        readJar(jarPath, cache.mainClasses);
        for (Path libPath : libraries) {
            readJar(libPath, cache.libClasses);
        }

        return cache;
    }

    public static ClassInfoCache fromFolder(Path folder) throws IOException {
        ClassInfoCache cache = new ClassInfoCache();

        readFolder(folder, cache.mainClasses);

        return cache;
    }

    public static ClassInfoCache fromFolder(Path folder, List<Path> libraries) throws IOException {
        ClassInfoCache cache = new ClassInfoCache();

        readFolder(folder, cache.mainClasses);
        for (Path libPath : libraries) {
            readJar(libPath, cache.libClasses);
        }

        return cache;
    }

    public static ClassInfoCache fromMaps(Map<String, ClassInfo> mainClasses, Map<String, ClassInfo> libClasses) {
        return new ClassInfoCache(mainClasses, libClasses);
    }

    public static ClassInfoCache empty() {
        return new ClassInfoCache(new HashMap<>(), new HashMap<>());
    }

    private ClassInfoCache() {}

    private ClassInfoCache(Map<String, ClassInfo> mainClasses, Map<String, ClassInfo> libClasses) {
        this.mainClasses.putAll(mainClasses);
        this.libClasses.putAll(libClasses);
    }

    public static void readJar(File file, Map<String, ClassInfo> classes) throws IOException {
        try (ZipFile zip = new ZipFile(file)) {
            for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class") || entry.getName().startsWith("."))
                    continue;

                ClassReader reader;
                try (InputStream entryInputStream = zip.getInputStream(entry)) {
                    reader = new ClassReader(ByteStreams.toByteArray(entryInputStream));
                }
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);
                ClassInfo info = new ClassInfo(classNode);

                if (!classes.containsKey(info.name))
                    classes.put(info.name, info);
            }
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not open JAR file: " + e.getMessage());
        }
    }

    public static void readJar(Path jarPath, Map<String, ClassInfo> classes) throws IOException {
        try (FileSystem zipFs = FileSystems.newFileSystem(jarPath, null)) {
            Path root = zipFs.getPath("/");
            readFolder(root, classes);
        } catch (FileNotFoundException e) {
            throw new FileNotFoundException("Could not open JAR file: " + e.getMessage());
        }
    }

    public static void readFolder(Path folder, Map<String, ClassInfo> classes) throws IOException {
        try (Stream<Path> walker = Files.walk(folder)) {
            Iterable<Path> iterable = walker::iterator;
            for (Path entryPath : iterable) {
                Path namePath = entryPath.getFileName();
                String name = namePath == null ? null : namePath.toString();
                if (name == null || !name.endsWith(".class") || name.startsWith("."))
                    continue;

                ClassReader reader;
                try (InputStream entryInputStream = Files.newInputStream(entryPath)) {
                    reader = new ClassReader(ByteStreams.toByteArray(entryInputStream));
                }
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);
                ClassInfo info = new ClassInfo(classNode);

                if (!classes.containsKey(info.name))
                    classes.put(info.name, info);
            }
        }
    }

    public Map<String, ClassInfo> getMainClasses() {
        return this.mainClasses;
    }

    @Nullable
    public ClassInfo getMainClassInfo(String className) {
        return this.mainClasses.get(className);
    }

    @NotNull
    public ClassInfo getClassInfo(String className) {
        ClassInfo info = this.mainClasses.containsKey(className) ? this.mainClasses.get(className) : this.libClasses.get(className);

        if (info == null && !failedClasses.contains(className)) {
            try {
                Class<?> cls = Class.forName(className.replace('/', '.'), false, this.getClass().getClassLoader());
                info = new ClassInfo(cls);
                this.libClasses.put(className, info);
            } catch (ClassNotFoundException ex) {
                failedClasses.add(className);
            }
        }

        if (info == null)
            throw new IllegalArgumentException("Class " + className + " was not found in class info cache or JVM classpath");

        return info;
    }
}
