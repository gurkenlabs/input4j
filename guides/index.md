---
layout: default
title: Guides - input4j
description: Complete documentation for input4j - cross-platform Java gamepad and joystick input library. Learn how to use the Foreign Function & Memory API for native input handling.
keywords: input4j guide, Java gamepad tutorial, game controller Java, FFM API input, cross-platform Java input, XInput Java, DirectInput Java, evdev Java
---

<script type="application/ld+json">
{
  "@context": "https://schema.org",
  "@type": "FAQPage",
  "mainEntity": [
    {
      "@type": "Question",
      "name": "What Java version is required for input4j?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "input4j requires Java 22 or later because it uses the Foreign Function & Memory API (FFM API) for native system calls. The FFM API was standardized in Java 22. Recommended distributions include Oracle JDK 21+, OpenJDK 21+, Amazon Corretto 21+, and Azul Zulu JDK 21+."
      }
    },
    {
      "@type": "Question",
      "name": "Which game controllers are supported by input4j?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "input4j supports Xbox controllers (Xbox One, Series X/S via XInput on Windows), PlayStation controllers (DualShock 4, DualSense via HID), generic USB gamepads (any HID-compliant controller), and specialized input devices like flight sticks and racing wheels. On Linux, the evdev interface supports all HID-compliant devices."
      }
    },
    {
      "@type": "Question",
      "name": "Does input4j work with game engines like LibGDX or LITIENGINE?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "Yes! input4j is designed to work with any Java game engine or framework including LITIENGINE, LibGDX, JavaFX, Slick2D, and custom engines. The library is framework-agnostic and provides a clean API that integrates easily with any input handling pattern."
      }
    },
    {
      "@type": "Question",
      "name": "Why does input4j not require native DLL/SO files?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "input4j uses the Foreign Function & Memory API which allows direct calls to native functions without JNI or native libraries. Benefits include simpler deployment (just add the JAR to your classpath), no native library distribution headaches, better compatibility with Java's security model, and type-safe native function calls at compile time."
      }
    },
    {
      "@type": "Question",
      "name": "How do I handle controller disconnects in input4j?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "input4j provides device connection callbacks. Use InputDevices.onDeviceConnected(device -> {...}) to listen for new controllers and device.onDisconnected(() -> {...}) to handle disconnections. This enables hot-plugging support for gamepads."
      }
    },
    {
      "@type": "Question",
      "name": "How do I fix Linux permission denied errors for /dev/input/event*?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "On Linux, add your user to the input group with 'sudo usermod -a -G input $USER' and log out/in for changes to take effect. Alternatively, run your application with elevated privileges for testing, though this is not recommended for production."
      }
    },
    {
      "@type": "Question",
      "name": "Can I use input4j for keyboard and mouse input?",
      "acceptedAnswer": {
        "@type": "Answer",
        "text": "No, input4j is specifically designed for gamepad and joystick input. For keyboard and mouse handling, use the native APIs provided by your game engine or framework such as Java AWT/Swing KeyListener, JavaFX input events, LibGDX input API, or LITIENGINE input handling."
      }
    }
  ]
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
    },
    {
      "@type": "ListItem",
      "position": 2,
      "name": "Guides",
      "item": "https://gurkenlabs.github.io/input4j/guides/"
    }
  ]
}
</script>

<div class="hero">
  <div class="container">
    <h1 class="hero-title">Documentation</h1>
    <p class="hero-subtitle">Learn how to use input4j for cross-platform gamepad and joystick input handling in Java applications</p>
  </div>
</div>

<div class="section">
  <div class="container">
    <h2 class="section-title">Guides</h2>
    <div class="cards">
      <div class="card">
        <div class="card-icon">🌐</div>
        <h3 class="card-title">Cross-platform Java Input Without JNI</h3>
        <p class="card-text">Learn how input4j uses the Foreign Function & Memory API (FFM API) to provide cross-platform gamepad and joystick input handling without native dependencies or JNI complexity.</p>
        <a href="{{ '/guides/cross-platform-java-input' | relative_url }}" class="btn btn-primary" style="margin-top: var(--space-md);">Read Guide</a>
      </div>
      
      <div class="card">
        <div class="card-icon">🎮</div>
        <h3 class="card-title">How to use input4j in your Java Game</h3>
        <p class="card-text">A practical step-by-step guide to integrating gamepad controller support into your Java game. Covers event-based and polling input handling, controller mapping, and force feedback vibration.</p>
        <a href="{{ '/guides/java-game-integration' | relative_url }}" class="btn btn-primary" style="margin-top: var(--space-md);">Read Guide</a>
      </div>
      
      <div class="card">
        <div class="card-icon">🏗️</div>
        <h3 class="card-title">Internal Architecture & FFM API</h3>
        <p class="card-text">Deep dive into input4j's internal architecture and implementation. Learn how the Foreign Function & Memory API enables native system calls without JNI or native libraries.</p>
        <a href="{{ '/guides/architecture-ffm-api' | relative_url }}" class="btn btn-primary" style="margin-top: var(--space-md);">Read Guide</a>
      </div>
    </div>
  </div>
</div>

<div class="section section-alt">
  <div class="container">
    <h2 class="section-title">Frequently Asked Questions</h2>
    
    <div class="guide-toc">
      <h3>Common Issues</h3>
      
      <div class="faq-item">
        <h4>Linux Permission Denied - /dev/input/event*</h4>
        <p>On Linux, you may encounter "Permission denied" errors when trying to access gamepad devices. This is because the <code>/dev/input/event*</code> files require special permissions.</p>
        
        <p><strong>Solution:</strong> Add your user to the <code>input</code> group:</p>
        
        <div class="code-block">
          <div class="code-header">
            <span class="code-lang">Bash</span>
            <button class="code-copy" onclick="copyCode(this)">Copy</button>
          </div>
          <pre><code class="language-bash"># Add user to input group
