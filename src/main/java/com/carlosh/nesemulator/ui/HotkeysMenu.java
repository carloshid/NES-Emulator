package com.carlosh.nesemulator.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class HotkeysMenu extends Pane {

  private Label aLabel = new Label("A");
  private Label bLabel = new Label("B");
  private Label upLabel = new Label("Up");
  private Label downLabel = new Label("Down");
  private Label leftLabel = new Label("Left");
  private Label rightLabel = new Label("Right");
  private Label selectLabel = new Label("Select");
  private Label startLabel = new Label("Start");

  private class ControllerBox extends VBox {
    private Label controllerLabel;
    private KeybindField aKeybindField;
    private KeybindField bKeybindField;
    private KeybindField upKeybindField;
    private KeybindField downKeybindField;
    private KeybindField leftKeybindField;
    private KeybindField rightKeybindField;
    private KeybindField selectKeybindField;
    private KeybindField startKeybindField;

    private KeyCode[] controller0Keybinds = new KeyCode[8];
    private KeyCode[] controller1Keybinds = new KeyCode[8];

    public ControllerBox(String controllerStr, int controllerID) {
      this.setHeight(200);
      this.setWidth(80);

      controllerLabel = new Label(controllerStr);
      aKeybindField = new KeybindField(KeyCode.A, 0, controllerID);
      bKeybindField = new KeybindField(KeyCode.B, 1, controllerID);
      upKeybindField = new KeybindField(KeyCode.UP, 4, controllerID);
      downKeybindField = new KeybindField(KeyCode.DOWN, 5, controllerID);
      leftKeybindField = new KeybindField(KeyCode.LEFT, 6, controllerID);
      rightKeybindField = new KeybindField(KeyCode.RIGHT, 7, controllerID);
      selectKeybindField = new KeybindField(KeyCode.S, 2, controllerID);
      startKeybindField = new KeybindField(KeyCode.D, 3, controllerID);

      this.getChildren().addAll(controllerLabel, aKeybindField, bKeybindField, upKeybindField,
          downKeybindField, leftKeybindField, rightKeybindField, selectKeybindField, startKeybindField);
      this.setSpacing(5);

      for (Node node : this.getChildren()) {
        if (node instanceof KeybindField) {
          ((KeybindField) node).setMaxWidth(70);
        }
      }
    }
  }

  public HotkeysMenu() {
    ControllerBox controller0Box = new ControllerBox("Controller 0", 0);
    ControllerBox controller1Box = new ControllerBox("Controller 1", 1);
    Label topLabel = new Label("Hotkeys");
    topLabel.setFont(new Font(20));

    topLabel.relocate(10, 0);
    controller0Box.relocate(60, 30);
    controller1Box.relocate(140, 30);

    VBox labels = new VBox();
    labels.getChildren().addAll(aLabel, bLabel, upLabel, downLabel, leftLabel, rightLabel, selectLabel, startLabel);
    labels.setSpacing(11);
    labels.relocate(10, 50);
    for (Node label : labels.getChildren()) {
      ((Label) label).setFont(new Font(14));
    }

    this.getChildren().addAll(topLabel, labels, controller0Box, controller1Box);
  }

}
