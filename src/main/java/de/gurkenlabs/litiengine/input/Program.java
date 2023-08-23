package de.gurkenlabs.litiengine.input;

public class Program {
  public static void main(String[] args) {
    try (var inputDeviceProvider = InputDeviceProvider.init()) {
      while (true) {
        for (var inputDevice : inputDeviceProvider.getDevices()) {
          inputDevice.poll();
          System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents().stream().toList());
        }


        Thread.sleep(1000);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
