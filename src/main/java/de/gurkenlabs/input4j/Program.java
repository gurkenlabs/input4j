package de.gurkenlabs.input4j;

public class Program {
  public static void main(String[] args) throws Exception {
    try (var inputDevices = InputDevices.init()) {
      while (!inputDevices.getAll().isEmpty()) {
        for (var inputDevice : inputDevices.getAll()) {
          inputDevice.poll();
          System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents());
        }

        Thread.sleep(1000);
      }
    }
  }
}