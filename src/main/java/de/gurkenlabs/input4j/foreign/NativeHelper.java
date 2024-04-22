package de.gurkenlabs.input4j.foreign;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

public final class NativeHelper {

  public static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
    return SymbolLookup.loaderLookup().find(name).or(() -> Linker.nativeLinker().defaultLookup().find(name)).
            map(addr -> Linker.nativeLinker().downcallHandle(addr, fdesc)).
            orElse(null);
  }

  public static MethodHandle downcallHandle(MemorySegment address, FunctionDescriptor fdesc) {
    return Linker.nativeLinker().downcallHandle(address, fdesc);
  }

  public static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc, String captureCallState){
    Linker.Option ccs = Linker.Option.captureCallState(captureCallState);
    return Linker.nativeLinker().downcallHandle(
            SymbolLookup.loaderLookup().find(name).or(() -> Linker.nativeLinker().defaultLookup().find(name)).orElseThrow(),
            fdesc,
            ccs);
  }
}
