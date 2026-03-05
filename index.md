---
layout: default
title: input4j
---

<div class="hero-section">
  <div class="hero-content">
    <h1 class="hero-title">🎮 input4j</h1>
    <p class="hero-subtitle">A lightweight, cross-platform Java library for unified input device handling</p>
    <div class="hero-badges">
      <span class="badge">Windows</span>
      <span class="badge">Linux</span>
      <span class="badge">macOS</span>
      <span class="badge">Java 22+</span>
    </div>
    <div class="hero-animations">
      <div class="device-animation">
        <div class="gamepad">
          <div class="dpad"></div>
          <div class="buttons">
            <div class="button a"></div>
            <div class="button b"></div>
            <div class="button x"></div>
            <div class="button y"></div>
          </div>
          <div class="joysticks">
            <div class="joystick left"></div>
            <div class="joystick right"></div>
          </div>
        </div>
      </div>
    </div>
    <div class="hero-actions">
      <a href="#getting-started" class="btn btn-primary">Get Started</a>
      <a href="https://github.com/gurkenlabs/input4j" class="btn btn-secondary">GitHub</a>
    </div>
  </div>
</div>

<div class="features-section" id="features">
  <div class="container">
    <h2 class="section-title">Why input4j?</h2>
    <div class="features-grid">
      <div class="feature-card">
        <div class="feature-icon">🎮</div>
        <h3>Cross-Platform Support</h3>
        <p>Works seamlessly on Windows, Linux, and macOS without native dependencies</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🔧</div>
        <h3>Unified API</h3>
        <p>Single API for gamepads, joysticks, and other input devices</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">⚡</div>
        <h3>High Performance</h3>
        <p>Uses Foreign Function & Memory API for faster performance than JNI</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🌐</div>
        <h3>Event-Based</h3>
        <p>Supports both event-based and polling input handling</p>
      </div>
    </div>
  </div>
</div>

<div class="quick-start-section" id="getting-started">
  <div class="container">
    <h2 class="section-title">Quick Start</h2>
    <div class="code-snippet">
      <div class="code-header">
        <span class="language">Java</span>
        <button class="copy-btn" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">try (var devices = InputDevices.init()) {
    for (var device : devices.getAll()) {
        System.out.println(device.getName());
    }
}</code></pre>
    </div>
    <div class="code-snippet">
      <div class="code-header">
        <span class="language">Java</span>
        <button class="copy-btn" onclick="copyCode(this)">Copy</button>
      </div>
      <pre><code class="language-java">device.onButtonPressed(XInput.X, () -> 
    System.out.println("X button pressed"));
</code></pre>
    </div>
  </div>
</div>

<div class="stats-section">
  <div class="container">
    <h2 class="section-title">Trusted by Developers</h2>
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-number">5K+</div>
        <div class="stat-label">Active Users</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">100+</div>
        <div class="stat-label">Projects</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">5⭐</div>
        <div class="stat-label">GitHub Stars</div>
      </div>
    </div>
  </div>
</div>

{% include footer.html %}