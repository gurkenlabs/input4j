---
layout: default
title: input4j - Cross-Platform Java Gamepad & Joystick Library
description: A lightweight, cross-platform Java library for unified input device handling. Supports Windows, Linux, and macOS with no native dependencies.
---

<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "HowTo",
  "name": "Getting Started with input4j",
  "description": "Add input4j to your Java project and start handling gamepad input in minutes.",
  "step": [
    {
      "@type": "HowToStep",
      "name": "Add input4j dependency",
      "text": "Add input4j to your project using Gradle or Maven. For Gradle, add 'implementation de.gurkenlabs:input4j:1.1.1' to your dependencies. For Maven, add the dependency XML to your pom.xml.",
      "url": "https://gurkenlabs.github.io/input4j/#quickstart",
      "position": 1
    },
    {
      "@type": "HowToStep",
      "name": "Initialize input devices",
      "text": "Initialize the input system by creating an InputDevices instance. Use try-with-resources to ensure proper cleanup.",
      "codeSample": "try (var devices = InputDevices.init()) {\n    for (var device : devices.getAll()) {\n        System.out.println(\"Found: \" + device.getName());\n    }\n}",
      "position": 2
    },
    {
      "@type": "HowToStep",
      "name": "Handle button events",
      "text": "Register callbacks for button press events. Use the onButtonPressed method with the button enum and a lambda handler.",
      "codeSample": "device.onButtonPressed(XInput.Button.A, () -> \n    System.out.println(\"A pressed!\"));",
      "position": 3
    },
    {
      "@type": "HowToStep",
      "name": "Handle axis events",
      "text": "Monitor analog input changes using the onAxisChanged method for joystick and trigger inputs.",
      "codeSample": "device.onAxisChanged(XInput.Axis.LEFT_X, value -> \n    System.out.println(\"Left X: \" + value));",
      "position": 4
    }
  ],
  "totalTime": "PT5M",
  "supply": {
    "@type": "HowToSupply",
    "name": "Java 22 or later",
    "required": true
  }
}
</script>

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
    }
  ]
}
</script>

<div class="hero">
  <div class="container">
    <img src="{{ '/assets/input4j_logo_x256.png' | relative_url }}" alt="input4j logo" class="hero-logo">
    <h1 class="hero-title">input<span class="logo-accent">4j</span></h1>
    <p class="hero-subtitle">Lightweight, cross-platform Java library for unified gamepad and joystick input handling</p>
    <div class="hero-badges">
      <span class="badge" aria-label="Windows">🪟 Windows</span>
      <span class="badge" aria-label="Linux">🐧 Linux</span>
      <span class="badge" aria-label="macOS">🍎 macOS</span>
      <br>
      <span class="badge" aria-label="Java 22+">Java 22+</span>
      <span class="badge" aria-label="No Native Dependencies">No Native Dependencies</span>
      <span class="badge" aria-label="MIT License">MIT License</span>
    </div>
    <div class="hero-actions">
      <a href="#quickstart" class="btn btn-primary">Get Started</a>
      <a href="https://github.com/gurkenlabs/input4j" class="btn btn-secondary" target="_blank" rel="noopener">View on GitHub</a>
      <a href="{{ '/guides' | relative_url }}" class="btn btn-ghost">Read Guides</a>
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
    implementation 'de.gurkenlabs:input4j:1.1.1'
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
    &lt;version&gt;1.1.1&lt;/version&gt;
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

<div class="section section-alt" id="features">
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

<div class="section">
  <div class="container">
    <h2 class="section-title">Why Use input4j?</h2>
    <div class="cards">
      <div class="card">
        <div class="card-icon" aria-label="Cross-Platform">🌍</div>
        <h3 class="card-title">Cross-Platform</h3>
        <p class="card-text">Works seamlessly on Windows, Linux, and macOS. Single API for all platforms with zero platform-specific code required.</p>
      </div>
      <div class="card">
        <div class="card-icon" aria-label="Modern Java API">⚡</div>
        <h3 class="card-title">Modern Java API</h3>
        <p class="card-text">Leverages the Foreign Function & Memory API (Java 22+) for high-performance native access without JNI complexity.</p>
      </div>
      <div class="card">
        <div class="card-icon" aria-label="Unified Input API">🎮</div>
        <h3 class="card-title">Unified Input API</h3>
        <p class="card-text">Single consistent API for gamepads, joysticks, and other input devices. Supports XInput, DirectInput, evdev, and IOKit/HID.</p>
      </div>
      <div class="card">
        <div class="card-icon" aria-label="No Native Dependencies">🔌</div>
        <h3 class="card-title">No Native Dependencies</h3>
        <p class="card-text">No .dll, .so, or .dylib files to manage. Simply add the JAR and start coding. Perfect for distribution.</p>
      </div>
      <div class="card">
        <div class="card-icon" aria-label="Event-Based & Polling">📡</div>
        <h3 class="card-title">Event-Based &amp; Polling</h3>
        <p class="card-text">Flexible input handling with both event-driven callbacks and polling modes. Choose what fits your architecture.</p>
      </div>
      <div class="card">
        <div class="card-icon" aria-label="Lightweight">🛠️</div>
        <h3 class="card-title">Lightweight</h3>
        <p class="card-text">Minimal footprint with no external dependencies. Designed for games, simulations, and applications.</p>
      </div>
    </div>
  </div>
</div>

<div class="section">
  <div class="container">
    <h2 class="section-title">Supported Platforms</h2>
    <div class="cards">
      <div class="card">
        <div class="card-icon" aria-label="Windows">🪟</div>
        <h3 class="card-title">Windows</h3>
        <p class="card-text">Full support for XInput (Xbox controllers) and DirectInput (legacy gamepads).</p>
      </div>
      <div class="card">
        <div class="card-icon" aria-label="Linux">🐧</div>
        <h3 class="card-title">Linux</h3>
        <p class="card-text">Native evdev integration via /dev/input interface for all HID-compliant gamepads.</p>
      </div>
      <div class="card">
        <div class="card-icon" aria-label="macOS">🍎</div>
        <h3 class="card-title">macOS</h3>
        <p class="card-text">IOKit HID framework integration for reliable gamepad detection on Apple Silicon and Intel Macs.</p>
      </div>
    </div>
  </div>
</div>
