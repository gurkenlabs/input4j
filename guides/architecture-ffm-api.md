---
layout: default
title: Internal Architecture - How input4j Uses the FFM API
description: Deep dive into input4j's internal architecture and how the Foreign Function & Memory API enables native input handling without JNI. Learn about MemorySegment, SymbolLookup, platform abstraction, XInput, DirectInput, evdev, IOKit implementations.
keywords: FFM API architecture, Java native input, JNI alternative, MemorySegment, SymbolLookup, native interop Java, XInput implementation, evdev implementation, IOKit implementation, Java native bridge, platform abstraction layer, foreign memory Java
---

<div class="guide-hero">
  <div class="container">
    <div class="guide-header">
      <h1 class="guide-title">🏗️ Internal Architecture</h1>
      <p class="guide-subtitle">How input4j uses the Foreign Function & Memory API for native input handling</p>
      <div class="guide-meta">
        <span class="meta-item">📅 Updated Mar 2026</span>
        <span class="meta-item">📄 15 min read</span>
        <span class="meta-item">⭐ Advanced</span>
      </div>
    </div>
  </div>
</div>

<div class="guide-content">
  <div class="container">
    <div class="guide-toc">
      <h3>Table of Contents</h3>
      <ul>
        <li><a href="#overview">Architecture Overview</a></li>
        <li><a href="#ffm-basics">FFM API Basics</a></li>
        <li><a href="#memory">Memory Management</a></li>
        <li><a href="#platform-layer">Platform Abstraction Layer</a></li>
        <li><a href="#windows">Windows Implementation</a></li>
        <li><a href="#linux">Linux Implementation</a></li>
        <li><a href="#macos">macOS Implementation</a></li>
      </ul>
    </div>

    <div class="guide-section" id="overview">
      <h2>Architecture Overview</h2>
      <p>input4j follows a layered architecture that provides a unified API while leveraging platform-specific native APIs:</p>
      
      <div class="architecture-diagram">
        <div class="diagram-layer">
          <div class="layer-header">Application Layer</div>
          <div class="layer-content">Your Java Game / Application</div>
        </div>
        <div class="diagram-layer">
          <div class="layer-header">Unified API</div>
          <div class="layer-content">InputDevices, InputDevice, InputComponent</div>
        </div>
        <div class="diagram-layer">
          <div class="layer-header">Device Abstraction</div>
          <div class="layer-content">AbstractInputDevicePlugin, VirtualComponentHandler</div>
        </div>
        <div class="diagram-layer">
          <div class="layer-header">FFM Bridge</div>
          <div class="layer-content">Native memory access, function calls</div>
        </div>
        <div class="diagram-layer">
          <div class="layer-header">Native APIs</div>
          <div class="layer-content">XInput, DirectInput, evdev, IOKit</div>
        </div>
      </div>
    </div>

    <div class="guide-section" id="ffm-basics">
      <h2>FFM API Basics</h2>
      <p>The Foreign Function & Memory API (incubator in Java 21, standard in Java 22+) provides two key capabilities:</p>
      
      <div class="feature-list">
        <div class="feature-item">
          <div class="feature-icon">🔌</div>
          <div class="feature-content">
            <h3>Foreign Function Calls</h3>
            <p>Call native functions directly from Java without JNI overhead</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">💾</div>
          <div class="feature-content">
            <h3>Foreign Memory Access</h3>
            <p>Read and write native memory directly with strong safety guarantees</p>
          </div>
        </div>
      </div>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Basic FFM Setup</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">import jdk.incubator.foreign.*;
import jdk.incubator.foreign.MemoryLayouts.*;

public class NativeBridge {
    // Load the native library
    private static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();
    
    // Define function descriptor
    private static final FunctionDescriptor XINPUT_GET_STATE = 
        FunctionDescriptor.of(
            ValueLayout.JAVA_INT,    // return: DWORD (int)
            ValueLayout.JAVA_INT,    // dwUserIndex
            ValueLayout.ADDRESS      // pState (pointer)
        );
    
