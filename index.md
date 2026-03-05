---
layout: default
title: input4j
description: A lightweight, cross-platform Java library for unified input device handling. Supports Windows, Linux, and macOS with no native dependencies.
keywords: Java input library, gamepad API, joystick input, cross-platform Java, XInput, DirectInput, evdev, IOKit, HID, game controller, Java 21, Foreign Function API
---

<div class="hero-section">
  <div class="hero-content">
    <h1 class="hero-title">input4j</h1>
    <p class="hero-subtitle">Lightweight, cross-platform Java library for unified gamepad and joystick input handling</p>
    <div class="hero-badges">
      <span class="badge">Windows</span>
      <span class="badge">Linux</span>
      <span class="badge">macOS</span>
      <span class="badge">Java 21+</span>
      <span class="badge">No Native Dependencies</span>
      <span class="badge">MIT License</span>
    </div>
    <div class="hero-actions">
      <a href="#getting-started" class="btn btn-primary">Get Started</a>
      <a href="https://github.com/gurkenlabs/input4j" class="btn btn-secondary">View on GitHub</a>
      <a href="/guides" class="btn btn-outline">Read Guides</a>
    </div>
  </div>
</div>

<div class="features-section" id="features">
  <div class="container">
    <h2 class="section-title">Why Use input4j?</h2>
    <div class="features-grid">
      <div class="feature-card">
        <span class="feature-icon">🌍</span>
        <h3>Cross-Platform</h3>
        <p>Works seamlessly on Windows, Linux, and macOS. Single API for all platforms with zero platform-specific code required in your application.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">⚡</span>
        <h3>Modern Java API</h3>
        <p>Leverages the Foreign Function & Memory API (Java 21+) for high-performance native access without JNI complexity or native libraries.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🎮</span>
        <h3>Unified Input API</h3>
        <p>Single consistent API for gamepads, joysticks, and other input devices. Supports XInput, DirectInput, evdev, and IOKit/HID backends.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🔌</span>
        <h3>No Native Dependencies</h3>
        <p>No .dll, .so, or .dylib files to manage. Simply add the JAR and start coding. Perfect for distribution and deployment.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">📡</span>
        <h3>Event-Based & Polling</h3>
        <p>Flexible input handling with both event-driven callbacks and polling modes. Choose what fits your application architecture.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🛠️</span>
        <h3>Lightweight</h3>
        <p>Minimal footprint with no external dependencies. Designed for games, simulations, and applications that need reliable input handling.</p>
      </div>
    </div>
  </div>
</div>

<div class="quick-start-section" id="getting-started">
  <div class="container">
    <h2 class="section-title">Quick Start</h2>
    <p style="text-align: center; color: var(--color-text-secondary); margin-bottom: var(--spacing-xl); max-width: 600px; margin-left: auto; margin-right: auto;">
      Add input4j to your project and start handling gamepad input in minutes. No native setup required.
    </p>
    <div class="code-snippet">
      <div class="code-header">
        <span class="language">Gradle (Groovy)</span>
        <button class="copy-btn" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code>dependencies {
    implementation 'de.gurkenlabs:input4j:1.0.0'
}</code></pre>
    </div>
    <div class="code-snippet">
      <div class="code-header">
        <span class="language">Maven</span>
        <button class="copy-btn" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code>&lt;dependency&gt;
    &lt;groupId&gt;de.gurkenlabs&lt;/groupId&gt;
    &lt;artifactId&gt;input4j&lt;/artifactId&gt;
    &lt;version&gt;1.0.0&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
    </div>
    <div class="code-snippet">
      <div class="code-header">
        <span class="language">Java - Initialize and List Devices</span>
        <button class="copy-btn" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code>try (var devices = InputDevices.init()) {
    for (var device : devices.getAll()) {
        System.out.println("Found: " + device.getName());
    }
}</code></pre>
    </div>
    <div class="code-snippet">
      <div class="code-header">
        <span class="language">Java - Handle Button Events</span>
        <button class="copy-btn" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code>device.onButtonPressed(XInput.Button.A, () -> 
    System.out.println("A button pressed!"));

device.onAxisChanged(XInput.Axis.LEFT_X, value -> 
    System.out.println("Left stick X: " + value));</code></pre>
    </div>
  </div>
</div>

<div class="features-section">
  <div class="container">
    <h2 class="section-title">Supported Platforms</h2>
    <div class="features-grid">
      <div class="feature-card">
        <span class="feature-icon">🪟</span>
        <h3>Windows</h3>
        <p>Full support for XInput (Xbox controllers) and DirectInput (legacy gamepads). Automatic detection and handling of connected controllers.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🐧</span>
        <h3>Linux</h3>
        <p>Native evdev integration via /dev/input interface. Supports all HID-compliant gamepads and joysticks on Linux systems.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🍎</span>
        <h3>macOS</h3>
        <p>IOKit HID framework integration for reliable gamepad detection and input handling on Apple Silicon and Intel Macs.</p>
      </div>
    </div>
  </div>
</div>

<div class="stats-section">
  <div class="container">
    <h2 class="section-title">Trusted by Java Developers</h2>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-number">100%</div>
        <div class="stat-label">Java Native</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">0</div>
        <div class="stat-label">Native Dependencies</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">3</div>
        <div class="stat-label">Platforms Supported</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">MIT</div>
        <div class="stat-label">Open Source License</div>
      </div>
    </div>
  </div>
</div>

<div class="quick-start-section">
  <div class="container">
    <h2 class="section-title">Use Cases</h2>
    <div class="features-grid">
      <div class="feature-card">
        <span class="feature-icon">🎮</span>
        <h3>Game Development</h3>
        <p>Add gamepad support to your Java games without worrying about platform-specific implementation details.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🖥️</span>
        <h3>Desktop Applications</h3>
        <p>Enable controller navigation in media centers, emulators, or accessibility applications.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🤖</span>
        <h3>Robotics & Simulations</h3>
        <p>Use game controllers for robot teleoperation, drone control, or simulation input.</p>
      </div>
      <div class="feature-card">
        <span class="feature-icon">🎰</span>
        <h3>Arcade & Kiosks</h3>
        <p>Build arcade cabinets or interactive kiosks with reliable controller input handling.</p>
      </div>
    </div>
  </div>
</div>
