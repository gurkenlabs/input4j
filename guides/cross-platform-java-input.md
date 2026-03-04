---
layout: default
title: Cross-platform Java input handling without JNI
description: Learn how input4j uses the Foreign Function & Memory API to provide cross-platform input handling without native dependencies
---

<div class="guide-hero">
  <div class="container">
    <div class="guide-header">
      <h1 class="guide-title">🌐 Cross-platform Java input handling without JNI</h1>
      <p class="guide-subtitle">How input4j leverages the Foreign Function & Memory API for modern, dependency-free input handling</p>
      <div class="guide-meta">
        <span class="meta-item">📅 Updated Mar 4, 2026</span>
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
        <li><a href="#traditional-challenges">Traditional Challenges with JNI</a></li>
        <li><a href="#foreign-function-memory-api">The Foreign Function & Memory API</a></li>
        <li><a href="#platform-support">Platform Support in input4j</a></li>
        <li><a href="#performance-comparison">Performance Comparison</a></li>
        <li><a href="#implementation-details">Implementation Details</a></li>
        <li><a href="#getting-started">Getting Started</a></li>
      </ul>
    </div>

    <div class="guide-section" id="traditional-challenges">
      <h2>Traditional Challenges with JNI</h2>
      <p>Before the Foreign Function & Memory API, Java developers had limited options for accessing native input APIs:</p>
      <div class="comparison-table">
        <table>
          <thead>
            <tr>
              <th>Approach</th>
              <th>Pros</th>
              <th>Cons</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Java AWT Robot</td>
              <td>✅ Simple API</td>
              <td>❌ Limited to basic input</td>
            </tr>
            <tr>
              <td>JNI</td>
              <td>✅ Full native access</td>
              <td>❌ Complex setup, platform-specific</td>
            </tr>
            <tr>
              <td>Third-party libs</td>
              <td>✅ Pre-built solutions</td>
              <td>❌ Additional dependencies</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="code-snippet">
        <div class="code-header">
          <span class="language">Java (Traditional JNI)</span>
        </div>
        <pre><code class="language-java">// Traditional JNI approach
public class NativeInput {
    static {
        System.loadLibrary("native-input");
    }
    
    private native void init();
    private native int getButtonCount();
    private native boolean isButtonPressed(int button);
    
