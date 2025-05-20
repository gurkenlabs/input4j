# 🎮 Input4j - The pure Java input library


**Input4j** is a cutting-edge, pure Java input library leveraging the **Foreign Function & Memory API** (FFM API), offering cross-platform support, high performance, flexible controller and gamepad input handling, and future-proofing without the need for additional native artifacts, making it the **best choice for Java developers**.

Join the revolution in Java input handling with Input4j and experience unparalleled performance and ease of use!

## 🚀 Key Features
- **Cross-Platform Input handling**: Fully compatible with Windows, Linux, and OSX (in progress).
- **Performance**: Optimized for high performance with minimal overhead.
- **Flexible Input Handling**: Supports both polling and event-based input mechanisms.
- **Simplicity**: Easy to integrate and use with straightforward APIs.
- **Future-Proof**: Built on the latest Java technologies, ensuring long-term support and compatibility.

### 🆚 Advantages over traditional java input libraries
Input4j offers several advantages over traditional libraries like JInput by leveraging the new Foreign Function & Memory API (FFM API) instead of using Java Native Interface (JNI).
It is a more modern and efficient JNI alternative and an easy way to interact with native input libraries, providing the following benefits:

- No Native Artifacts & No Dependencies: Direct interaction with native libraries without the need for additional native artifacts simplifies the build and deployment process.
- Performance: Based on the FFM API, this library reduces the overhead associated with native calls, resulting in significantly faster performance then Java JNI.
- Safety: Safer memory management and access patterns reduce the risk of memory leaks and buffer overflows.
- Ease of Use: A more straightforward and modern API makes it easier to write and maintain code.
- Future-Proof: Built on the latest Java technologies, ensuring long-term support and compatibility with future Java versions.


## 📦️ Installation
Add the following dependency to your `build.gradle` file to start using Input4j:

```groovy
dependencies {
  implementation 'de.gurkenlabs:input4j:1.0.0'
}
```

## 💻 Quick start guide

### Manually Polling and Reading Input Data
```java
try (var inputDevices = InputDevices.init()) {
  while (!inputDevices.getAll().isEmpty()) {
    // iterate all available input devices and poll their data every second
    for (var inputDevice : inputDevices.getAll()) {
      inputDevice.poll();
      System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents());
    }

    Thread.sleep(1000);
  }
}
```
### Event-Based Input Handling
```java
try (var devices = InputDevices.init()) {
  var device = devices.getAll().stream().findFirst().orElse(null);
  if (device == null) {
    System.out.println("No input devices found.");
    return;
  }

  device.onInputValueChanged(e -> System.out.println("Value changed: " + e.component() + " -> " + e.newValue()));
  device.onButtonPressed(XInput.X, () -> System.out.println("X button pressed"));
  device.onAxisChanged(Axis.AXIS_X, value -> System.out.println("X axis: " + value));

  // simulate external polling loop
  while (true) {
    device.poll();
    Thread.sleep(1000);
  }
}
```

## 🔌 Technical Details

### Platform-Specific Input APIs
- **Windows: DirectInput  ✅**
    - Full implementation using `dinput.h`
    - Supports legacy and modern input devices

- **Windows: XInput ✅**
    - Modern gamepad support via `xinput.h`
    - Xbox controller compatibility

- **Linux: evdev ✅**
    - Event interface via `/dev/input`

- **macOS: IOKit ✅**
    - HID device provisioning via `IOHIDManager`

### System Requirements
- Java Runtime: 22+