    // Get function handle
    private static final MethodHandle xInputGetState = 
        LOOKUP.lookup("XInputGetState")
            .orElseThrow()
            .bindTo(XINPUT_GET_STATE);
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="memory">
      <h2>Memory Management</h2>
      <p>FFM API provides safe memory access with automatic bounds checking:</p>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Reading Native Memory</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">// Allocate native memory for XINPUT_STATE structure
MemorySegment stateBuffer = Arena.global().allocate(16);

// Call native function
int result = (int) xInputGetState.invokeExact(0, stateBuffer);

if (result == 0) { // ERROR_SUCCESS
    // Read values from native memory
    int packetNumber = stateBuffer.get(ValueLayout.JAVA_INT, 0);
    short buttons = stateBuffer.get(ValueLayout.JAVA_SHORT, 4);
    byte leftTrigger = stateBuffer.get(ValueLayout.JAVA_BYTE, 6);
    byte rightTrigger = stateBuffer.get(ValueLayout.JAVA_BYTE, 7);
    
    // Read gamepad struct at offset 8
    short leftX = stateBuffer.get(ValueLayout.JAVA_SHORT, 8);
    short leftY = stateBuffer.get(ValueLayout.JAVA_SHORT, 10);
    short rightX = stateBuffer.get(ValueLayout.JAVA_SHORT, 12);
    short rightY = stateBuffer.get(ValueLayout.JAVA_SHORT, 14);
}

// Memory is automatically freed when Arena is closed
// No manual free() calls needed!</code></pre>
      </div>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Struct Layouts</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">// Define structured memory layout
public static final GroupLayout XINPUT_STATE = 
    StructLayout(
        ValueLayout.JAVA_INT.withName("dwPacketNumber"),
        StructLayout(
            ValueLayout.JAVA_SHORT.withName("sThumbLX"),
            ValueLayout.JAVA_SHORT.withName("sThumbLY"),
            ValueLayout.JAVA_SHORT.withName("sThumbRX"),
            ValueLayout.JAVA_SHORT.withName("sThumbRY"),
            ValueLayout.JAVA_BYTE.withName("bLeftTrigger"),
            ValueLayout.JAVA_BYTE.withName("bRightTrigger"),
            ValueLayout.JAVA_SHORT.withName("wButtons")
        ).withName("Gamepad"),
        MemoryLayout.paddingLayout(6)
    ).withName("XINPUT_STATE");

// Access structured data
int packetNumber = state.get(XINPUT_STATE, 0, "dwPacketNumber");
short leftX = state.get(XINPUT_STATE, 0, "Gamepad", "sThumbLX");</code></pre>
      </div>
    </div>

    <div class="guide-section" id="platform-layer">
      <h2>Platform Abstraction Layer</h2>
      <p>input4j uses a plugin-based architecture to support multiple platforms:</p>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Device Plugin Interface</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">public interface InputDevicePlugin {
    String getName();
    Platform getPlatform();
    List&lt;InputDevice&gt; getDevices();
    void update(); // Poll for changes
    void close();
}

public abstract class AbstractInputDevicePlugin implements InputDevicePlugin {
    protected final List&lt;InputDevice&gt; devices = new ArrayList&lt;&gt;();
    protected final Map&lt;String, InputComponent&gt; components = new HashMap&lt;&gt;();
    
    @Override
    public List&lt;InputDevice&gt; getDevices() {
        return Collections.unmodifiableList(devices);
    }
    
