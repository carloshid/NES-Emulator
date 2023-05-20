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
   * Sets the bit corresponding to the pressed key to 1.
   *
   * @param e the key event
   */
  public static void keyPressedStatic(KeyEvent e) {
    KeyCode[] keyCodes0 = new KeyCode[] { controller0.a, controller0.b, controller0.select,
        controller0.start, controller0.up, controller0.down, controller0.left, controller0.right };
    KeyCode[] keyCodes1 = new KeyCode[] { controller1.a, controller1.b, controller1.select,
        controller1.start, controller1.up, controller1.down, controller1.left, controller1.right };

    for (KeyCode keyCode : keyCodes0) {
      if (e.getCode() == keyCode) {
        controller0.keyPressed(e);
      }
    }

    for (KeyCode code : keyCodes1) {
      if (e.getCode() == code) {
        controller1.keyPressed(e);
      }
    }
  }

  /**
   * Sets the bit corresponding to the released key to 0.
   *
   * @param e the key event
   */
  public static void keyReleasedStatic(KeyEvent e) {
    KeyCode[] keyCodes0 = new KeyCode[] { controller0.a, controller0.b, controller0.select,
        controller0.start, controller0.up, controller0.down, controller0.left, controller0.right };
    KeyCode[] keyCodes1 = new KeyCode[] { controller1.a, controller1.b, controller1.select,
        controller1.start, controller1.up, controller1.down, controller1.left, controller1.right };

    for (KeyCode keyCode : keyCodes0) {
      if (e.getCode() == keyCode) {
        controller0.keyReleased(e);
      }
    }

    for (KeyCode code : keyCodes1) {
      if (e.getCode() == code) {
        controller1.keyReleased(e);
      }
    }
  }

  /**
   * Sets the bit corresponding to the released key to 0.
   *
   * @param e the key event
   */
  public void keyReleased(KeyEvent e) {
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

  public void setKeyCodes(KeyCode[] keyCodes) {
    a = keyCodes[0];
    b = keyCodes[1];
    select = keyCodes[2];
    start = keyCodes[3];
    up = keyCodes[4];
    down = keyCodes[5];
    left = keyCodes[6];
    right = keyCodes[7];
  }
}
