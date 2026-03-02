# AGENTS.md - Input4j Developer Guide

This document provides guidelines for agents working on the Input4j codebase.

## Project Overview

Input4j is a pure Java input library using the Foreign Function & Memory API (FFM API) for cross-platform gamepad/controller support on Windows, Linux, and macOS.

- **Language**: Java 22+
- **Build System**: Gradle
- **Test Framework**: JUnit Jupiter (JUnit 5)
- **Code Style**: Spotless (Google Java Format)

---

## Build, Lint, and Test Commands

### Build the project
```bash
./gradlew build
```

### Run all tests
```bash
./gradlew test
```

### Run a single test class
```bash
./gradlew test --tests "de.gurkenlabs.input4j.InputDeviceTests"
```

### Run a single test method
```bash
./gradlew test --tests "de.gurkenlabs.input4j.InputDeviceTests.testPoll"
```

### Check code style (Spotless)
```bash
./gradlew spotlessCheck
```

### Apply code style fixes
```bash
./gradlew spotlessApply
```

### Build without code style check (CI use)
```bash
./gradlew build -PskipSpotless
```

### Generate JaCoCo test coverage report
```bash
./gradlew jacocoTestReport
```

### Run with remote debugging
```bash
./gradlew remoteDebug
```

---

## Code Style Guidelines

### General Principles

- All public classes should be `final` unless explicitly designed for extension
- Use `var` for local variables when the type is obvious from the right side
- Use method references (`Object::method`) instead of lambdas where possible
- Always use braces for control structures, even single-line bodies
- Maximum line length: 100 characters (enforced by Spotless)

### Naming Conventions

| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `InputDevice`, `XInputPlugin` |
| Methods | camelCase | `poll()`, `getComponents()` |
| Fields | camelCase | `identifier`, `pollCallback` |
| Constants | UPPER_SNAKE_CASE | `MAX_DEFAULT_AXIS_ID` |
| Packages | lowercase | `de.gurkenlabs.input4j.foreign.windows` |

### Import Organization

Imports are organized in the following order (enforced by Spotless):
1. `java.*` imports
2. External library imports
3. `de.gurkenlabs.*` imports
4. Static imports

```java
import java.awt.Frame;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDeviceListener;

import static org.junit.jupiter.api.Assertions.*;
```

### Java Records

Use records for immutable data containers, especially for events:

```java
public record InputValueChangedEvent(InputComponent component, float oldValue, float newValue) {
}
```

### Javadoc Requirements

- All public classes and public/protected methods must have Javadoc
- Use `@param`, `@return`, and `@throws` tags appropriately
- Include `{@link}` references for related classes

```java
/**
 * Initializes the input device provider with the default platform library.
 *
 * @return The initialized input device provider or null if initialization fails.
 */
public static InputDevicePlugin init() { ... }
```

### Error Handling

- Use `IllegalArgumentException` for invalid arguments
- Use `IllegalStateException` for invalid state
- Log errors with `java.util.logging.Logger` before returning null or throwing
- Return empty `Optional` instead of null for optional results

```java
private static final Logger log = Logger.getLogger(InputDevices.class.getName());

public void setAccuracy(int accuracy) {
  if (accuracy < 0) {
    throw new IllegalArgumentException("Accuracy must be non-negative");
  }
  this.accuracy = accuracy;
}
```

### Thread Safety

- Use `ConcurrentHashMap`, `CopyOnWriteArrayList`, and `ConcurrentHashMap.newKeySet()` for thread-safe collections
- Use final fields wherever possible

```java
private final List<InputComponent> components = new CopyOnWriteArrayList<>();
private final Collection<InputDeviceListener> listeners = ConcurrentHashMap.newKeySet();
```

### FFM API (Foreign Function & Memory)

The FFM API (java.lang.foreign) enables Java programs to interoperate with native code without JNI. It became permanent in Java 22 (JEP 454).

#### Core Concepts

