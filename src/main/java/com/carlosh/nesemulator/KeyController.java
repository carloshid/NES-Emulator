package com.carlosh.nesemulator;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller class for the two controllers of the NES. The state of the controller represents
 * the state of the 8 buttons of an NES controller (A, B, SELECT, START, UP, DOWN, LEFT, RIGHT),
 * with 1 bit for each button. The bit is set to 1 if the button is pressed, and 0 otherwise.
 */
public class KeyController {

  // Controller 0, used most of the time
  public static final KeyController controller0 = new KeyController();
  // Controller 1, used only when using two controllers (e.g. if playing with two players)
  public static final KeyController controller1 = new KeyController();

  // The current state of the controller
  public int state = 0;

  private KeyController() {
  }

  public KeyCode a = KeyCode.A;
  public KeyCode b = KeyCode.B;
  public KeyCode select = KeyCode.S;
  public KeyCode start = KeyCode.D;
  public KeyCode up = KeyCode.UP;
  public KeyCode down = KeyCode.DOWN;
  public KeyCode left = KeyCode.LEFT;
  public KeyCode right = KeyCode.RIGHT;

  /**
   * Sets the bit corresponding to the pressed key to 1.
   *
   * @param e the key event
   */
  public void keyPressed(KeyEvent e) {
//    switch (e.getCode()) {
//      case A -> state |= 0x80;
//      case B -> state |= 0x40;
//      case S -> state |= 0x20;
//      case D -> state |= 0x10;
//      case UP -> state |= 0x08;
//      case DOWN -> state |= 0x04;
//      case LEFT -> state |= 0x02;
//      case RIGHT -> state |= 0x01;
//    }
    if (e.getCode() == a) {
      state |= 0x80;
    } else if (e.getCode() == b) {
      state |= 0x40;
    } else if (e.getCode() == select) {
      state |= 0x20;
    } else if (e.getCode() == start) {
      state |= 0x10;
    } else if (e.getCode() == up) {
      state |= 0x08;
    } else if (e.getCode() == down) {
      state |= 0x04;
    } else if (e.getCode() == left) {
      state |= 0x02;
    } else if (e.getCode() == right) {
      state |= 0x01;
    }
  }

  /**
   * Sets the bit corresponding to the released key to 0.
   *
   * @param e the key event
   */
  public void keyReleased(KeyEvent e) {
//    switch (e.getCode()) {
//      case A -> state &= ~0x80;
//      case B -> state &= ~0x40;
//      case S -> state &= ~0x20;
//      case D -> state &= ~0x10;
//      case UP -> state &= ~0x08;
//      case DOWN -> state &= ~0x04;
//      case LEFT -> state &= ~0x02;
//      case RIGHT -> state &= ~0x01;
//    }
    if (e.getCode() == a) {
        state &= ~0x80;
    } else if (e.getCode() == b) {
      state &= ~0x40;
    } else if (e.getCode() == select) {
      state &= ~0x20;
    } else if (e.getCode() == start) {
      state &= ~0x10;
    } else if (e.getCode() == up) {
      state &= ~0x08;
    } else if (e.getCode() == down) {
      state &= ~0x04;
    } else if (e.getCode() == left) {
      state &= ~0x02;
    } else if (e.getCode() == right) {
      state &= ~0x01;
    }
  }
}
