package de.gurkenlabs.litiengine.input;

import java.util.stream.Collectors;

public class Program {
  public static void main(String[] args) {
    try (var inputDeviceProvider = InputDeviceProvider.init()) {
      inputDeviceProvider.collectDevices();

      while (true) {
        for (var inputDevice : inputDeviceProvider.getDevices()) {
          inputDevice.poll();
          System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents().stream().map(x -> x.getValue()).collect(Collectors.toList()));
        }


        Thread.sleep(1000);
      }

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
