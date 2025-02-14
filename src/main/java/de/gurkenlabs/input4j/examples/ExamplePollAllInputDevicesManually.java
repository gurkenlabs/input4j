package de.gurkenlabs.input4j.examples;

import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;
import de.gurkenlabs.input4j.InputDevices;

import javax.swing.*;
import java.io.IOException;

class ExamplePollAllInputDevicesManually {

  /**
   * This example demonstrates how to manually poll all input devices.
   *
   * @param args the arguments
   * @throws IOException if an I/O error occurs
   */
  public static void main(String[] args) throws IOException {

    // Use the default plugin, if you want to use an explicit plugin, use getDirectXPlugin() or specify another InputLibrary explicitly
    var inputDevices = getDefaultPlugin();
    try (inputDevices) {
      while (!inputDevices.getAll().isEmpty()) {
        for (var inputDevice : inputDevices.getAll()) {
          inputDevice.poll();
          if (!inputDevice.hasInputData()) {
            continue;
          }

          System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents().stream().filter(x -> x.getData() != 0).toList());

          // Rumble the device if the X button is pressed
          // handleRumble(inputDevice);
        }

        Thread.sleep(1000);
      }

      System.out.println("Exiting: No input devices found.");
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private static InputDevicePlugin getDefaultPlugin() throws IOException {
    return InputDevices.init();
  }

  private static InputDevicePlugin getDirectXPlugin() throws IOException {
    var frame = new JFrame();
    frame.setVisible(true);
    return InputDevices.init(frame, InputDevices.InputLibrary.WIN_DIRECTINPUT);
  }

  private static void handleRumble(InputDevice inputDevice) {
    inputDevice.getComponent("X").ifPresent(component -> {
      if (component.getData() == 1) {
        inputDevice.rumble(1);
      } else {
        inputDevice.rumble(0);
      }
    });
  }
}