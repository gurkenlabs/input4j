# AGENTS.md - Input4j Developer Guide

## Tech Stack
- Language: Java 22+
- Build: Gradle
- Test: JUnit Jupiter
- Code style: Spotless (Google Java Format)

## Commands
```bash
./gradlew build              # Build project
./gradlew test               # Run tests
./gradlew spotlessCheck      # Check code style
./gradlew spotlessApply      # Fix code style
./gradlew build -PskipSpotless  # Skip style check (CI)
```

## Boundaries (Three-Tier)

### ✅ Always
- Run `./gradlew build` before finishing

### ⚠️ Ask First
- Modifying CI/config files

### 🚫 Never
- Push to remote (ask user to push)
- Commit secrets or credentials

## Project Structure
```
src/main/java/de/gurkenlabs/input4j/
├── InputDevices.java, InputDevice.java, InputComponent.java
├── components/          # Axis, Button definitions
│   ├── Axis.java        # AXIS_X, AXIS_Y, etc.
│   ├── Button.java     # BUTTON_0, DPAD_UP, etc.
│   ├── XInput.java     # Xbox controller mappings
│   └── DualShock4.java  # PS4 controller mappings
├── foreign/            # Native implementations
│   ├── NativeHelper.java  # FFM utility
│   ├── util/PlatformDetector.java
│   ├── windows/
│   │   ├── xinput/     # XInputPlugin, XINPUT_*.java
│   │   └── dinput/     # DirectInputPlugin
│   ├── linux/
│   │   ├── LinuxEventDevicePlugin.java
│   │   ├── LinuxEventDevice.java
│   │   ├── LinuxInputMappings.java   # Controller mapping
│   │   ├── LinuxEventCode.java       # Linux event codes
│   │   ├── LinuxComponentType.java
│   │   └── input_event.java
│   └── macos/iokit/
│       ├── IOKitPlugin.java
│       └── IOHIDDevice.java
└── examples/
```

## Platform Plugins
| Platform | API | Plugin Class |
|----------|-----|--------------|
| Linux | evdev | `LinuxEventDevicePlugin` |
| Windows | XInput | `XInputPlugin` |
| Windows | DirectInput | `DirectInputPlugin` |
| macOS | IOKit | `IOKitPlugin` |

## Code Style Guidelines
- All public classes `final` unless designed for extension
- Use `var` when type is obvious from right side
- Use method references (`Object::method`) over lambdas where possible
- Always use braces for control structures
- Max line length: 100 characters (Spotless)
- Run `spotlessApply` before commit

### Import Order (Spotless)
1. `java.*` imports
2. External library imports
3. `de.gurkenlabs.*` imports
4. Static imports

### Error Handling
- `IllegalArgumentException` for invalid arguments
- `IllegalStateException` for invalid state
- Log errors before returning null/throwing
- Return empty `Optional` instead of null

### Thread Safety
- Use `ConcurrentHashMap`, `CopyOnWriteArrayList`
- Use final fields where possible

### Test Conventions
- Test class: `<ClassName>Tests`
- Method: `<methodName>_<scenario>`
- Use `@BeforeEach` for setup

## Key Interfaces
| Class | Purpose |
|-------|---------|
| `InputDevices` | Main entry point, initializes plugins |
| `InputDevicePlugin` | Plugin interface |
| `InputDevice` | Controller representation |
| `InputComponent` | Button or axis |
| `InputDeviceListener` | Device connect/disconnect events |
| `LinuxInputMappings` | Linux button/axis mapping registry |
| `LinuxEventCode` | Linux event code constants |

## FFM API (Java 22+)

The FFM API (java.lang.foreign) enables Java programs to interoperate with native code without JNI. It became permanent in Java 22 (JEP 454).

### Native Helper Usage
Use `NativeHelper` for creating downcall handles:
```java
MethodHandle handle = NativeHelper.downcallHandle(
    "XInputGetState",
    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS)
);
```

### Memory Layouts
Use `$LAYOUT` naming convention:
```java
static final MemoryLayout $LAYOUT = MemoryLayout.structLayout(
    ValueLayout.JAVA_INT.withName("fieldName"),
    ...
);
```

### VarHandle for Fields
```java
private static final VarHandle VH_fieldName =
    $LAYOUT.varHandle(PathElement.groupElement("fieldName"));
```

### Reading Structs
```java
static XINPUT_STATE read(MemorySegment segment) {
    var state = new XINPUT_STATE();
    state.fieldName = (int) VH_fieldName.get(segment, 0);
    return state;
}
```

### Memory Management
- Use `Arena` for memory lifecycle
- Try-with-resources for automatic cleanup
- `Arena.ofConfined()` for single-threaded, `Arena.ofShared()` for multi-threaded

### Native Access
Enable in build.gradle:
```groovy
test { jvmArgs += '--enable-native-access=ALL-UNNAMED' }
tasks.withType(JavaExec).configureEach { jvmArgs += '--enable-native-access=ALL-UNNAMED' }
```

## Linux Controller Mappings

Linux evdev exposes raw event codes that vary between manufacturers. Some controllers (e.g., GameSir) use `BTN_0-BTN_9` instead of standard `BTN_SOUTH-BTN_WEST`.

### Usage
```java
import de.gurkenlabs.input4j.foreign.linux.LinuxInputMappings;
import de.gurkenlabs.input4j.components.Button;
import de.gurkenlabs.input4j.foreign.linux.LinuxEventCode;

// Register custom mapping
LinuxInputMappings.registerButtonMapping(
    0x05AC, 0x03DD,     // VID, PID
    "GameSir G3",       // device name pattern (% for prefix)
    LinuxEventCode.BTN_0,
    Button.BUTTON_0
);

// Lookup
Optional<InputComponent.ID> id = LinuxInputMappings.getButtonMapping(
    vendorId, productId, deviceName, linuxEventCode);
```

### Built-in Mappings
The system includes mappings for GameSir, 8BitDo, DragonRise, Sony, Nintendo, and generic controllers.

### Adding New Mappings
Add to `LinuxInputMappings.loadBuiltInMappings()` method.

## Common Tasks

### Adding a New Platform Plugin
1. Create a new class implementing `InputDevicePlugin` in `src/main/java/de/gurkenlabs/input4j/foreign/<platform>/`
2. Add the plugin class to `InputDevices.InputLibrary` enum
3. Add tests in `src/test/java/`

### Adding a New Predefined Button/Axis
Create or extend a class in `src/main/java/de/gurkenlabs/input4j/components/`:
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