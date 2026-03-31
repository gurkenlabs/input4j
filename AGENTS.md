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

## Project Structure

```
src/main/java/de/gurkenlabs/input4j/
├── InputDevices.java                # Main entry point with InputLibrary enum
├── InputDevice.java                 # Device representation
├── InputComponent.java              # Component (button/axis)
├── InputDevicePlugin.java           # Plugin interface
├── InputDeviceListener.java         # Event callbacks
├── AbstractInputDevicePlugin.java   # Base plugin implementation
├── ComponentType.java              # Component type enum
├── components/                      # Predefined button/axis definitions
│   ├── Axis.java                    # Standard axis IDs
│   ├── Button.java                  # Standard button IDs
│   ├── XInput.java                  # Xbox controller mappings
│   └── DualShock4.java              # PS4 controller mappings
├── foreign/                         # Native implementations
│   ├── NativeHelper.java            # FFM downcall handle utilities
│   ├── util/
│   │   └── PlatformDetector.java   # OS detection
│   ├── windows/
│   │   ├── xinput/                  # XInput (Xbox controllers)
│   │   │   ├── XInputPlugin.java
│   │   │   ├── XINPUT_STATE.java
│   │   │   ├── XINPUT_GAMEPAD.java
│   │   │   ├── XINPUT_CAPABILITIES.java
│   │   │   └── XINPUT_VIBRATION.java
│   │   └── dinput/                  # DirectInput (legacy)
│   │       ├── DirectInputPlugin.java
│   │       ├── IDirectInput8.java
│   │       └── DIDEVICEINSTANCE.java
│   ├── linux/                       # evdev (/dev/input/event*)
│   │   ├── LinuxEventDevicePlugin.java
│   │   ├── LinuxEventDevice.java
│   │   └── input_event.java
│   └── macos/iokit/                 # IOKit (HID devices)
│       ├── IOKitPlugin.java
│       └── IOHIDDevice.java
└── examples/
```

---

## Platform-Specific APIs

| Platform | API | Header/Source | Plugin Class |
|----------|-----|--------------|--------------|
| Windows | XInput | `xinput.h` | `XInputPlugin` |
| Windows | DirectInput | `dinput.h` | `DirectInputPlugin` |
| Linux | evdev | `/dev/input/event*` | `LinuxEventDevicePlugin` |
| macOS | IOKit | `IOHIDManager` | `IOKitPlugin` |

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
| Native struct layouts | UPPER_SNAKE_CASE | `$LAYOUT`, `VH_fieldName` |

### Import Organization

Imports are organized in the following order (enforced by Spotless):
1. `java.*` imports
2. External library imports
3. `de.gurkenlabs.*` imports
4. Static imports

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

### Error Handling

- Use `IllegalArgumentException` for invalid arguments
- Use `IllegalStateException` for invalid state
- Log errors with `java.util.logging.Logger` before returning null or throwing
- Return empty `Optional` instead of null for optional results

### Thread Safety

- Use `ConcurrentHashMap`, `CopyOnWriteArrayList`, and `ConcurrentHashMap.newKeySet()` for thread-safe collections
- Use final fields wherever possible

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

## FFM API (Foreign Function & Memory)

The FFM API (java.lang.foreign) enables Java programs to interoperate with native code without JNI. It became permanent in Java 22 (JEP 454).

### Native Helper Usage

Use `NativeHelper` for creating downcall handles:

```java
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import de.gurkenlabs.input4j.foreign.NativeHelper;

MethodHandle handle = NativeHelper.downcallHandle(
    "XInputGetState",
    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
);
```

### Defining Native Struct Layouts

Use `MemoryLayout` for structured native data with the `$LAYOUT` naming convention:

