---
layout: default
title: Java Game Integration
description: Learn how to integrate input4j into your Java game for cross-platform gamepad controller support. Complete guide covering event-based input, polling, game loop integration, controller mapping, and force feedback vibration.
keywords: Java game gamepad, game controller Java, input handling game Java, Java game input, LibGDX gamepad, LITIENGINE input, gamepad API Java, controller mapping Java, force feedback Java, rumble vibration Java, game loop input
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
      "name": "How to use input4j in your Java Game"
    }
  ]
}
</script>

<div class="guide-hero">
  <div class="container">
    <div class="guide-header">
      <h1 class="guide-title">🎮 How to use input4j in your Java Game</h1>
      <p class="guide-subtitle">A practical guide to adding gamepad support to your Java game</p>
      <div class="guide-meta">
        <span class="meta-item">📅 Updated Mar 2026</span>
        <span class="meta-item">📄 10 min read</span>
        <span class="meta-item">⭐ Beginner</span>
      </div>
    </div>
  </div>
</div>

<div class="guide-content">
  <div class="container">
    <div class="guide-toc">
      <h3>Table of Contents</h3>
      <ul>
        <li><a href="#setup">Setting up input4j</a></li>
        <li><a href="#initialization">Device Initialization</a></li>
        <li><a href="#polling">Polling vs Events</a></li>
        <li><a href="#game-loop">Integration with Game Loop</a></li>
        <li><a href="#controller-mapping">Controller Mapping</a></li>
        <li><a href="#rumble">Force Feedback / Rumble</a></li>
      </ul>
    </div>

    <div class="guide-section" id="setup">
      <h2>Setting up input4j</h2>
      <p>First, add input a dependency to your project:</p>
<p>For Gradle projects:</p>      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Gradle</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-gradle">dependencies {
    implementation 'de.gurkenlabs:input4j:1.1.1'
}</code></pre>
      </div>
      
      <p>For Maven projects:</p>
      
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
    </div>

    <div class="guide-section" id="initialization">
      <h2>Device Initialization</h2>
      <p>Initialize the input system when your game starts:</p>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Java</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">import de.gurkenlabs.input4j.InputDevices;
import de.gurkenlabs.input4j.InputDevice;

public class Game {
    private List&lt;InputDevice&gt; controllers;
    
    public void init() {
        // Initialize the input system
        var deviceList = InputDevices.init();
        
        // Get all connected controllers
        controllers = deviceList.getAll();
        
        // Or get a specific controller by index
        // InputDevice controller = controllers.get(0);
        
        System.out.println("Found " + controllers.size() + " controllers");
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="polling">
      <h2>Polling vs Events</h2>
      <p>input4j supports two input handling modes:</p>
      
      <div class="feature-list">
        <div class="feature-item">
          <div class="feature-icon">📡</div>
          <div class="feature-content">
            <h3>Event-Based (Recommended)</h3>
            <p>Register callbacks for button presses and axis changes. Better for event-driven architectures.</p>
          </div>
        </div>
        <div class="feature-item">
          <div class="feature-icon">🔄</div>
          <div class="feature-content">
            <h3>Polling</h3>
            <p>Query current input state each frame. Better for game loops with fixed update cycles.</p>
          </div>
        </div>
      </div>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Event-Based Example</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">// Event-based: Register callbacks
device.onButtonPressed(XInput.Button.A, () -> {
    player.jump();
});

device.onButtonReleased(XInput.Button.B, () -> {
    player.attack();
});

device.onAxisChanged(XInput.Axis.LEFT_X, value -> {
    player.setHorizontalMovement(value);
});</code></pre>
      </div>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Polling Example</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">// Polling: Query in game loop
public void update(float deltaTime) {
    for (InputDevice device : controllers) {
        // Check button state
        if (device.isPressed(XInput.Button.A)) {
            player.jump();
        }
        
        // Get axis value (-1.0 to 1.0)
        float leftX = device.getAxisValue(XInput.Axis.LEFT_X);
        float leftY = device.getAxisValue(XInput.Axis.LEFT_Y);
        
        player.move(leftX, leftY);
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="game-loop">
      <h2>Integration with Game Loop</h2>
      <p>Here's a complete example of integrating input4j with a typical game loop:</p>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Java</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">public class Game {
    private InputDevice controller;
    
    public void init() {
        var devices = InputDevices.init();
        this.controller = devices.getAll().stream()
            .findFirst()
            .orElse(null);
            
        if (controller != null) {
            // Use event-based for menu navigation
            controller.onButtonPressed(XInput.Button.START, () -> {
                startGame();
            });
            
            controller.onButtonPressed(XInput.Button.BACK, () -> {
                openMenu();
            });
        }
    }
    
    public void update(float deltaTime) {
        if (controller == null) return;
        
        // Polling for movement during gameplay
        float moveX = controller.getAxisValue(XInput.Axis.LEFT_X);
        float moveY = controller.getAxisValue(XInput.Axis.LEFT_Y);
        
        // Apply deadzone
        if (Math.abs(moveX) < 0.1f) moveX = 0;
        if (Math.abs(moveY) < 0.1f) moveY = 0;
        
        player.setVelocity(moveX * SPEED, moveY * SPEED);
    }
}</code></pre>
      </div>
    </div>

    <div class="guide-section" id="controller-mapping">
      <h2>Controller Mapping</h2>
      <p>Different controllers map to different button definitions:</p>
      
      <div class="comparison-table">
        <table>
          <thead>
            <tr>
              <th>Controller</th>
              <th>Button Set</th>
              <th>Notes</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Xbox</td>
              <td>XInput.Button</td>
              <td>A, B, X, Y, LB, RB, START, BACK, etc.</td>
            </tr>
            <tr>
              <td>PlayStation</td>
              <td>DualShock4.Button</td>
              <td>CROSS, CIRCLE, SQUARE, TRIANGLE, etc.</td>
            </tr>
            <tr>
              <td>Generic</td>
              <td>Button (0-127)</td>
              <td>Raw button indices</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="guide-section" id="rumble">
      <h2>Force Feedback / Rumble</h2>
      <p>Add haptic feedback to enhance game feel:</p>
      
      <div class="code-block">
        <div class="code-header">
          <span class="code-lang">Java</span>
          <button class="code-copy" onclick="copyCode(this)">Copy</button>
        </div>
        <pre><code class="language-java">// Set vibration intensity (0.0 to 1.0)
device.setVibration(0.5f, 0.5f); // Left motor, Right motor

// Rumble on hit
device.setVibration(1.0f, 0.0f);
Thread.sleep(100);
device.setVibration(0.0f, 0.0f);

// Or use try-with-resources for auto-cleanup
try (var devices = InputDevices.init()) {
    for (var device : devices.getAll()) {
        device.setVibration(0.8f, 0.8f);
    }
}</code></pre>
      </div>
    </div>
  </div>
</div>
