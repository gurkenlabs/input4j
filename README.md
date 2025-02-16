# 🎮 Input4j - The pure Java input library


**Input4j** is a cutting-edge, pure Java input library leveraging the **Foreign Function & Memory API** (FFM API), offering cross-platform support, high performance, flexible input handling, and future-proofing without the need for additional native artifacts, making it the **best choice for Java developers**.

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

- No Native Artifacts: Direct interaction with native libraries without the need for additional native artifacts simplifies the build and deployment process.
- Performance: The FFM API reduces the overhead associated with native calls, resulting in better performance.
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

## 🔌 Technical Details

### Platform-Specific Input APIs
- **DirectInput (Windows) ✅**
    - Full implementation using `dinput.h`
    - Supports legacy and modern input devices

- **XInput (Windows) ✅**
    - Modern gamepad support via `xinput.h`
    - Xbox controller compatibility

- **evdev (Linux) ✅**
    - Event interface via `/dev/input`
    - Raw input device access

- **IOKit (macOS) 🚧**
    - HID device enumeration
    - Framework: IOKit.framework

- **Game Controller (macOS) 🚧**
    - Planned implementation
    - Framework: GameController.framework

### System Requirements
- Java Runtime: 22+