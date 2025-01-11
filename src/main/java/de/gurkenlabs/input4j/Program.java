package de.gurkenlabs.input4j;

public class Program {
  public static void main(String[] args) throws Exception {
    try (var inputDevices = InputDevices.init()) {
      while (!inputDevices.getAll().isEmpty()) {
        for (var inputDevice : inputDevices.getAll()) {
          inputDevice.poll();
          System.out.println(inputDevice.getInstanceName() + ":" + inputDevice.getComponents().stream().filter(x -> x.getData() != 0).toList());

          // Rumble the device if the X button is pressed
          // handleRumble(inputDevice);
        }

        Thread.sleep(1000);
      }

      System.out.println("Exiting: No input devices found.");
    }
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