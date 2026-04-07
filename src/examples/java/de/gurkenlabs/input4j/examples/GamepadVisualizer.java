package de.gurkenlabs.input4j.examples;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GamepadVisualizer extends JPanel {
  private static final Color BODY_STROKE = new Color(40, 40, 40);
  private static final Color STICK_WELL = new Color(210, 210, 210);
  private static final Color BUTTON_OFF = new Color(60, 60, 60);
  private static final Color BUTTON_ON = new Color(78, 201, 176);
  private static final Color TEXT_COLOR = new Color(220, 220, 220);
  private static final Color GLOW_COLOR = new Color(78, 201, 176, 80);

  private static final int W = 400;
  private static final int H = 300;
  private static final int CENTER_X = W / 2;

  private boolean a, b, x, y, lb, rb, back, start, lstick, rstick;
  private boolean dpadUp, dpadDown, dpadLeft, dpadRight;
  private float lx, ly, rx, ry, lTrigger, rTrigger;

  public GamepadVisualizer() {
    setPreferredSize(new Dimension(W, H));
    setBackground(new Color(240, 240, 240));
  }

  public void updateFromDevice(InputDevice device) {
    if (device == null) { reset(); return; }
    a = getBool(device, "A");
    b = getBool(device, "B");
    x = getBool(device, "X");
    y = getBool(device, "Y");
    lb = getBool(device, "LEFT_SHOULDER");
    rb = getBool(device, "RIGHT_SHOULDER");
    back = getBool(device, "BACK");
    start = getBool(device, "START");
    lstick = getBool(device, "LEFT_THUMB");
    rstick = getBool(device, "RIGHT_THUMB");
    dpadUp = getBool(device, "DPAD_UP");
    dpadDown = getBool(device, "DPAD_DOWN");
    dpadLeft = getBool(device, "DPAD_LEFT");
    dpadRight = getBool(device, "DPAD_RIGHT");
    lx = getAxis(device, "LEFT_THUMB_X");
    ly = -getAxis(device, "LEFT_THUMB_Y");
    rx = getAxis(device, "RIGHT_THUMB_X");
    ry = -getAxis(device, "RIGHT_THUMB_Y");
    lTrigger = getAxis(device, "LEFT_TRIGGER");
    rTrigger = getAxis(device, "RIGHT_TRIGGER");
    repaint();
  }

  private boolean getBool(InputDevice device, String name) {
    return device.getComponent(name).map(c -> c.getData() > 0).orElse(false);
  }

  private float getAxis(InputDevice device, String name) {
    return device.getComponent(name).map(InputComponent::getData).orElse(0f);
  }

  private void reset() {
    a = b = x = y = lb = rb = back = start = lstick = rstick = false;
    dpadUp = dpadDown = dpadLeft = dpadRight = false;
    lx = ly = rx = ry = lTrigger = rTrigger = 0;
    repaint();
  }

  @Override
  protected void paintComponent(Graphics g0) {
    super.paintComponent(g0);
    Graphics2D g = (Graphics2D) g0;
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    float scale = Math.min((float) getWidth() / W, (float) getHeight() / H);
    g.translate((getWidth() - W * scale) / 2, (getHeight() - H * scale) / 2);
    g.scale(scale, scale);

    drawTriggers(g);
    drawShoulders(g);
    drawCenterButtons(g);

    // Left Group: Offset Stick and D-Pad
    drawStick(g, CENTER_X - 95, 120, lx, ly, lstick);
    drawDPad(g, CENTER_X - 50, 195);

    // Right Group: Face Buttons and Offset Stick
    drawFaceButtons(g, CENTER_X + 95, 120);
    drawStick(g, CENTER_X + 50, 195, rx, ry, rstick);
  }

  private void drawTriggers(Graphics2D g) {
    int w = 45, h = 12, y = 20, offset = 100;
    drawTrigger(g, CENTER_X - offset - (w / 2), y, w, h, lTrigger, true);
    drawTrigger(g, CENTER_X + offset - (w / 2), y, w, h, rTrigger, false);
  }

  private void drawTrigger(Graphics2D g, int x, int y, int w, int h, float val, boolean left) {
    g.setColor(BUTTON_OFF);
    g.fillRoundRect(x, y, w, h, 6, 6);
    g.setColor(BUTTON_ON);
    int fillW = (int) (w * val);
    int fillX = left ? x : x + w - fillW;
    g.fillRoundRect(fillX, y, fillW, h, 6, 6);
    g.setColor(BODY_STROKE);
    g.drawRoundRect(x, y, w, h, 6, 6);
  }

  private void drawShoulders(Graphics2D g) {
    int w = 70, h = 22, y = 45, offset = 90;
    drawShoulder(g, CENTER_X - offset, y, w, h, lb, "LB");
    drawShoulder(g, CENTER_X + offset, y, w, h, rb, "RB");
  }

  private void drawShoulder(Graphics2D g, int cx, int y, int w, int h, boolean active, String label) {
    int x = cx - (w / 2);
    if (active) drawGlow(g, cx, y + h / 2, w / 2);
    g.setColor(active ? BUTTON_ON : BUTTON_OFF);
    g.fillRoundRect(x, y, w, h, 8, 8);
    g.setColor(active ? Color.WHITE : TEXT_COLOR);
    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
    int tw = g.getFontMetrics().stringWidth(label);
    g.drawString(label, cx - (tw / 2), y + 15);
  }

  private void drawDPad(Graphics2D g, int cx, int cy) {
    int size = 16, arm = 24;
    // Central "well"
    g.setColor(BUTTON_OFF);
    g.fillRect(cx - size / 2, cy - size / 2, size, size);

    drawDPadPart(g, cx - size / 2, cy - size / 2 - arm, size, arm, dpadUp);
    drawDPadPart(g, cx - size / 2, cy + size / 2, size, arm, dpadDown);
    drawDPadPart(g, cx - size / 2 - arm, cy - size / 2, arm, size, dpadLeft);
    drawDPadPart(g, cx + size / 2, cy - size / 2, arm, size, dpadRight);
  }

  private void drawDPadPart(Graphics2D g, int x, int y, int w, int h, boolean active) {
    g.setColor(active ? BUTTON_ON : BUTTON_OFF);
    g.fillRoundRect(x, y, w, h, 4, 4);
    g.setColor(BODY_STROKE);
    g.drawRoundRect(x, y, w, h, 4, 4);
  }

  private void drawStick(Graphics2D g, int cx, int cy, float x, float y, boolean pressed) {
    // Stick Well
    g.setColor(STICK_WELL);
    g.fillOval(cx - 30, cy - 30, 60, 60);
    g.setColor(new Color(180, 180, 180));
    g.drawOval(cx - 30, cy - 30, 60, 60);

    // Stick Cap (Thumb surface)
    int sx = cx + (int) (x * 20);
    int sy = cy + (int) (y * 20);

    RadialGradientPaint rgp = new RadialGradientPaint(sx, sy - 5, 25,
      new float[]{0f, 1f}, new Color[]{new Color(90, 90, 90), new Color(40, 40, 40)});
    g.setPaint(rgp);
    g.fillOval(sx - 22, sy - 22, 44, 44);

    g.setColor(pressed ? BUTTON_ON : new Color(30, 30, 30));
    g.fillOval(sx - 8, sy - 8, 16, 16);
  }

  private void drawFaceButtons(Graphics2D g, int cx, int cy) {
    int d = 30;
    drawButton(g, cx, cy + d, a, "A");
    drawButton(g, cx + d, cy, b, "B");
    drawButton(g, cx, cy - d, y, "Y");
    drawButton(g, cx - d, cy, x, "X");
  }

  private void drawButton(Graphics2D g, int cx, int cy, boolean pressed, String label) {
    int r = 14;
    if (pressed) drawGlow(g, cx, cy, r + 10);
    g.setColor(pressed ? BUTTON_ON : BUTTON_OFF);
    g.fillOval(cx - r, cy - r, r * 2, r * 2);
    g.setColor(pressed ? Color.WHITE : TEXT_COLOR);
    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    int tw = g.getFontMetrics().stringWidth(label);
    g.drawString(label, cx - (tw / 2), cy + 5);
  }

  private void drawCenterButtons(Graphics2D g) {
    int y = 85;
    drawSmallCircle(g, CENTER_X - 35, y, 7, back);
    drawSmallCircle(g, CENTER_X + 35, y, 7, start);

    // Home Button
    g.setColor(BUTTON_OFF);
    g.fillOval(CENTER_X - 12, y - 5, 24, 24);
    g.setColor(BODY_STROKE);
    g.drawOval(CENTER_X - 12, y - 5, 24, 24);
  }

  private void drawSmallCircle(Graphics2D g, int cx, int cy, int r, boolean active) {
    if (active) drawGlow(g, cx, cy, r + 5);
    g.setColor(active ? BUTTON_ON : BUTTON_OFF);
    g.fillOval(cx - r, cy - r, r * 2, r * 2);
  }

  private void drawGlow(Graphics2D g, int cx, int cy, int radius) {
    RadialGradientPaint rgp = new RadialGradientPaint(cx, cy, radius,
      new float[]{0f, 1f}, new Color[]{GLOW_COLOR, new Color(78, 201, 176, 0)});
    g.setPaint(rgp);
    g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
  }
}
