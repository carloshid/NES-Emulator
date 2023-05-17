package com.carlosh.nesemulator.ui;

import com.carlosh.nesemulator.KeyController;
import javafx.scene.input.KeyCode;

public class ConfigOptions {
  private static boolean configFileExists = false;
  private static KeyCode[] controller0 = new KeyCode[8];
  private static KeyCode[] controller1 = new KeyCode[8];

  public static KeyCode getKeyCode(int controllerID, int buttonID) {
    if (controllerID == 0) {
      return controller0[buttonID];
    } else {
      return controller1[buttonID];
    }
  }

  public static void setKeyCode(int controllerID, int buttonID, KeyCode keyCode) {
    if (controllerID == 0) {
      controller0[buttonID] = keyCode;
    } else {
      controller1[buttonID] = keyCode;
    }
  }

  public static void loadConfigOptions() {
    if (!configFileExists) {
        controller0[0] = KeyCode.Z;
        controller0[1] = KeyCode.X;
        controller0[2] = KeyCode.C;
        controller0[3] = KeyCode.V;
        controller0[4] = KeyCode.UP;
        controller0[5] = KeyCode.DOWN;
        controller0[6] = KeyCode.LEFT;
        controller0[7] = KeyCode.RIGHT;

        controller1[0] = KeyCode.Q;
        controller1[1] = KeyCode.W;
        controller1[2] = KeyCode.E;
        controller1[3] = KeyCode.R;
        controller1[4] = KeyCode.NUMPAD5;
        controller1[5] = KeyCode.NUMPAD2;
        controller1[6] = KeyCode.NUMPAD1;
        controller1[7] = KeyCode.NUMPAD3;

        KeyController.controller0.setKeyCodes(controller0);
        KeyController.controller1.setKeyCodes(controller1);

    } else {
        // TODO : Load config options from file.
    }
  }
}
