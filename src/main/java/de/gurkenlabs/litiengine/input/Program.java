package de.gurkenlabs.litiengine.input;

public class Program {
  public static void main(String[] args) {
    try {
      InputEnviroment.init();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
