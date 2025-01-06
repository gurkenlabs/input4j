package de.gurkenlabs.input4j.foreign.windows.dinput;

import java.awt.*;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandle;

import static de.gurkenlabs.input4j.foreign.NativeHelper.downcallHandle;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

/**
 * The `WindowHelper` class provides utility methods for interacting with Windows OS window handles (HWND).
 * It uses the `User32` library to find window handles based on window titles.
 */
public final class WindowHelper {
  private static final MethodHandle findWindowA;

  static {
    System.loadLibrary("User32");

    // Initialize the MethodHandle for the FindWindowA function from User32.dll
    findWindowA = downcallHandle("FindWindowA", FunctionDescriptor.of(JAVA_LONG, ADDRESS, ADDRESS));
  }

  /**
   * Retrieves the window handle (HWND) for the specified AWT Frame.
   *
   * @param window The AWT Frame for which to find the window handle.
   * @return The window handle (HWND) as a long value.
   */
  public static long getHWND(Frame window) {
    var currentTitle = window.getTitle();

    // temporarily set unique window name to ensure that the right window is found
    var windowSearchTitle = System.nanoTime() + " @" + ProcessHandle.current().pid();
    window.setTitle(windowSearchTitle);
    try (var memorySession = Arena.ofConfined()) {
      // Allocate memory for the window title (LPSTR)
      MemorySegment lpWindowName = memorySession.allocateFrom(window.getTitle());

      // Call FindWindow with className = NULL (null pointer in Java)
      return (long) findWindowA.invoke(MemorySegment.NULL, lpWindowName);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    } finally {
      // Restore the original window title
      window.setTitle(currentTitle);
    }
  }
}