| Concept | Description |
|---------|-------------|
| **Linker** | Bridge between Java and native code. Use `Linker.nativeLinker()` |
| **SymbolLookup** | Locates native functions in libraries. Use `linker.defaultLookup()` |
| **MemorySegment** | Represents a region of native memory |
| **Arena** | Manages lifecycle of memory allocations |
| **MemoryLayout** | Describes the layout of native data structures |
| **FunctionDescriptor** | Describes native function signatures |
| **MethodHandle** | Invokeable handle for calling native functions |

#### Working with Native Functions

```java
Linker linker = Linker.nativeLinker();
SymbolLookup stdlib = linker.defaultLookup();

// Describe the native function signature
FunctionDescriptor desc = FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS);

// Create downcall handle
MemorySegment strlenAddr = stdlib.find("strlen").get();
MethodHandle strlen = linker.downcallHandle(strlenAddr, desc);

// Call the native function
try (Arena arena = Arena.ofConfined()) {
  MemorySegment str = arena.allocateFrom("Hello");
  int len = (int) strlen.invokeExact(str);
}
```

#### Native Memory Management

- Always use `Arena` to manage memory lifecycle
- Use try-with-resources for automatic cleanup
- Prefer `Arena.ofConfined()` for single-threaded, `Arena.ofShared()` for multi-threaded

```java
try (Arena arena = Arena.ofConfined()) {
  MemorySegment segment = arena.allocate(1024);
  // Use segment...
} // Automatically freed
```

#### Working with Structs

Use `MemoryLayout` for structured native data:

```java
StructLayout layout = MemoryLayout.structLayout(
    ValueLayout.JAVA_INT.withName("x"),
    ValueLayout.JAVA_INT.withName("y")
);

int x = segment.get(layout, "x");
segment.set(layout, "x", 42, ValueLayout.JAVA_INT);
```

#### Restricted Methods

FFM includes restricted methods that can crash the JVM if misused. Enable native access in build.gradle:

```groovy
test {
  jvmArgs += '--enable-native-access=ALL-UNNAMED'
}
tasks.withType(JavaExec).configureEach {
  jvmArgs += '--enable-native-access=ALL-UNNAMED'
}
```

#### Native Helper Usage

Use `NativeHelper` for creating downcall handles:

```java
public final class NativeHelper {
  public static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) { ... }
  public static MethodHandle downcallHandle(MemorySegment address, FunctionDescriptor fdesc) { ... }
  public static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc, String captureCallState) { ... }
}
```

### Test Conventions

- Test class name: `<ClassName>Tests`
- Test method name: `<methodName>_<scenario>`
- Use `@BeforeEach` for setup
- Use static imports for assertions

```java
import static org.junit.jupiter.api.Assertions.*;

class InputDeviceTests {
  @BeforeEach
  void setUp() { ... }

  @Test
  void testPoll() { ... }
}
```

---

## Project Structure

```
src/
├── main/java/de/gurkenlabs/input4j/
│   ├── InputDevices.java          # Main entry point
│   ├── InputDevice.java           # Device representation
│   ├── InputComponent.java        # Component (button/axis)
│   ├── components/                # Predefined button/axis definitions
│   │   ├── Axis.java
│   │   ├── Button.java
│   │   ├── XInput.java
│   │   └── DualShock4.java
│   └── foreign/                   # Native implementations
│       ├── NativeHelper.java      # FFM utilities
│       ├── linux/
│       ├── windows/
│       │   ├── dinput/           # DirectInput
│       │   └── xinput/           # XInput
│       └── macos/
└── test/java/                     # Test sources mirror main structure
```

---

## Common Tasks

### Adding a new platform plugin

1. Create a new class implementing `InputDevicePlugin` in `src/main/java/de/gurkenlabs/input4j/foreign/<platform>/`
2. Add the plugin class to `InputDevices.InputLibrary` enum
3. Add corresponding tests in `src/test/java/`

### Adding a new predefined button/axis

1. Create or extend a class in `src/main/java/de/gurkenlabs/input4j/components/`
2. Use `InputComponent.ID` constructor to define button/axis IDs
3. Add static final fields for each button/axis

```java
public static final XInput A = new XInput(InputComponent.Button.get(0), "A");
```
