---
layout: default
title: LITIENGINE Case Study - Replacing JInput with Input4j
description: Learn how LITIENGINE migrated from JInput to Input4j for gamepad support, eliminating native dependencies and improving cross-platform compatibility.
keywords: LITIENGINE Input4j, JInput replacement, Java gamepad migration, LITIENGINE game controller, input4j game engine integration
---

<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "BreadcrumbList",
  "itemListElement": [
    {
      "@type": "ListItem",
      "position": 1,
      "name": "Home",
      "item": "https://gurkenlabs.github.io/input4j/"
    },
    {
      "@type": "ListItem",
      "position": 2,
      "name": "Docs",
      "item": "https://gurkenlabs.github.io/input4j/docs/"
    },
    {
      "@type": "ListItem",
      "position": 3,
      "name": "LITIENGINE Case Study",
      "item": "https://gurkenlabs.github.io/input4j/docs/litiengine-input4j-migration"
    }
  ]
}
</script>

<div class="hero">
  <div class="container">
    <h1 class="hero-title">LITIENGINE Case Study</h1>
    <p class="hero-subtitle">How LITIENGINE replaced JInput with Input4j for gamepad support</p>
  </div>
</div>

<div class="section">
  <div class="container">
    <h2 class="section-title">From JInput to Input4j</h2>
    
    <p class="section-subtitle">
      LITIENGINE, the pure 2D Java game engine, migrated from JInput to Input4j in version 0.11.0. 
      This migration eliminated the need for native third-party binaries and significantly improved cross-platform support.
    </p>

    <div class="guide-toc">
      <h3>In this guide:</h3>
      <ul>
        <li><a href="#background">Background: The JInput Challenge</a></li>
        <li><a href="#migration">The Migration to Input4j</a></li>
        <li><a href="#benefits">Benefits of Using Input4j</a></li>
        <li><a href="#code-examples">Code Examples</a></li>
        <li><a href="#how-litiengine-uses-input4j">How LITIENGINE Uses Input4j</a></li>
      </ul>
    </div>
  </div>
</div>

<div class="section" id="background">
  <div class="container">
    <h2 class="section-title">Background: The JInput Challenge</h2>
    
    <p>
      LITIENGINE originally used <strong>JInput</strong>, a popular Java library for game controller input. 
      However, JInput has several significant drawbacks that led the LITIENGINE team to seek an alternative:
    </p>

    <div class="cards" style="margin-top: var(--space-lg);">
      <div class="card">
        <div class="card-icon">🔧</div>
        <h3 class="card-title">Native Dependencies</h3>
        <p class="card-text">JInput requires native DLL/SO files (jinput.dll, libjinput.so, libjinput.dylib) to be distributed with the application, complicating deployment.</p>
      </div>
      
      <div class="card">
        <div class="card-icon">🐛</div>
        <h3 class="card-title">Maintenance Issues</h3>
        <p class="card-text">JInput is no longer actively maintained, with the last major updates years ago, leading to compatibility issues with newer Java versions.</p>
      </div>
      
      <div class="card">
        <div class="card-icon">🔨</div>
        <h3 class="card-title">JNI Complexity</h3>
        <p class="card-text">JInput uses traditional JNI (Java Native Interface), which requires writing C code and managing native build toolchains.</p>
      </div>
    </div>
  </div>
</div>

<div class="section section-alt" id="migration">
  <div class="container">
    <h2 class="section-title">The Migration to Input4j</h2>
    
    <p>
      In <strong>LITIENGINE 0.11.0</strong>, the team replaced JInput with Input4j, their own custom input library built on the 
      <strong>Foreign Function & Memory API (FFM API)</strong>. This migration was a major milestone that:
    </p>

    <ul style="margin-top: var(--space-lg); margin-left: var(--space-lg);">
      <li style="margin-bottom: var(--space-md);">Eliminated all native third-party binaries from the engine distribution</li>
      <li style="margin-bottom: var(--space-md);">Improved cross-platform support for Windows, Linux, and macOS</li>
      <li style="margin-bottom: var(--space-md);">Enabled the engine to leverage modern Java features</li>
      <li style="margin-bottom: var(--space-md);">Simplified the build and release process</li>
    </ul>

    <div class="code-block" style="margin-top: var(--space-xl);">
      <div class="code-header">
        <span class="code-lang">Gradle</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-gradle">// LITIENGINE 0.11.0+ uses Input4j automatically
dependencies {
  implementation 'de.gurkenlabs:litiengine:0.11.1'
}</code></pre>
    </div>

    <p style="margin-top: var(--space-lg);">
      When you use LITIENGINE 0.11.0 or later, Input4j is automatically included as a transitive dependency, 
      providing seamless gamepad support without any additional configuration.
    </p>
  </div>
</div>

