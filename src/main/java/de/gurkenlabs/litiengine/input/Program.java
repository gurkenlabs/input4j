package de.gurkenlabs.litiengine.input;

public class Program {
  public static void main(String[] args) {
    // TODO: update code base accoring to foreign API changes of Java 20: https://openjdk.org/jeps/434
    // TODO: Replace MemoryAddress with long for stored pointers (MemoryAddress are now zero length MemorySegments)
    // TODO: Replace usages of MemorySession with Area/SegmentScope
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