sudo usermod -a -G input $USER

# Log out and log back in for changes to take effect
# Or temporarily run with sudo for testing</code></pre>
        </div>
        
        <p><strong>Alternative:</strong> Run your application with elevated privileges (not recommended for production):</p>
        
        <div class="code-block">
          <div class="code-header">
            <span class="code-lang">Bash</span>
            <button class="code-copy" onclick="copyCode(this)">Copy</button>
          </div>
          <pre><code class="language-bash">sudo java -jar your-game.jar</code></pre>
        </div>
        
        <p>For Wayland users on KDE Plasma or other modern desktop environments, you may also need to ensure gamepad permissions are correctly configured in the system settings.</p>
      </div>
      
      <div class="faq-item">
        <h4>What Java version is required?</h4>
        <p>input4j requires <strong>Java 22</strong> or later because it uses the Foreign Function & Memory API (FFM API) for native system calls. The FFM API was standardized in Java 22.</p>
        <p>Make sure you're using a JDK distribution that includes the FFM API, such as:</p>
        <ul>
          <li>Oracle JDK 21+</li>
          <li>OpenJDK 21+</li>
          <li>Amazon Corretto 21+</li>
          <li>Azul Zulu JDK 21+</li>
        </ul>
      </div>
      
      <div class="faq-item">
        <h4>Does input4j work with game engines?</h4>
        <p>Yes! input4j is designed to work with any Java game engine or framework, including:</p>
        <ul>
          <li><strong>LITIENGINE</strong> - The pure Java 2D game engine</li>
          <li><strong>LibGDX</strong> - Popular cross-platform game framework</li>
          <li><strong>JavaFX</strong> - Desktop application framework</li>
          <li><strong>Slick2D</strong> - 2D game library</li>
          <li><strong>Custom engines</strong> - Use input4j directly in your game loop</li>
        </ul>
        <p>The library is framework-agnostic and provides a clean API that integrates easily with any input handling pattern.</p>
      </div>
      
      <div class="faq-item">
        <h4>Which game controllers are supported?</h4>
        <p>input4j supports a wide variety of game controllers across all platforms:</p>
        <ul>
          <li><strong>Xbox controllers</strong> - Xbox One, Series X/S (via XInput on Windows)</li>
          <li><strong>PlayStation controllers</strong> - DualShock 4, DualSense (via HID)</li>
          <li><strong>Generic USB gamepads</strong> - Any HID-compliant controller</li>
          <li><strong>Joysticks</strong> - Flight sticks, racing wheels, and specialized input devices</li>
        </ul>
        <p>On Linux, the library uses the evdev interface which supports all HID-compliant devices. On macOS, IOKit provides broad controller compatibility.</p>
      </div>
      
      <div class="faq-item">
        <h4>Why no native DLL/SO files?</h4>
        <p>Unlike other Java input libraries that rely on JNI (Java Native Interface) and require native .dll, .so, or .dylib files, input4j uses the <strong>Foreign Function & Memory API</strong>. This allows direct calls to native functions without the overhead and complexity of JNI.</p>
        <p>Benefits include:</p>
        <ul>
          <li>Simpler deployment - just add the JAR to your classpath</li>
          <li>No native library distribution headaches</li>
          <li>Better compatibility with Java's security model</li>
          <li>Type-safe native function calls at compile time</li>
        </ul>
      </div>
      
      <div class="faq-item">
        <h4>Can I use input4j for keyboard/mouse input?</h4>
        <p>input4j is specifically designed for <strong>gamepad and joystick input</strong>. For keyboard and mouse handling, use the native APIs provided by your game engine or framework:</p>
        <ul>
          <li>Java AWT/Swing KeyListener and MouseListener</li>
          <li>JavaFX input events</li>
          <li>LibGDX input API</li>
          <li>LITIENGINE input handling</li>
        </ul>
      </div>
      
      <div class="faq-item">
        <h4>How do I handle controller disconnects?</h4>
        <p>input4j provides device connection callbacks. Here's how to handle controller hot-plugging:</p>
        
        <div class="code-block">
          <div class="code-header">
            <span class="code-lang">Java</span>
            <button class="code-copy" onclick="copyCode(this)">Copy</button>
          </div>
          <pre><code class="language-java">// Listen for device connections
InputDevices.onDeviceConnected(device -> {
    System.out.println("Controller connected: " + device.getName());
});

// Listen for disconnections  
device.onDisconnected(() -> {
    System.out.println("Controller disconnected!");
});</code></pre>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="section">
  <div class="container">
    <h2 class="section-title">API Reference</h2>
    <div class="cards">
      <div class="card">
        <div class="card-icon">📚</div>
        <h3 class="card-title">Javadoc</h3>
        <p class="card-text">Complete API documentation for InputDevices, InputDevice, InputComponent, XInput, and all platform-specific implementations.</p>
        <a href="https://javadoc.io/doc/de.gurkenlabs/input4j" class="btn btn-primary" style="margin-top: var(--space-md);" target="_blank">View Javadoc</a>
      </div>
      <div class="card">
        <div class="card-icon">📦</div>
        <h3 class="card-title">Maven Central</h3>
        <p class="card-text">Release artifacts, version history, and dependency information available on Maven Central.</p>
        <a href="https://central.sonatype.com/artifact/de.gurkenlabs/input4j" class="btn btn-primary" style="margin-top: var(--space-md);" target="_blank">View on Maven</a>
      </div>
    </div>
  </div>
</div>
