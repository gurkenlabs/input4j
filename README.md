# Input4j 

**Input4j** is a free and pure Java input library based on the **Foreign Function & Memory API** (FFM API). 
This library doesn't have any additional native artifacts and directly interoperates with 
platform native libraries to access input devices.

> ⚠ The FFM API has been finalized with ([JEP 454](https://openjdk.org/jeps/454)) with **Java 22**. Naturally, this library is only available for the most recent Java versions.
> 
> Required version: **Java 22**

## 🎮 Main Features
...
## ⚙️ Installation
...

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

