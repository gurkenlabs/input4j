package de.gurkenlabs.input4j;

import javax.swing.*;

public class Program {
  public static void main(String[] args) throws Exception {

    var owner = new JFrame("Input4J Test");
    owner.setVisible(true);
    try (var inputDevices = InputDevices.init(owner)) {
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