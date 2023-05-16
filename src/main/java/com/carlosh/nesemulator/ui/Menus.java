package com.carlosh.nesemulator.ui;

import com.carlosh.nesemulator.Main;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * This class contains methods to create the different menus for the application.
 */
public class Menus {
  private static Stage stage;

  /**
   * Creates a menu bar with a menu labeled "File" with the following options:
   * - Load ROM: loads a ROM file and starts the emulation
   * - Options: opens a menu which allows the user to change different options
   * - Exit: exits the application
   * - Hotkeys: opens a menu which allows the user to change the different hotkeys for each controller
   *
   * @param stage The stage to which the menu bar will be added
   * @return The created menu bar
   */
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
    MenuItem options = new MenuItem("Options");

    // Event handler to show options dialog
    options.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        // Open options menu

        // Create and display the options window here
        Stage optionsStage = new Stage();
        optionsStage.initModality(Modality.APPLICATION_MODAL);
        optionsStage.setTitle("Options");

        // Create the settings layout and set it to the scene
        VBox optionsMenu = new VBox(); // TODO : Change this from VBox to OptionsMenu
        Scene optionsScene = new Scene(optionsMenu, 270, 200);
        optionsStage.setScene(optionsScene);
        optionsStage.showAndWait();

      }
    });

    return options;
  }

  private static MenuItem exit_menuItem() {
    MenuItem exit = new MenuItem("Exit");

    // Event handler to close the application
    exit.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        System.exit(0);
      }
    });

    return exit;
  }

  private static MenuItem hotkeys_menuItem() {
    MenuItem hotkeys = new MenuItem("Hotkeys");

    // Event handler to show options dialog
    hotkeys.setOnAction(new EventHandler<ActionEvent>() {
      public void handle(ActionEvent event) {
        // Open hotkeys menu

        // Create and display the hotkeys window here
        Stage hotkeysStage = new Stage();
        hotkeysStage.initModality(Modality.APPLICATION_MODAL);
        hotkeysStage.setTitle("Hotkeys");

        // Create the hotkeys layout and set it to the scene
        HotkeysMenu hotkeysMenu = new HotkeysMenu();
        Scene hotkeysScene = new Scene(hotkeysMenu, 220, 300);
        hotkeysStage.setScene(hotkeysScene);
        hotkeysStage.showAndWait();

      }
    });

    return hotkeys;
  }

}
