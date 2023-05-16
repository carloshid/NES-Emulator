package com.carlosh.nesemulator.ui;

import com.carlosh.nesemulator.KeyController;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class KeybindField extends TextField {

  private KeyCode keyCode;
  private int buttonID;
  private int controllerID;

  public KeybindField(KeyCode keyCode, int buttonID, int controllerID) {
    assert (buttonID >= 0 && buttonID <= 7);
    assert (controllerID >= 0 && controllerID <= 1);

    setText(keyCode.getName());
    this.keyCode = keyCode;
    this.buttonID = buttonID;
    this.controllerID = controllerID;

    this.setEditable(false);
    this.setOnKeyPressed(this::onKeyPressed);
  }

  public void updateKeybind(KeyCode code) {
    keyCode = code;
    if (controllerID == 0) {
      switch (buttonID) {
        case 0 -> KeyController.controller0.a = keyCode;
        case 1 -> KeyController.controller0.b = keyCode;
        case 2 -> KeyController.controller0.select = keyCode;
        case 3 -> KeyController.controller0.start = keyCode;
        case 4 -> KeyController.controller0.up = keyCode;
        case 5 -> KeyController.controller0.down = keyCode;
        case 6 -> KeyController.controller0.left = keyCode;
        case 7 -> KeyController.controller0.right = keyCode;
      }
    } else {
      switch (buttonID) {
        case 0 -> KeyController.controller1.a = keyCode;
        case 1 -> KeyController.controller1.b = keyCode;
        case 2 -> KeyController.controller1.select = keyCode;
        case 3 -> KeyController.controller1.start = keyCode;
        case 4 -> KeyController.controller1.up = keyCode;
        case 5 -> KeyController.controller1.down = keyCode;
        case 6 -> KeyController.controller1.left = keyCode;
        case 7 -> KeyController.controller1.right = keyCode;
      }
    }
  }

  private void onKeyPressed(KeyEvent event) {
    KeyCode code = event.getCode();
    setText(code.getName());
    updateKeybind(code);
  }

}
