package de.gurkenlabs.input4j.examples;

import de.gurkenlabs.input4j.InputComponent;
import de.gurkenlabs.input4j.InputDevice;

import javax.swing.*;
import java.awt.*;

public class GamepadVisualizer extends JPanel {
  private static final Color BODY_STROKE = new Color(28, 28, 28);
  private static final Color STICK_DOT = new Color(40, 40, 40);
  private static final Color BUTTON_OFF = new Color(55, 55, 55);
  private static final Color BUTTON_ON = new Color(78, 201, 176);
  private static final Color TEXT_COLOR = new Color(180, 180, 180);

  private static final int W = 400;
  private static final int H = 300;
  private static final int CENTER_X = W / 2;

  private boolean a, b, x, y, lb, rb, back, start, lstick, rstick;
  private boolean dpadUp, dpadDown, dpadLeft, dpadRight;
  private float lx, ly, rx, ry, lTrigger, rTrigger;

  public GamepadVisualizer() {
    setPreferredSize(new Dimension(W, H));
    setBackground(Color.WHITE);
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

    g.setPaint(new GradientPaint(0, 0, new Color(245, 245, 245), 0, getHeight(), new Color(220, 220, 220)));
    g.fillRect(0, 0, getWidth(), getHeight());

    float scale = Math.min((float) getWidth() / W, (float) getHeight() / H);
    g.translate((getWidth() - W * scale) / 2, (getHeight() - H * scale) / 2);
    g.scale(scale, scale);

    drawTriggers(g);
    drawShoulders(g);
    drawCenterButtons(g);

    // Left Group
    drawStick(g, CENTER_X - 90, 115, lx, ly, lstick);
    drawDPad(g, CENTER_X - 45, 185);

    // Right Group
    drawFaceButtons(g, CENTER_X + 90, 115);
    drawStick(g, CENTER_X + 45, 185, rx, ry, rstick);
  }

  private void drawTriggers(Graphics2D g) {
    int w = 40, h = 14, y = 25, offset = 95;
    int lx = CENTER_X - offset - (w / 2);
    int rx = CENTER_X + offset - (w / 2);

    g.setColor(BUTTON_OFF);
    g.fillRoundRect(lx, y, w, h, 8, 8);
    g.fillRoundRect(rx, y, w, h, 8, 8);

    g.setColor(BUTTON_ON);
    g.fillRoundRect(lx, y, (int) (w * lTrigger), h, 8, 8);
    g.fillRoundRect(rx + w - (int) (w * rTrigger), y, (int) (w * rTrigger), h, 8, 8);

    g.setColor(BODY_STROKE);
    g.drawRoundRect(lx, y, w, h, 8, 8);
    g.drawRoundRect(rx, y, w, h, 8, 8);
  }

  private void drawShoulders(Graphics2D g) {
    int w = 65, h = 20, y = 48, offset = 85;
    drawShoulder(g, CENTER_X - offset, y, w, h, lb, "LB");
    drawShoulder(g, CENTER_X + offset, y, w, h, rb, "RB");
  }

  private void drawShoulder(Graphics2D g, int cx, int y, int w, int h, boolean active, String label) {
    int x = cx - (w / 2);
    g.setColor(active ? BUTTON_ON : BUTTON_OFF);
    g.fillRoundRect(x, y, w, h, 6, 6);
    g.setColor(BODY_STROKE);
    g.drawRoundRect(x, y, w, h, 6, 6);
    g.setColor(TEXT_COLOR);
    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
    int tw = g.getFontMetrics().stringWidth(label);
    g.drawString(label, cx - (tw / 2), y + 14);
  }

  private void drawDPad(Graphics2D g, int cx, int cy) {
    int bw = 14, bh = 26, gap = 4;
    drawDPadPart(g, cx - (bw / 2), cy - bh - gap, bw, bh, dpadUp);
    drawDPadPart(g, cx - (bw / 2), cy + gap, bw, bh, dpadDown);
    drawDPadPart(g, cx - bh - gap, cy - (bw / 2), bh, bw, dpadLeft);
    drawDPadPart(g, cx + gap, cy - (bw / 2), bh, bw, dpadRight);
  }

  private void drawDPadPart(Graphics2D g, int x, int y, int w, int h, boolean active) {
    g.setColor(active ? BUTTON_ON : BUTTON_OFF);
    g.fillRoundRect(x, y, w, h, 4, 4);
    g.setColor(BODY_STROKE);
    g.drawRoundRect(x, y, w, h, 4, 4);
  }

  private void drawStick(Graphics2D g, int cx, int cy, float x, float y, boolean pressed) {
    g.setColor(STICK_DOT);
    g.drawOval(cx - 26, cy - 26, 52, 52);
    g.setColor(pressed ? BUTTON_ON : STICK_DOT);
    int dotX = cx + (int) (x * 16) - 9;
    int dotY = cy + (int) (y * 16) - 9;
    g.fillOval(dotX, dotY, 18, 18);
  }

  private void drawFaceButtons(Graphics2D g, int cx, int cy) {
    int dist = 28;
    drawButton(g, cx, cy + dist, a, "A");
    drawButton(g, cx + dist, cy, b, "B");
    drawButton(g, cx, cy - dist, y, "Y");
    drawButton(g, cx - dist, cy, x, "X");
  }

  private void drawButton(Graphics2D g, int cx, int cy, boolean pressed, String label) {
    int r = 13;
    g.setColor(pressed ? BUTTON_ON : BUTTON_OFF);
    g.fillOval(cx - r, cy - r, r * 2, r * 2);
    g.setColor(BODY_STROKE);
    g.drawOval(cx - r, cy - r, r * 2, r * 2);
    g.setColor(TEXT_COLOR);
    g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
    int tw = g.getFontMetrics().stringWidth(label);
    g.drawString(label, cx - (tw / 2), cy + 5);
  }

  private void drawCenterButtons(Graphics2D g) {
    int y = 85, r = 8;
    drawSmallCircle(g, CENTER_X - 35, y, r, back);
    drawSmallCircle(g, CENTER_X + 35, y, r, start);
  }

  private void drawSmallCircle(Graphics2D g, int cx, int cy, int r, boolean active) {
    g.setColor(active ? BUTTON_ON : BUTTON_OFF);
    g.fillOval(cx - r, cy - r, r * 2, r * 2);
    g.setColor(BODY_STROKE);
    g.drawOval(cx - r, cy - r, r * 2, r * 2);
  }
}
