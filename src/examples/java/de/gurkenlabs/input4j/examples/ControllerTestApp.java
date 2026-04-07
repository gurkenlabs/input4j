package de.gurkenlabs.input4j.examples;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;
import de.gurkenlabs.input4j.InputDevicePlugin;
import de.gurkenlabs.input4j.InputDevices;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class ControllerTestApp extends JFrame {
  private static final Color BUTTON_PRESSED = new Color(76, 175, 80);
  private static final Color BUTTON_RELEASED = new Color(200, 200, 200);
  private static final Color AXIS_POSITIVE = new Color(66, 165, 245);
  private static final Color AXIS_NEGATIVE = new Color(239, 83, 80);

  private JComboBox<InputDevice> deviceSelector;
  private JLabel deviceInfoLabel;
  private JPanel buttonsPanel;
  private JPanel axesPanel;
  private JPanel dpadPanel;
  private JSlider rumbleSlider;
  private JLabel pollRateLabel;
  private JTextPane consolePane;
  private final java.util.List<String> eventLog = new java.util.ArrayList<>();
  private final java.util.Map<String, Float> lastAxisValues = new java.util.HashMap<>();
  private final java.util.Map<String, Float> lastButtonValues = new java.util.HashMap<>();

  private InputDevicePlugin plugin;
  private InputDevice currentDevice;
  private final java.util.Map<String, JLabel> buttonLabels = new java.util.HashMap<>();
  private final java.util.Map<String, JProgressBar> axisBars = new java.util.HashMap<>();
  private Timer pollTimer;
  private long lastPollTime;
  private int pollCount;

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new ControllerTestApp().setVisible(true));
  }

  public ControllerTestApp() {
    setTitle("Input4j Controller Test");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setMinimumSize(new Dimension(600, 500));

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

    var visualPanel = new JPanel(new GridLayout(2, 1, 10, 10));
    buttonsPanel = createButtonsPanel();
    axesPanel = createAxesPanel();
    visualPanel.add(buttonsPanel);
    visualPanel.add(axesPanel);

    var consolePanel = createConsolePanel();

    panel.add(visualPanel, BorderLayout.CENTER);
    panel.add(consolePanel, BorderLayout.EAST);
    return panel;
  }

  private JPanel createConsolePanel() {
    var panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Event Log"));
    panel.setPreferredSize(new Dimension(280, 0));

    consolePane = new JTextPane();
    consolePane.setEditable(false);
    consolePane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

    var scrollPane = new JScrollPane(consolePane);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

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

  private JPanel createButtonsPanel() {
    var panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Buttons"));

    var gridPanel = new JPanel(new GridLayout(4, 4, 5, 5));

    String[] buttonOrder = {"A", "B", "X", "Y", "LEFT_SHOULDER", "RIGHT_SHOULDER", "BACK", "START", "LEFT_THUMB", "RIGHT_THUMB", "DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT"};
    for (String name : buttonOrder) {
      var label = new JLabel(trimName(name), SwingConstants.CENTER);
      label.setOpaque(true);
      label.setBackground(BUTTON_RELEASED);
      label.setForeground(Color.BLACK);
      label.setFont(label.getFont().deriveFont(Font.BOLD, 14));
      label.setPreferredSize(new Dimension(60, 40));
      buttonLabels.put(name, label);
      gridPanel.add(label);
    }

    for (int i = 0; i < 8; i++) {
      gridPanel.add(Box.createGlue());
    }

    panel.add(gridPanel, BorderLayout.CENTER);
    return panel;
  }

  private String trimName(String name) {
    return name.replace("LEFT_", "L").replace("RIGHT_", "R").replace("THUMB", "").replace("SHOULDER", "B").replace("DPAD_", "");
  }

  private JPanel createAxesPanel() {
    var panel = new JPanel(new BorderLayout());
    panel.setBorder(BorderFactory.createTitledBorder("Axes"));

    dpadPanel = createDpadPanel();

    var axesGridPanel = new JPanel(new GridLayout(4, 1, 5, 5));

    String[] axisOrder = {"LEFT_THUMB_X", "LEFT_THUMB_Y", "RIGHT_THUMB_X", "RIGHT_THUMB_Y", "LEFT_TRIGGER", "RIGHT_TRIGGER"};
    for (String name : axisOrder) {
      var isTrigger = name.contains("TRIGGER");
      var bar = new JProgressBar(isTrigger ? 0 : -100, 100);
      bar.setValue(0);
      bar.setStringPainted(true);
      bar.setFont(bar.getFont().deriveFont(10f));
      axisBars.put(name, bar);

      var label = new JLabel(shortenAxisName(name) + ":", SwingConstants.LEFT);
      label.setPreferredSize(new Dimension(55, 20));
      label.setMinimumSize(new Dimension(55, 20));

      var rowPanel = new JPanel(new BorderLayout(2, 0));
      rowPanel.add(label, BorderLayout.WEST);
      rowPanel.add(bar, BorderLayout.CENTER);

      axesGridPanel.add(rowPanel);
    }

    var centerPanel = new JPanel(new BorderLayout(10, 0));
    centerPanel.add(dpadPanel, BorderLayout.WEST);
    centerPanel.add(axesGridPanel, BorderLayout.CENTER);

    panel.add(centerPanel, BorderLayout.CENTER);
    return panel;
  }

  private String shortenAxisName(String name) {
    return name.replace("LEFT_", "L").replace("RIGHT_", "R").replace("_THUMB", "").replace("_TRIGGER", "");
  }

  private JPanel createDpadPanel() {
    var panel = new JPanel(new GridLayout(3, 3, 2, 2));
    panel.setPreferredSize(new Dimension(100, 100));

    String[] layout = {"DPAD_LEFT", "DPAD_UP", "DPAD_RIGHT", "", "DPAD_DOWN", ""};
    for (int i = 0; i < 6; i++) {
      var btn = new JPanel();
      btn.setBackground(BUTTON_RELEASED);
      btn.setPreferredSize(new Dimension(30, 30));
      if (!layout[i].isEmpty()) {
        buttonLabels.put(layout[i], new JLabel(layout[i].replace("DPAD_", ""), SwingConstants.CENTER));
        buttonLabels.get(layout[i]).setOpaque(true);
        buttonLabels.get(layout[i]).setBackground(BUTTON_RELEASED);
        buttonLabels.get(layout[i]).setForeground(Color.BLACK);
        panel.add(buttonLabels.get(layout[i]));
      } else {
        panel.add(btn);
      }
    }

    var wrapper = new JPanel();
    wrapper.add(panel);
    return wrapper;
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
    for (var component : device.getComponents()) {
      String name = component.getId().name;
      float data = component.getData();

      if (component.isButton()) {
        var label = buttonLabels.get(name);
        if (label != null) {
          label.setBackground(data > 0 ? BUTTON_PRESSED : BUTTON_RELEASED);
        }

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
        var bar = axisBars.get(name);
        if (bar != null) {
          updateAxisBar(bar, data);
        }
        logAxisEvent(name, data);
      }
    }

    updateDPad(device);
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

  private void updateAxisBar(JProgressBar bar, float data) {
    int value = (int) (data * 100);
    bar.setValue(value);

    if (data > 0.1f) {
      bar.setForeground(AXIS_POSITIVE);
    } else if (data < -0.1f) {
      bar.setForeground(AXIS_NEGATIVE);
    } else {
      bar.setForeground(null);
    }
  }

  private void updateDPad(InputDevice device) {
    String[] dpadButtons = {"DPAD_UP", "DPAD_DOWN", "DPAD_LEFT", "DPAD_RIGHT"};
    for (String name : dpadButtons) {
      var data = getComponentValue(device, name);
      updateDPadButton(name, data > 0);
    }
  }

  private void updateDPadButton(String name, boolean pressed) {
    var label = buttonLabels.get(name);
    if (label != null) {
      label.setBackground(pressed ? BUTTON_PRESSED : BUTTON_RELEASED);
    }
  }

  private float getComponentValue(InputDevice device, String name) {
    return device.getComponent(name).map(InputComponent::getData).orElse(0f);
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