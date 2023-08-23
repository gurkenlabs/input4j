package de.gurkenlabs.input4j;

public class Program {
  public static void main(String[] args) throws Exception {
    try (var inputDeviceProvider = InputDevices.init()) {
      while (true) {
        for (var inputDevice : inputDeviceProvider.getDevices()) {
          inputDevice.poll();
          System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents());
        }

        Thread.sleep(1000);
      }
    }
  }
}