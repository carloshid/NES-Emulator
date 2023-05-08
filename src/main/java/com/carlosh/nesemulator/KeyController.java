package com.carlosh.nesemulator;

import javafx.scene.input.KeyEvent;

public class KeyController {

  public static final KeyController instance = new KeyController();
//  private boolean A_PRESSED = false;
//  private boolean B_PRESSED = false;
//  private boolean SELECT_PRESSED = false;
//  private boolean START_PRESSED = false;
//  private boolean UP_PRESSED = false;
//  private boolean DOWN_PRESSED = false;
//  private boolean LEFT_PRESSED = false;
//  private boolean RIGHT_PRESSED = false;
  public int state = 0;

  private KeyController() {
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
    //System.out.println("Key pressed: " + e.getCode());
//    switch (e.getCode()) {
//      case A -> A_PRESSED = true;
//      case B -> B_PRESSED = true;
//      case S -> SELECT_PRESSED = true;
//      case D -> START_PRESSED = true;
//      case UP -> UP_PRESSED = true;
//      case DOWN -> DOWN_PRESSED = true;
//      case LEFT -> LEFT_PRESSED = true;
//      case RIGHT -> RIGHT_PRESSED = true;
//    }
    switch (e.getCode()) {
      case A -> state |= 0x80;
      case B -> state |= 0x40;
      case S -> state |= 0x20;
      case D -> state |= 0x10;
      case UP -> state |= 0x08;
      case DOWN -> state |= 0x04;
      case LEFT -> state |= 0x02;
      case RIGHT -> state |= 0x01;
    }
    //Bus.bus.controller[0] = state;
  }

  public void keyReleased(KeyEvent e) {
//    System.out.println("Key released: " + e.getCode());
//    switch (e.getCode()) {
//      case A -> A_PRESSED = false;
//      case B -> B_PRESSED = false;
//      case S -> SELECT_PRESSED = false;
//      case D -> START_PRESSED = false;
//      case UP -> UP_PRESSED = false;
//      case DOWN -> DOWN_PRESSED = false;
//      case LEFT -> LEFT_PRESSED = false;
//      case RIGHT -> RIGHT_PRESSED = false;
//    }
//    updateState();

    switch (e.getCode()) {
      case A -> state &= ~0x80;
      case B -> state &= ~0x40;
      case S -> state &= ~0x20;
      case D -> state &= ~0x10;
      case UP -> state &= ~0x08;
      case DOWN -> state &= ~0x04;
      case LEFT -> state &= ~0x02;
      case RIGHT -> state &= ~0x01;
    }
    //Bus.bus.controller[0] = state;
  }

  public void updateState() {
//    int state = 0;
//    if (A_PRESSED) {
//      state |= 0x80;
//    }
//    if (B_PRESSED) {
//      state |= 0x40;
//    }
//    if (SELECT_PRESSED) {
//      state |= 0x20;
//    }
//    if (START_PRESSED) {
//      state |= 0x10;
//    }
//    if (UP_PRESSED) {
//      state |= 0x08;
//    }
//    if (DOWN_PRESSED) {
//      state |= 0x04;
//    }
//    if (LEFT_PRESSED) {
//      state |= 0x02;
//    }
//    if (RIGHT_PRESSED) {
//      state |= 0x01;
//    }
//    System.out.println("State: " + state);
    Bus.bus.controller[0] = state;
  }
}
