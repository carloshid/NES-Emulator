package com.carlosh.nesemulator;

import static java.lang.Thread.sleep;

import com.carlosh.nesemulator.ui.Menus;
import com.carlosh.nesemulator.ui.ScreenNES;
import java.io.File;
import java.util.concurrent.Callable;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

  private static VBox root;

  public static void main(String[] args) {

    launch(args);
  }

  public static void startEmulation(Stage stage, String file) throws Exception {
    // Start the NES screen and add it to the application's screen
    ScreenNES screen = new ScreenNES();
    StackPane sp = new StackPane(screen);
    root.getChildren().add(sp);
    // Adjust the size of the stage to match the NES screen
    stage.setWidth(screen.getWidth());
    stage.setHeight(screen.getHeight() + 64);

    // Start the NES emulation
    ROM rom = new ROM(file);  // TODO: Add error handling
    Bus bus = new Bus();
    bus.addROM(rom);
    bus.reset();

    // Start the emulation loop in a separate thread
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        long res = 0;
        long elapsedTime, currentTime, previousElapsedTime;
        long previousTime = System.nanoTime();

        while (true) {

          currentTime = System.nanoTime();
          elapsedTime = currentTime - previousTime;
          previousTime = currentTime;

          previousElapsedTime = elapsedTime;

          Bus.bus.controller[0] = KeyController.controller0.state;
          Bus.bus.controller[1] = KeyController.controller1.state;

          if (res > 0) {
            res -= elapsedTime;
          } else {
            res += 1000000000 / 60 - elapsedTime;

            while (!PPU.instance.ready) {
              bus.clock();
            }
            PPU.instance.ready = false;
          }


          ScreenNES.getInstance().updateScreen(PPU.instance.pixels);

        }
      }
    });

    t.start();

  }

  public void start(Stage stage) {
    // Create the menu bar and start the application ui
    MenuBar menuBar = Menus.menuBar(stage);
    VBox vbox = new VBox(menuBar);
    root = vbox;

    Scene scene = new Scene(vbox, ScreenNES.NES_WIDTH * ScreenNES.SCALE, ScreenNES.NES_HEIGHT * ScreenNES.SCALE + 64);
    stage.setScene(scene);
    stage.setTitle("NES Emulator");
    stage.show();
  }
}
