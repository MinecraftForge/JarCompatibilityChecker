# JarCompatibilityChecker
**JarCompatibilityChecker** (or **JCC** for short) is a tool written with Java 8 which reports API or binary incompatibilities between two JARs.
The JAR with the API or base code to be compared against is called the **base JAR**.
The input JAR to compare against the API or base for incompatibilities is called the **concrete JAR**.

Compatibility modes:
- **API** - Checks for compatibility between the public and protected members (API) of the base JAR and concrete JAR
- **Binary** - Checks for binary compatibility between all members, both public and private, of the base JAR and concrete JAR

It is not a goal of this tool to check for source compatibility.
Because it operates on compiled class files located inside JARs, the tool only looks for incompatibilities that could cause exceptions and crashes at runtime.
This means that there may be changes between the base JAR and concrete JAR which cause a source incompatibility but are not reported by this tool as it is still compatible at runtime.

## Usage
```groovy
repositories {
    maven {
        name = 'MinecraftForge'
        url = 'https://maven.minecraftforge.net/'
    }
}

dependencies {
    implementation 'net.minecraftforge:JarCompatibilityChecker:0.1.+'
}
```

For command-line usage, the `all` classifier JAR can be downloaded and used.
For usage from inside other libraries, the no-classifier JAR can be referenced through gradle.
The main entrypoint for other libraries is the `net.minecraftforge.jarcompatibilitychecker.JarCompatibilityChecker` class.

## Note on Terminology
JarCompatibilityChecker and the Java Language Specification have different meanings for binary compatibility.
For JarCompatibilityChecker, binary compatibility means that all members, both public and private, are compatible between the base JAR and concrete JAR.
Compatible means that a member still exists in the concrete JAR, its type or parameters has not been changed, and its visibility has not been lowered.
For the Java Language Specification, binary compatibility means a new version of a JAR does not break other binaries depending on previous versions of that JAR.
Binaries can normally only reference public and protected members of another JAR,
so this definition of binary compatibility is more in line with JarCompatibilityChecker's definition of API compatibility.
