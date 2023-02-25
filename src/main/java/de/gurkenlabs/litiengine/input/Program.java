package de.gurkenlabs.litiengine.input;

public class Program {
  public static void main(String[] args) {
    try (var inputDeviceProvider = InputDeviceProvider.init()) {
      inputDeviceProvider.collectDevices();

      for (var inputDevice : inputDeviceProvider.getDevices()) {
        System.out.println(inputDevice.getInstanceName());

        inputDevice.poll();
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
