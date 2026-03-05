---
layout: default
title: input4j
description: A lightweight, cross-platform Java library for unified input device handling. Supports Windows, Linux, and macOS with no native dependencies.
---

<div class="hero">
  <div class="container">
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
      <a href="#quickstart" class="btn btn-primary">Get Started</a>
      <a href="https://github.com/gurkenlabs/input4j" class="btn btn-secondary" target="_blank" rel="noopener">View on GitHub</a>
      <a href="/guides" class="btn btn-ghost">Read Guides</a>
    </div>
  </div>
</div>

<div class="section" id="features">
  <div class="container">
    <h2 class="section-title">Why Use input4j?</h2>
    <div class="cards">
      <div class="card">
        <div class="card-icon">🌍</div>
        <h3 class="card-title">Cross-Platform</h3>
        <p class="card-text">Works seamlessly on Windows, Linux, and macOS. Single API for all platforms with zero platform-specific code required.</p>
      </div>
      <div class="card">
        <div class="card-icon">⚡</div>
        <h3 class="card-title">Modern Java API</h3>
        <p class="card-text">Leverages the Foreign Function & Memory API (Java 21+) for high-performance native access without JNI complexity.</p>
      </div>
      <div class="card">
        <div class="card-icon">🎮</div>
        <h3 class="card-title">Unified Input API</h3>
        <p class="card-text">Single consistent API for gamepads, joysticks, and other input devices. Supports XInput, DirectInput, evdev, and IOKit/HID.</p>
      </div>
      <div class="card">
        <div class="card-icon">🔌</div>
        <h3 class="card-title">No Native Dependencies</h3>
        <p class="card-text">No .dll, .so, or .dylib files to manage. Simply add the JAR and start coding. Perfect for distribution.</p>
      </div>
      <div class="card">
        <div class="card-icon">📡</div>
        <h3 class="card-title">Event-Based & Polling</h3>
        <p class="card-text">Flexible input handling with both event-driven callbacks and polling modes. Choose what fits your architecture.</p>
      </div>
      <div class="card">
        <div class="card-icon">🛠️</div>
        <h3 class="card-title">Lightweight</h3>
        <p class="card-text">Minimal footprint with no external dependencies. Designed for games, simulations, and applications.</p>
      </div>
    </div>
  </div>
</div>

<div class="section section-alt" id="quickstart">
  <div class="container">
    <h2 class="section-title">Quick Start</h2>
    <p class="section-subtitle">Add input4j to your project and start handling gamepad input in minutes.</p>
    
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Gradle</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-gradle">dependencies {
    implementation 'de.gurkenlabs:input4j:1.0.0'
}</code></pre>
    </div>
    
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Maven</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-markup">&lt;dependency&gt;
    &lt;groupId&gt;de.gurkenlabs&lt;/groupId&gt;
    &lt;artifactId&gt;input4j&lt;/artifactId&gt;
    &lt;version&gt;1.0.0&lt;/version&gt;
&lt;/dependency&gt;</code></pre>
    </div>
    
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java - Initialize</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">try (var devices = InputDevices.init()) {
    for (var device : devices.getAll()) {
        System.out.println("Found: " + device.getName());
    }
}</code></pre>
    </div>
    
    <div class="code-block">
      <div class="code-header">
        <span class="code-lang">Java - Handle Events</span>
        <button class="code-copy" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">device.onButtonPressed(XInput.Button.A, () -> 
    System.out.println("A pressed!"));

device.onAxisChanged(XInput.Axis.LEFT_X, value -> 
    System.out.println("Left X: " + value));</code></pre>
    </div>
  </div>
</div>

<div class="section">
  <div class="container">
    <h2 class="section-title">Supported Platforms</h2>
    <div class="cards">
      <div class="card">
        <div class="card-icon">🪟</div>
        <h3 class="card-title">Windows</h3>
        <p class="card-text">Full support for XInput (Xbox controllers) and DirectInput (legacy gamepads).</p>
      </div>
      <div class="card">
        <div class="card-icon">🐧</div>
        <h3 class="card-title">Linux</h3>
        <p class="card-text">Native evdev integration via /dev/input interface for all HID-compliant gamepads.</p>
      </div>
      <div class="card">
        <div class="card-icon">🍎</div>
        <h3 class="card-title">macOS</h3>
        <p class="card-text">IOKit HID framework integration for reliable gamepad detection on Apple Silicon and Intel Macs.</p>
      </div>
    </div>
  </div>
</div>

<div class="section section-alt">
  <div class="container">
    <h2 class="section-title">Features</h2>
    <div class="stats">
      <div class="stat-card">
        <div class="stat-value">100%</div>
        <div class="stat-label">Java Native</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">0</div>
        <div class="stat-label">Native Dependencies</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">3</div>
        <div class="stat-label">Platforms</div>
      </div>
      <div class="stat-card">
        <div class="stat-value">MIT</div>
        <div class="stat-label">Open Source</div>
      </div>
    </div>
  </div>
</div>