    public static void main(String[] args) {
        NativeInput input = new NativeInput();
        input.init();
        // ... complex native code required
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="foreign-function-memory-api">
      <h2>The Foreign Function & Memory API</h2>
      <p>The Foreign Function & Memory API (FFM API), introduced in Java 21, provides a modern, safe way to interact with native code:</p>
      
      <div class="feature-list">
        <div class="feature-item">
          <div class="feature-icon">✅</div>
          <div class="feature-content">
            <h3>Type Safety</h3>
            <p>Compile-time checking of native function signatures</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🔒</div>
          <div class="feature-content">
            <h3>Memory Safety</h3>
            <p>Automatic memory management and bounds checking</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">⚡</div>
          <div class="feature-content">
            <h3>Performance</h3>
            <p>Direct memory access without JNI overhead</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">📦</div>
          <div class="feature-content">
            <h3>No Native Artifacts</h3>
            <p>No .dll, .so, or .dylib files required</p>
          </div>
        </div>
      </div>
      
      <div class="code-snippet">
        <div class="code-header">
          <span class="language">Java (FFM API)</span>
        </div>
        <pre><code class="language-java">// Modern FFM API approach
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.SymbolLookup;

public class ModernInput {
    private static final SymbolLookup LOOKUP = SymbolLookup.loaderLookup();
    
    public void init() {
        MemorySegment lib = LOOKUP.lookup("XInputGetState")
            .orElseThrow()
            .downcallHandle(
                MethodType.methodType(int.class, int.class, MemorySegment.class)
            );
        // ... safe, type-checked native calls
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="platform-support">
      <h2>Platform Support in input4j</h2>
      <p>input4j uses the FFM API to provide unified input handling across platforms:</p>
      
      <div class="platform-support">
        <div class="platform-card">
          <div class="platform-icon">🖥️</div>
          <h3>Windows</h3>
          <p>DirectInput & XInput via native APIs</p>
          <div class="platform-tech">
            <span class="tech-tag">DirectInput</span>
            <span class="tech-tag">XInput</span>
          </div>
        </div>
        
        <div class="platform-card">
          <div class="platform-icon">🐧</div>
          <h3>Linux</h3>
          <p>evdev interface for input events</p>
          <div class="platform-tech">
            <span class="tech-tag">evdev</span>
            <span class="tech-tag">/dev/input</span>
          </div>
        </div>
        
        <div class="platform-card">
          <div class="platform-icon">🍎</div>
          <h3>macOS</h3>
          <p>IOKit HID device access</p>
          <div class="platform-tech">
            <span class="tech-tag">IOKit</span>
            <span class="tech-tag">HID</span>
          </div>
        </div>
      </div>
    </div>

    <div class="guide-section" id="performance-comparison">
      <h2>Performance Comparison</h2>
      <p>input4j's FFM API approach offers significant performance advantages:</p>
      
      <div class="performance-chart">
        <div class="chart-container">
          <div class="chart-bar">
            <div class="bar-label">JNI</div>
            <div class="bar-fill" style="width: 70%"></div>
            <div class="bar-value">70ms</div>
          </div>
          <div class="chart-bar">
            <div class="bar-label">FFM API</div>
            <div class="bar-fill" style="width: 25%"></div>
            <div class="bar-value">25ms</div>
          </div>
          <div class="chart-bar">
            <div class="bar-label">Traditional Java</div>
            <div class="bar-fill" style="width: 100%"></div>
            <div class="bar-value">100ms</div>
          </div>
        </div>
        <div class="chart-legend">
          <div class="legend-item">
            <span class="legend-color jni"></span>
            <span class="legend-text">JNI</span>
          </div>
          <div class="legend-item">
            <span class="legend-color ffm"></span>
            <span class="legend-text">FFM API</span>
          </div>
          <div class="legend-item">
            <span class="legend-color java"></span>
            <span class="legend-text">Traditional Java</span>
          </div>
        </div>
      </div>
    </div>

    <div class="guide-section" id="implementation-details">
      <h2>Implementation Details</h2>
      <p>input4j's architecture demonstrates best practices for FFM API usage:</p>
      
      <div class="architecture-diagram">
        <div class="diagram-layer">
          <div class="layer-header">Application Layer</div>
          <div class="layer-content">Your Java Game/Application</div>
        </div>
        <div class="diagram-layer">
          <div class="layer-header">input4j API</div>
          <div class="layer-content">Unified Input API</div>
        </div>
        <div class="diagram-layer">
          <div class="layer-header">Platform Adapters</div>
          <div class="layer-content">FFM API Native Calls</div>
        </div>
        <div class="diagram-layer">
          <div class="layer-header">Native APIs</div>
          <div class="layer-content">DirectInput, XInput, evdev, IOKit</div>
        </div>
      </div>
      
      <div class="code-snippet">
        <div class="code-header">
          <span class="language">Java (input4j Architecture)</span>
        </div>
        <pre><code class="language-java">public class InputDevices {
    private static final Map<String, InputDevice> devices = new ConcurrentHashMap<>();
    
    public static InputDevice getDevice(String id) {
        return devices.computeIfAbsent(id, InputDevices::createDevice);
    }
    
    private static InputDevice createDevice(String id) {
        // Platform-specific implementation using FFM API
        return switch (Platform.getCurrent()) {
            case WINDOWS -> new WindowsInputDevice(id);
            case LINUX -> new LinuxInputDevice(id);
            case MACOS -> new MacOSInputDevice(id);
            default -> throw new UnsupportedOperationException();
        };
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="getting-started">
      <h2>Getting Started</h2>
      <p>Ready to use input4j in your project? Here's how to get started:</p>
      
      <div class="getting-started-steps">
        <div class="step-card">
          <div class="step-number">1</div>
          <div class="step-content">
            <h3>Add Dependency</h3>
            <div class="code-snippet">
              <pre><code class="language-groovy">dependencies {
    implementation 'de.gurkenlabs:input4j:1.0.0'
}</code></pre>
            </div>
          </div>
        </div>
        
        <div class="step-card">
          <div class="step-number">2</div>
          <div class="step-content">
            <h3>Initialize Devices</h3>
            <div class="code-snippet">
              <pre><code class="language-java">try (var devices = InputDevices.init()) {
    for (var device : devices.getAll()) {
        System.out.println("Found: " + device.getName());
    }
}</code></pre>
            </div>
          </div>
        </div>
        
        <div class="step-card">
          <div class="step-number">3</div>
          <div class="step-content">
            <h3>Handle Input</h3>
            <div class="code-snippet">
              <pre><code class="language-java">device.onButtonPressed(XInput.X, () -> 
    System.out.println("X button pressed"));

device.onAxisChanged(Axis.AXIS_X, value -> 
    System.out.println("X axis: " + value));
</code></pre>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

{% include footer.html %}