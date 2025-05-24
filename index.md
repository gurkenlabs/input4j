---
layout: default
title: input4j
---

# 🎮 input4j

A lightweight, cross-platform Java library for unified input device handling.

## ✨ Features
- Supports Windows, Linux, and macOS
- Unified API for gamepads, joysticks, and other input devices
- Event-based and polling input handling

## 🚦 Getting Started
Add input4j to your Java project and start handling input devices easily.

## 💡 Example
```java
InputDevices.update();
for (InputDevice device : InputDevices.getAll()) {
    System.out.println(device.getName());
}
```

## 🆚 Advantages over traditional Java input libraries
Input4j leverages the new Foreign Function & Memory API (FFM API) instead of JNI, offering:
- No native artifacts or dependencies
- Faster performance than JNI
- Safer memory management
- Modern, easy-to-use API
- Future-proof for new Java versions

## 📦 Installation
Add the following to your `build.gradle`:
```groovy
dependencies {
  implementation 'de.gurkenlabs:input4j:1.0.0'
}
```

## ⚡ Quick Start Guide
### 🔄 Manually Polling and Reading Input Data
```java
try (var inputDevices = InputDevices.init()) {
  while (!inputDevices.getAll().isEmpty()) {
    for (var inputDevice : inputDevices.getAll()) {
      inputDevice.poll();
      System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents());
    }
    Thread.sleep(1000);
  }
}
```
### 📨 Event-Based Input Handling
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
  while (true) {
    device.poll();
    Thread.sleep(1000);
  }
}
```
## 🚀 Technical Details
- 🪟 **Windows: DirectInput** (dinput.h) – legacy & modern devices
- 🎮 **Windows: XInput** (xinput.h) – Xbox controller support
- 🐧 **Linux: evdev** – /dev/input event interface
- 🍏 **macOS: IOKit** – HID device provisioning

## 🖥️ System Requirements
- ☕ Java Runtime: 22+

## 📝 Details about the Project

Input4j is a modern, open-source Java library designed to simplify and unify input device management for desktop applications and games. With Input4j, developers can easily access and handle a wide range of input devices such as gamepads, joysticks, and controllers across multiple platforms. The library is built for high performance and reliability, making it ideal for both professional and hobbyist Java developers who want seamless input integration without the hassle of native dependencies.

Input4j stands out due to its use of the latest Java technologies, including the Foreign Function & Memory API (FFM API), ensuring compatibility with current and future Java versions. Its intuitive API, cross-platform support, and focus on safety and speed make it the best choice for anyone looking to add robust input support to their Java projects. Whether you are developing a game, simulation, or any interactive application, Input4j provides the tools you need for responsive and flexible input handling.

---

<p style="text-align:center; color: #bbb; font-size: 0.9em;">
  Powered by <a href="https://litiengine.com" style="color: #7fd7ff;">LITIENGINE</a>
</p>
