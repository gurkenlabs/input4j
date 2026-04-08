package de.gurkenlabs.input4j.examples;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.github.weisj.darklaf.theme.HighContrastDarkTheme;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;
import de.gurkenlabs.input4j.InputDevices;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ControllerTestApp extends JFrame {
  private static final Logger LOGGER = Logger.getLogger(ControllerTestApp.class.getName());
  private JComboBox<InputDevice> deviceSelector;
  private JLabel deviceInfoLabel;
  private GamepadVisualizer visualizer;
  private JSlider rumbleSlider;
  private JLabel pollRateLabel;
  private JTextPane consolePane;
  private final java.util.List<String> eventLog = new java.util.ArrayList<>();
  private final java.util.Map<String, Float> lastAxisValues = new java.util.HashMap<>();
  private final java.util.Map<String, Float> lastButtonValues = new java.util.HashMap<>();

  private InputDevicePlugin plugin;
  private InputDevice currentDevice;
  private final java.util.Map<String, JProgressBar> axisBars = new java.util.HashMap<>();
  private Timer pollTimer;
  private long lastPollTime;
  private int pollCount;

  public static void main(String[] args) {
    try {
      LafManager.installTheme(new HighContrastDarkTheme());
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Failed to install Darklaf theme, falling back to default", e);
    }

    SwingUtilities.invokeLater(() -> new ControllerTestApp().setVisible(true));
  }

  public ControllerTestApp() {
    setTitle("Input4j Controller Test");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(800, 450));

    plugin = InputDevices.init();
    if (plugin == null) {
      JOptionPane.showMessageDialog(this, "Failed to initialize input device plugin", "Error",
          JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }

    var content = (JComponent) getContentPane();
    content.setLayout(new BorderLayout(10, 10));
    content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    var topPanel = createTopPanel();
    content.add(topPanel, BorderLayout.NORTH);

    var mainPanel = createMainPanel();
    content.add(mainPanel, BorderLayout.CENTER);

    var bottomPanel = createBottomPanel();
    content.add(bottomPanel, BorderLayout.SOUTH);

    plugin.onDeviceConnected(device -> {
      SwingUtilities.invokeLater(() -> {
        refreshDeviceList();
        if (currentDevice == null) {
          selectDevice(device);
        }
      });
    });
    plugin.onDeviceDisconnected(device -> {
      SwingUtilities.invokeLater(() -> {
        if (currentDevice == device) {
          currentDevice = null;
          refreshDeviceList();
        } else {
          refreshDeviceList();
        }
      });
    });

    refreshDeviceList();
    startPolling();
    pack();
    setLocationRelativeTo(null);
  }

  private JPanel createTopPanel() {
    var panel = new JPanel(new BorderLayout(5, 5));

    deviceSelector = new JComboBox<>();
    deviceSelector.addActionListener(e -> selectDevice((InputDevice) deviceSelector.getSelectedItem()));

    var selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    selectorPanel.add(new JLabel("Device:"));
    selectorPanel.add(Box.createHorizontalStrut(5));
    selectorPanel.add(deviceSelector);

    deviceInfoLabel = new JLabel("No device selected");
    deviceInfoLabel.setFont(deviceInfoLabel.getFont().deriveFont(Font.ITALIC));

    var infoPanel = new JPanel(new BorderLayout());
    infoPanel.add(deviceSelector, BorderLayout.NORTH);
    infoPanel.add(deviceInfoLabel, BorderLayout.SOUTH);

    panel.add(infoPanel, BorderLayout.NORTH);
    return panel;
  }

  private JPanel createMainPanel() {
    var panel = new JPanel(new BorderLayout(10, 10));

    visualizer = new GamepadVisualizer();
    var visualizerWrapper = new JPanel(new GridLayout());
    visualizerWrapper.add(visualizer);

    var consolePanel = createConsolePanel();

    panel.add(visualizerWrapper, BorderLayout.CENTER);
    panel.add(consolePanel, BorderLayout.EAST);
    return panel;
  }

  private JPanel createConsolePanel() {
    var panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Event Log"));
    panel.setPreferredSize(new Dimension(280, 0));

    consolePane = new JTextPane();
    consolePane.setEditable(false);

    // Dark theme compatible console styling
    consolePane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    consolePane.setBackground(new Color(22, 25, 27));
    consolePane.setForeground(new Color(210, 215, 220));
    consolePane.setCaretColor(new Color(180, 180, 180));
    consolePane.setSelectedTextColor(new Color(255, 255, 255));
    consolePane.setSelectionColor(new Color(50, 70, 90));
    consolePane.setOpaque(true);
    consolePane.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

    var scrollPane = new JScrollPane(consolePane);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.getViewport().setBackground(new Color(22, 25, 27));
    scrollPane.setBorder(BorderFactory.createEmptyBorder());

    var btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
    var clearBtn = new JButton("Clear");
    clearBtn.addActionListener(e -> {
      eventLog.clear();
      updateConsole();
    });
    btnPanel.add(clearBtn);

    panel.add(btnPanel, BorderLayout.NORTH);
    panel.add(scrollPane, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createBottomPanel() {
    var panel = new JPanel(new BorderLayout(10, 5));

    var rumblePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    rumblePanel.add(new JLabel("Rumble:"));
    rumbleSlider = new JSlider(0, 100, 0);
    rumbleSlider.setPreferredSize(new Dimension(150, 25));
    rumbleSlider.addChangeListener(e -> {
      if (currentDevice != null) {
        float intensity = rumbleSlider.getValue() / 100f;
        currentDevice.rumble(intensity);
      }
    });
    rumblePanel.add(rumbleSlider);

    var refreshBtn = new JButton("Refresh Devices");
    refreshBtn.addActionListener(e -> refreshDeviceList());
    rumblePanel.add(Box.createHorizontalStrut(20));
    rumblePanel.add(refreshBtn);

    pollRateLabel = new JLabel("Poll rate: -- fps");
    pollRateLabel.setFont(pollRateLabel.getFont().deriveFont(Font.ITALIC));

    panel.add(rumblePanel, BorderLayout.WEST);
    panel.add(pollRateLabel, BorderLayout.EAST);
    return panel;
  }

  private void refreshDeviceList() {
    var selectedDevice = currentDevice;
    deviceSelector.removeAllItems();
    var devices = plugin.getAll();
    for (var device : devices) {
      deviceSelector.addItem(device);
    }

    if (devices.isEmpty()) {
      deviceInfoLabel.setText("No devices connected");
      currentDevice = null;
    } else if (currentDevice != null && devices.contains(currentDevice)) {
    } else if (selectedDevice != null && devices.contains(selectedDevice)) {
      deviceSelector.setSelectedItem(selectedDevice);
    } else if (currentDevice == null) {
      selectDevice(devices.iterator().next());
    }
  }

  private void selectDevice(InputDevice device) {
    if (device == currentDevice) {
      return;
    }

    if (currentDevice != null) {
      currentDevice.rumble(0);
    }

    currentDevice = device;
    rumbleSlider.setValue(0);

    lastAxisValues.clear();
    lastButtonValues.clear();
    eventLog.clear();
    updateConsole();

    if (device != null) {
      deviceInfoLabel.setText(String.format("%s (VID: %04X, PID: %04X)", device.getDisplayName(),
          device.getVendorId(), device.getProductId()));
    } else {
      deviceInfoLabel.setText("No device selected");
    }
  }

  private void startPolling() {
    lastPollTime = System.nanoTime();
    pollTimer = new Timer(16, e -> pollDevices());
    pollTimer.start();
  }

  private void pollDevices() {
    for (var device : plugin.getAll()) {
      device.poll();
      if (device == currentDevice) {
        updateUI(device);
      }
    }

    pollCount++;
    if (pollCount % 30 == 0) {
      long now = System.nanoTime();
      double fps = 30_000_000_000.0 / (now - lastPollTime);
      lastPollTime = now;
      pollRateLabel.setText(String.format("Poll rate: %.1f fps", fps));
    }
  }

  private void updateUI(InputDevice device) {
    visualizer.updateFromDevice(device);

    for (var component : device.getComponents()) {
      String name = component.getId().name;
      float data = component.getData();

      if (component.isButton()) {
        Float lastButton = lastButtonValues.get(name);
        if (lastButton == null) {
          lastButton = 0f;
        }
        if (data != lastButton) {
          lastButtonValues.put(name, data);
          var timestamp = String.format("[%04d]", pollCount);
          eventLog.add(timestamp + " " + name + (data > 0 ? " pressed" : " released"));
          if (eventLog.size() > 200) {
            eventLog.remove(0);
          }
          updateConsole();
        }
      } else if (component.isAxis()) {
        logAxisEvent(name, data);
      }
    }
  }

  private void logAxisEvent(String name, float data) {
    Float lastValue = lastAxisValues.get(name);
    if (lastValue == null) {
      lastValue = 0f;
    }

    if (Math.abs(data - lastValue) > 0.05f) {
      lastAxisValues.put(name, data);
      var timestamp = String.format("[%04d]", pollCount);
      eventLog.add(timestamp + " " + name + ": " + String.format("%.2f", data));
      if (eventLog.size() > 200) {
        eventLog.remove(0);
      }
      updateConsole();
    }
  }

  private void updateConsole() {
    var sb = new StringBuilder();
    for (var line : eventLog) {
      sb.append(line).append("\n");
    }
    consolePane.setText(sb.toString());
    consolePane.setCaretPosition(consolePane.getDocument().getLength());
  }

  @Override
  public void dispose() {
    if (pollTimer != null) {
      pollTimer.stop();
    }
    if (plugin != null) {
      try {
        plugin.close();
      } catch (IOException e) {
      }
    }
    super.dispose();
  }
}