```java
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.VarHandle;

final class XINPUT_STATE {
  int dwPacketNumber;
  XINPUT_GAMEPAD Gamepad;

  static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
      ValueLayout.JAVA_INT.withName("dwPacketNumber"),
      XINPUT_GAMEPAD.$LAYOUT.withName("Gamepad")
  );

  private static final VarHandle VH_dwPacketNumber = 
      $LAYOUT.varHandle(ValueLayout.PathElement.groupElement("dwPacketNumber"));
  private static final long GamepadOffset = 
      $LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("Gamepad"));

  static XINPUT_STATE read(MemorySegment segment) {
    var state = new XINPUT_STATE();
    state.dwPacketNumber = (int) VH_dwPacketNumber.get(segment, 0);
    state.Gamepad = XINPUT_GAMEPAD.read(segment.asSlice(GamepadOffset));
    return state;
  }
}
```

### Native Memory Management

- Always use `Arena` to manage memory lifecycle
- Use try-with-resources for automatic cleanup
- Prefer `Arena.ofConfined()` for single-threaded, `Arena.ofShared()` for multi-threaded

### Restricted Methods

FFM includes restricted methods. Enable native access in build.gradle:

```groovy
test {
  jvmArgs += '--enable-native-access=ALL-UNNAMED'
}
tasks.withType(JavaExec).configureEach {
  jvmArgs += '--enable-native-access=ALL-UNNAMED'
}
```

---

## Common Tasks

### Adding a New Platform Plugin

1. Create a new class implementing `InputDevicePlugin` in `src/main/java/de/gurkenlabs/input4j/foreign/<platform>/`
2. Add the plugin class to `InputDevices.InputLibrary` enum:

```java
public enum InputLibrary {
  PLATFORM_DEFAULT,
  WIN_DIRECTINPUT,
  WIN_XINPUT,
  LINUX_INPUT,
  MACOS_IOKIT;

  public String getPlugin() {
    return switch (this) {
      case PLATFORM_DEFAULT -> InputLibrary.defaultForCurrentOs().getPlugin();
      case WIN_DIRECTINPUT -> "de.gurkenlabs.input4j.foreign.windows.dinput.DirectInputPlugin";
      case WIN_XINPUT -> "de.gurkenlabs.input4j.foreign.windows.xinput.XInputPlugin";
      case LINUX_INPUT -> "de.gurkenlabs.input4j.foreign.linux.LinuxEventDevicePlugin";
      case MACOS_IOKIT -> "de.gurkenlabs.input4j.foreign.macos.iokit.IOKitPlugin";
    };
  }
}
```

3. Add corresponding tests in `src/test/java/`

### Adding a New Predefined Button/Axis

1. Create or extend a class in `src/main/java/de/gurkenlabs/input4j/components/`
2. Use `InputComponent.ID` constructor to define button/axis IDs:

```java
public static final InputComponent.ID A = new InputComponent.ID(Button.BUTTON_0, "A");
public static final InputComponent.ID LEFT_THUMB_X = new InputComponent.ID(Axis.AXIS_X, "LEFT_THUMB_X");
```

### Defining Native Struct Layouts

1. Create a new class in the appropriate platform package
2. Define fields matching the native struct
3. Add static `$LAYOUT` field using `MemoryLayout.structLayout()`
4. Add `VarHandle` for each field using `withName()` for clarity
5. Implement `read()` and `write()` methods

### Platform Detection

Use `PlatformDetector` to detect the current OS:

```java
import de.gurkenlabs.input4j.foreign.util.PlatformDetector;

InputDevices.InputLibrary library = PlatformDetector.detect();
```

---

## Key Interfaces and Classes

| Class | Purpose |
|-------|---------|
| `InputDevices` | Main entry point, initializes plugins |
| `InputDevicePlugin` | Interface for platform plugins |
| `InputDevice` | Represents a controller/gamepad |
| `InputComponent` | Individual button or axis |
| `InputDeviceListener` | Event callbacks for device changes |
| `NativeHelper` | FFM utility for native function calls |
| `PlatformDetector` | OS detection utility |