<div class="section" id="benefits">
  <div class="container">
    <h2 class="section-title">Benefits of Using Input4j</h2>
    
    <p>
      Input4j provides several advantages over traditional JNI-based libraries like JInput:
    </p>

    <div class="cards" style="margin-top: var(--space-lg);">
      <div class="card">
        <div class="card-icon">📦</div>
        <h3 class="card-title">No Native Artifacts</h3>
        <p class="card-text">Input4j uses FFM API instead of JNI, eliminating the need for DLLs, SOs, or DYLIBs. Just add the JAR to your classpath.</p>
      </div>
      
      <div class="card">
        <div class="card-icon">🌍</div>
        <h3 class="card-title">Cross-Platform</h3>
        <p class="card-text">Single codebase works on Windows (XInput/DirectInput), Linux (evdev), and macOS (IOKit/HID).</p>
      </div>
      
      <div class="card">
        <div class="card-icon">⚡</div>
        <h3 class="card-title">High Performance</h3>
        <p class="card-text">Direct native calls without JNI overhead provide low-latency input handling for real-time gaming.</p>
      </div>
      
      <div class="card">
        <div class="card-icon">🔒</div>
        <h3 class="card-title">Type Safety</h3>
        <p class="card-text">FFM API provides compile-time type checking for native function calls, reducing runtime errors.</p>
      </div>
      
      <div class="card">
        <div class="card-icon">🚀</div>
        <h3 class="card-title">Future-Proof</h3>
        <p class="card-text">Built on standardized Java technology (FFM API in Java 22+) ensuring long-term support.</p>
      </div>
      
      <div class="card">
        <div class="card-icon">🎮</div>
        <h3 class="card-title">Modern API</h3>
        <p class="card-text">Clean, intuitive API with both polling and event-based input handling patterns.</p>
      </div>
    </div>
  </div>
</div>

<div class="section section-alt" id="code-examples">
  <div class="container">
    <h2 class="section-title">Code Examples</h2>
    
    <p>
      Here are examples of how Input4j provides gamepad input handling, the same functionality that LITIENGINE leverages:
    </p>

    <h3 style="margin-top: var(--space-xl);">Polling Input</h3>
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">try (var inputDevices = InputDevices.init()) {
  while (gameIsRunning) {
    for (var device : inputDevices.getAll()) {
      device.poll();
      
      // Check analog sticks
      float leftX = device.getComponentValue(Axis.AXIS_X);
      float leftY = device.getComponentValue(Axis.AXIS_Y);
      
      // Check buttons
      boolean aPressed = device.getComponentValue(Button.BUTTON_0) > 0;
      
      // Use values for game input
      if (Math.abs(leftX) > 0.1f) {
        player.moveX(leftX * speed);
      }
    }
    Thread.sleep(16); // ~60 FPS polling
  }
}</code></pre>
    </div>

    <h3 style="margin-top: var(--space-xl);">Event-Based Input</h3>
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">try (var devices = InputDevices.init()) {
  var device = devices.getAll().get(0);
  
  // Listen for button presses
  device.onButtonPressed(Button.BUTTON_0, () -> {
    System.out.println("A button pressed!");
    player.jump();
  });
  
  // Listen for axis changes
  device.onAxisChanged(Axis.AXIS_Y, value -> {
    System.out.println("Left stick Y: " + value);
  });
  
  // Handle controller disconnect
  device.onDisconnected(() -> {
    System.out.println("Controller disconnected!");
  });
  
  // Keep application running
  while (device.isConnected()) {
    Thread.sleep(100);
  }
}</code></pre>
    </div>

    <h3 style="margin-top: var(--space-xl);">XInput Support (Xbox Controllers)</h3>
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">// Xbox controller specific mappings
device.onButtonPressed(XInput.A, () -> jump());
device.onButtonPressed(XInput.B, () -> attack());
device.onButtonPressed(XInput.X, () -> interact());
device.onButtonPressed(XInput.Y, () -> menu());

// Triggers and vibration
device.onAxisChanged(XInput.TRIGGER_LEFT, value -> {
  // Left trigger - typically used for aim/sprint
});
device.onAxisChanged(XInput.TRIGGER_RIGHT, value -> {
  // Right trigger - typically used for fire/shoot
});

// Rumble/vibration feedback
device.rumble(0.5f, 0.5f); // left motor, right motor (0.0 - 1.0)</code></pre>
    </div>
  </div>
</div>

<div class="section" id="how-litiengine-uses-input4j">
  <div class="container">
    <h2 class="section-title">How LITIENGINE Uses Input4j</h2>
    
    <p>
      LITIENGINE wraps Input4j with a high-level API that makes gamepad handling simple for game developers. 
      Here's how you use gamepads in LITIENGINE:
    </p>

    <h3 style="margin-top: var(--space-xl);">Basic Gamepad Input</h3>
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">// Access gamepads through LITIENGINE's Input API
GamepadManager gamepads = Input.gamepads();

// Get the currently connected gamepad
Gamepad gamepad = gamepads.current();

