package com.carlosh.nesemulator.ui;

import com.carlosh.nesemulator.Main;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class Menus {

  // Load ROM
  // Options / Settings
  // Exit / Quit
  // Hotkeys

  private static Stage stage;

  public static MenuBar menuBar(Stage stage) {
    // Create a MenuBar with a File menu
    MenuBar menuBar = new MenuBar();
    Menu fileMenu = new Menu("File");

    Menus.stage = stage;

    MenuItem loadROM = loadRom_menuItem();
    MenuItem options = options_menuItem();
    MenuItem exit = exit_menuItem();
    MenuItem hotkeys = hotkeys_menuItem();

    fileMenu.getItems().addAll(loadROM, options, exit, hotkeys);
    menuBar.getMenus().add(fileMenu);

    return menuBar;
  }

  private static MenuItem loadRom_menuItem() {
    MenuItem loadROM = new MenuItem("Load ROM");

    // Event handler to load rom file
    loadROM.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        // Show a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
          try {
            Main.startEmulation(stage, selectedFile.getAbsolutePath());
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
    });

    return loadROM;
  }

  private static MenuItem options_menuItem() {
    return new MenuItem("Options");
  }

  private static MenuItem exit_menuItem() {
    return new MenuItem("Exit");
  }

  private static MenuItem hotkeys_menuItem() {
    return new MenuItem("Hotkeys");
  }



}
