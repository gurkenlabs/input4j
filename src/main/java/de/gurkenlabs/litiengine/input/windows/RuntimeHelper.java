package de.gurkenlabs.litiengine.input.windows;

import java.lang.foreign.Linker;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.MemoryAddress;
import java.lang.invoke.MethodHandle;

final class RuntimeHelper {

    private RuntimeHelper() {}
    private final static Linker LINKER = Linker.nativeLinker();

    private final static SymbolLookup SYMBOL_LOOKUP;

    static {
        System.loadLibrary("setupapi");
        System.loadLibrary("Kernel32");
        System.loadLibrary("dinput8");

        SymbolLookup loaderLookup = SymbolLookup.loaderLookup();
        SYMBOL_LOOKUP = name -> loaderLookup.lookup(name).or(() -> LINKER.defaultLookup().lookup(name));
    }

    static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
        return SYMBOL_LOOKUP.lookup(name).
                map(addr -> LINKER.downcallHandle(addr, fdesc)).
                orElse(null);
    }

    static MethodHandle downcallHandle(MemoryAddress address, FunctionDescriptor fdesc) {
        return LINKER.downcallHandle(address, fdesc);
    }
}