if (gamepad != null) {
  // Check analog stick values for movement
  float leftStickX = gamepad.getAxisValue(Gamepad.Axis.LEFT_STICK_X);
  float leftStickY = gamepad.getAxisValue(Gamepad.Axis.LEFT_STICK_Y);
  
  // Apply to entity movement
  entity.setVelocityX(leftStickX * speed);
  entity.setVelocityY(leftStickY * speed);
}</code></pre>
    </div>

    <h3 style="margin-top: var(--space-xl);">Event-Based Gamepad Input</h3>
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">// Listen for button presses
Input.gamepads().onPressed((button, value) -> {
  if (button.equals(Gamepad.Xbox.A)) {
    player.jump();
  } else if (button.equals(Gamepad.Xbox.B)) {
    player.attack();
  } else if (button.equals(Gamepad.Xbox.X)) {
    player.interact();
  } else if (button.equals(Gamepad.Xbox.Y)) {
    openMenu();
  }
});

// Listen for button releases
Input.gamepads().onReleased((button) -> {
  if (button.equals(Gamepad.Xbox.RT)) {
    player.stopFiring();
  }
});

// Continuous polling for analog inputs
Input.gamepads().onPoll(pollValue -> {
  float leftStickY = pollValue.getValue(Gamepad.Axis.LEFT_STICK_Y);
  if (leftStickY > 0) {
    player.moveDown(leftStickY);
  } else if (leftStickY < 0) {
    player.moveUp(Math.abs(leftStickY));
  }
});</code></pre>
    </div>

    <h3 style="margin-top: var(--space-xl);">Gamepad Detection</h3>
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">// Detect when gamepads are connected
Input.gamepads().onGamepadAdded(gamepad -> {
  System.out.println("Gamepad connected: " + gamepad.getName());
  System.out.println("Type: " + gamepad.getType());
});

// Detect when gamepads are disconnected
Input.gamepads().onGamepadRemoved(gamepad -> {
  System.out.println("Gamepad disconnected: " + gamepad.getName());
});

// Get all connected gamepads
for (Gamepad gamepad : Input.gamepads().getAll()) {
  System.out.println("Found: " + gamepad.getName());
}</code></pre>
    </div>

    <h3 style="margin-top: var(--space-xl);">Supported Controller Types</h3>
    <p>
      LITIENGINE automatically detects and supports various controller types through Input4j:
    </p>
    
    <ul style="margin-top: var(--space-md); margin-left: var(--space-lg);">
      <li style="margin-bottom: var(--space-sm);"><strong>Xbox controllers</strong> - Xbox One, Series X/S (via XInput on Windows)</li>
      <li style="margin-bottom: var(--space-sm);"><strong>PlayStation controllers</strong> - DualShock 4, DualSense</li>
      <li style="margin-bottom: var(--space-sm);"><strong>Generic USB gamepads</strong> - Any HID-compliant controller</li>
      <li style="margin-bottom: var(--space-sm);"><strong>Joysticks</strong> - Flight sticks, racing wheels, and specialized input devices</li>
    </ul>

    <div class="code-block" style="margin-top: var(--space-xl);">
      <div class="code-header">
        <span class="code-lang">Java</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">// LITIENGINE provides predefined mappings for common controllers
// Xbox controller buttons
Gamepad.Xbox.A           // A button
Gamepad.Xbox.B           // B button  
Gamepad.Xbox.X           // X button
Gamepad.Xbox.Y           // Y button
Gamepad.Xbox.LEFT_STICK_X   // Left stick X axis
Gamepad.Xbox.LEFT_STICK_Y   // Left stick Y axis
Gamepad.Xbox.RIGHT_STICK_X  // Right stick X axis
Gamepad.Xbox.RIGHT_STICK_Y  // Right stick Y axis
Gamepad.Xbox.LT           // Left trigger
Gamepad.Xbox.RT           // Right trigger

// DualShock4 controller buttons
Gamepad.DualShock4.CROSS
Gamepad.DualShock4.CIRCLE
Gamepad.DualShock4.TRIANGLE
Gamepad.DualShock4.SQUARE
Gamepad.DualShock4.OPTIONS
Gamepad.DualShock4.SHARE</code></pre>
    </div>
  </div>
</div>

<div class="section section-alt">
  <div class="container">
    <h2 class="section-title">Summary</h2>
    
    <p>
      The migration from JInput to Input4j represents a significant improvement for LITIENGINE and its users:
    </p>

    <div class="cards" style="margin-top: var(--space-lg);">
      <div class="card">
        <h3 class="card-title">For Game Developers</h3>
        <p class="card-text">Simpler deployment with no native DLLs to manage. Just add LITIENGINE as a dependency and gamepad support works out of the box.</p>
      </div>
      
      <div class="card">
        <h3 class="card-title">For End Users</h3>
        <p class="card-text">Better cross-platform compatibility. Plug in any gamepad and it just works on Windows, Linux, or macOS.</p>
      </div>
      
      <div class="card">
        <h3 class="card-title">For the LITIENGINE Team</h3>
        <p class="card-text">Easier build and release process with fewer dependencies to manage. Direct control over input handling implementation.</p>
      </div>
    </div>

    <p style="margin-top: var(--space-xl); text-align: center;">
      <strong>Input4j powers the gamepad experience in LITIENGINE.</strong><br>
      <a href="{{ '/docs/java-game-integration' | relative_url }}" class="btn btn-primary" style="margin-top: var(--space-md);">Learn More About Input4j</a>
    </p>
  </div>
</div>
