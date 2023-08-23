# Input4j 

**Input4j** is a free and pure Java input library based on the **Foreign Function & Memory API** (FFM API - *previously known as 
"Project Panama"*). This library doesn't have any additional native artifacts and directly interoperates with 
platform native libraries to access input devices.

> ⚠ At the moment, the FFM API is in its second preview ([JEP 434](https://bugs.openjdk.org/browse/JDK-8301625)) with **Java 20** 
> and reaches the third preview ([JEP 442](https://openjdk.org/jeps/442)) with **Java 21** .
> The Input4j library will be updated accordingly with all the required changes until the official release of the FFM API.
> Naturally, this library is only available for the most recent Java versions 
> 
> Currently required version: **Java 20**

## 🎮 Main Features
...
## ⚙️ Installation
...

## 💻 Code examples

```java
// iterate all available input devices and poll their data every second
try (var inputDeviceProvider = InputDevices.init()) {
    while (true) {
        for (var inputDevice : inputDeviceProvider.getDevices()) {
            inputDevice.poll();

            // print all devices and polled data to the console
            System.out.println(inputDevice.getInstanceName());
            System.out.println("\t" + inputDevice.getComponents());
        }
        
        Thread.sleep(1000);
    }
}
```

## 📦 Supported input APIs
 * **Windows**: DirectInput ✅
 * **Windows**: XInput 🚧
 * **Linux**: Linux Input 🚧
 * **OSX**: IOKIT 🚧
 * **OSX**: Game Controller 🚧

