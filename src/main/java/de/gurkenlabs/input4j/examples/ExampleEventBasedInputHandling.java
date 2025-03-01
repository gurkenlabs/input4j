package de.gurkenlabs.input4j.examples;

import de.gurkenlabs.input4j.InputDevices;
import de.gurkenlabs.input4j.components.Axis;
import de.gurkenlabs.input4j.components.XInput;

import java.io.IOException;

public class ExampleEventBasedInputHandling {
  public static void main(String[] args) throws IOException, InterruptedException {
    try (var devices = InputDevices.init()) {

      devices.onDeviceConnected(inputDevice -> System.out.println("Device connected " + inputDevice));
      devices.onDeviceDisconnected(inputDevice -> System.out.println("Device disconnected " + inputDevice));
      var device = devices.getAll().stream().findFirst().orElse(null);
      if (device == null) {
        System.out.println("No input devices found.");
        return;
      }

      device.onInputValueChanged(e ->
        System.out.println("Value changed: " + e.component() + ": " + e.oldValue() + " -> " + e.newValue())
      );

      device.onButtonPressed(XInput.X, () ->
        System.out.println("X button pressed")
      );

      device.onButtonReleased(XInput.X, () ->
        System.out.println("X button released")
      );

      device.onButtonPressed(0, () ->
        System.out.println("Button 0 pressed")
      );

      device.onAxisChanged(Axis.AXIS_X, value ->
        System.out.println("X axis value: " + value)
      );

      device.onAxisChanged(3, value ->
        System.out.println("Axis 3 value: " + value)
      );

      // simulate external polling loop
      while (true) {
        device.poll();
        Thread.sleep(1000);
      }
    }
  }
}