    protected void registerComponent(String id, ComponentType type) {
        InputComponent component = new InputComponent(id, type);
        components.put(id, component);
    }
}</code></pre>
      </div>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Device Discovery</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">public class InputDevices {
    private static final List&lt;InputDevicePlugin&gt; PLUGINS = 
        List.of(
            new XInputPlugin(),      // Windows Xbox
            new DirectInputPlugin(),  // Windows legacy
            new LinuxEventDevicePlugin(),  // Linux
            new IOKitPlugin()        // macOS
        );
    
    public static AutoCloseable init() {
        List&lt;InputDevice&gt; allDevices = new ArrayList&lt;&gt;();
        
        for (InputDevicePlugin plugin : PLUGINS) {
            try {
                if (plugin.isAvailable()) {
                    allDevices.addAll(plugin.getDevices());
                }
            } catch (Throwable e) {
                // Platform not supported, skip
                System.err.println("Skipping " + plugin.getName() + ": " + e);
            }
        }
        
        return () -> allDevices.forEach(InputDevice::close);
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="windows">
      <h2>Windows Implementation</h2>
      <p>Windows uses two APIs: XInput (Xbox controllers) and DirectInput (legacy):</p>
      
      <div class="platform-support">
        <div class="platform-card">
          <div class="platform-icon">🎯</div>
          <h3>XInput</h3>
          <p>Modern API for Xbox controllers. 12 buttons, 2 analog sticks, 2 triggers.</p>
          <div class="platform-tech">
            <span class="tech-tag">XInputGetState</span>
            <span class="tech-tag">XInputSetState</span>
          </div>
        </div>
        <div class="platform-card">
          <div class="platform-icon">📺</div>
          <h3>DirectInput</h3>
          <p>Legacy API for older gamepads. More flexible but deprecated.</p>
          <div class="platform-tech">
            <span class="tech-tag">IDirectInput8</span>
            <span class="tech-tag">EnumDevices</span>
          </div>
        </div>
      </div>
    </div>

    <div class="guide-section" id="linux">
      <h2>Linux Implementation</h2>
      <p>Linux uses the evdev interface for reading input events:</p>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Reading evdev Events</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">public class LinuxEventDevice {
    private final MemorySegment eventFD;
    private static final int EV_REL = 0x02;  // Relative axis
    private static final int EV_ABS = 0x03;  // Absolute axis
    private static final int EV_KEY = 0x01;  // Button
    
    // Event structure: time(8) + type(2) + code(2) + value(4) = 16 bytes
    private static final GroupLayout INPUT_EVENT = 
        StructLayout(
            StructLayout(
                ValueLayout.JAVA_LONG.withName("tvSec"),
                ValueLayout.JAVA_LONG.withName("tvUsec")
            ).withName("time"),
            ValueLayout.JAVA_SHORT.withName("type"),
            ValueLayout.JAVA_SHORT.withName("code"),
            ValueLayout.JAVA_INT.withName("value")
        );
    
    public void readEvents() {
        MemorySegment event = Arena.global().allocate(INPUT_EVENT);
        
        // Read from /dev/input/eventX
        int bytesRead = read(eventFD, event, 16);
        
        if (bytesRead > 0) {
            short type = event.get(INPUT_EVENT, 0, "type");
            short code = event.get(INPUT_EVENT, 0, "code");
            int value = event.get(INPUT_EVENT, 0, "value");
            
            processEvent(type, code, value);
        }
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="macos">
      <h2>macOS Implementation</h2>
      <p>macOS uses the IOKit HID framework:</p>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">IOKit HID Access</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">public class IOKitPlugin extends AbstractInputDevicePlugin {
    
    private MemorySegment manager;
    
    @Override
    public void init() {
        // Create HID Manager
        manager = IOKit.HIDManager_Create(kCFAllocatorDefault, MemoryAddress.NULL);
        
        // Set device matching (gamepads only)
        MemorySegment matchingDict = createMatchingDictionary(
            IOHIDDeviceUsagePage.GENERIC_DESKTOP,
            IOHIDDeviceUsage.JOYSTICK
        );
        
        IOKit.HIDManager_SetDeviceMatching(manager, matchingDict);
        IOKit.HIDManager_ScheduleWithRunLoop(manager, CFRunLoopGetMain(), 0);
        IOKit.HIDManager_Open(manager, 0);
    }
    
    private MemorySegment createMatchingDictionary(int usagePage, int usage) {
        // Create CFMutableDictionaryRef
        MemorySegment dict = IOKit.CFDictionary_CreateMutable(
            kCFAllocatorDefault, 0, 
            CFDictionaryKeyCallBacks, CFDictionaryValueCallBacks
        );
        
        // Add matching criteria
        // ... (matching code)
        
        return dict;
    }
}</code></pre>
      </div>
    </div>
  </div>
</div>
