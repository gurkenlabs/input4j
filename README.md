# Input4j 

**Input4j** is a free and pure Java input library based on the **Foreign Function & Memory API** (FFM API). 
This library doesn't have any additional native artifacts and directly interoperates with 
platform native libraries to access input devices.

> ⚠ Minimum required Java version: **Java 22**
> 
> The FFM API has been finalized with ([JEP 454](https://openjdk.org/jeps/454)) with **Java 22**.

## 🎮 Main Features
- **Cross-platform support**: Works on Windows, Linux, and OSX (in progress).
- **No additional native artifacts**: Directly interoperates with platform native libraries.
- **Polling and event-based input**: Supports both polling and event-based input mechanisms.
- **Extensible**: Easily extendable to support new input devices and platforms.

## ⚙️ Installation
To use Input4j in your project, add the following dependency to your `build.gradle` file:

```groovy
dependencies {
  implementation 'de.gurkenlabs:input4j:1.0.0'
}
```

## 💻 Code examples

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

## 📦 Supported input APIs
 * **Windows**: DirectInput ✅
 * **Windows**: XInput ✅
 * **Linux**: Linux Input (evdev) 🚧
 * **OSX**: IOKIT ❌
 * **OSX**: Game Controller ❌

