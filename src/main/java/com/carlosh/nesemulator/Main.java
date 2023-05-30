package com.carlosh.nesemulator;

import static java.lang.Thread.sleep;

import com.carlosh.nesemulator.ui.ConfigOptions;
import com.carlosh.nesemulator.ui.Menus;
import com.carlosh.nesemulator.ui.ScreenNES;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Main class of the application.
 */
public class Main extends Application {

  private static VBox root;
  private static boolean emulationRunning = false;
  private static Thread emulationThread;
  private static ROM rom;

  public static void main(String[] args) {
    ConfigOptions.loadConfigOptions();
    launch(args);
  }

  public static void startEmulation(Stage stage, String file) {
    // Try to load the ROM file
    try {
      rom = new ROM(file);
    } catch (Exception e) {
      System.out.println("File does not exist or is not readable");
      return;
    }

    // If there was already an emulation running, stop it before starting another one from the
    // loaded ROM.
    if (emulationRunning) {
      emulationThread.interrupt();

      while (emulationRunning) {
        try {
          sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

    emulationRunning = true;
    // Start the NES screen and add it to the application's screen
    ScreenNES screen = new ScreenNES();
    StackPane sp = new StackPane(screen);
    for (Node child : root.getChildren()) {
      if (child instanceof StackPane) {
        root.getChildren().remove(child);
        break;
      }
    }
    root.getChildren().add(sp);
    // Adjust the size of the stage to match the NES screen
    stage.setWidth(screen.getWidth());
    stage.setHeight(screen.getHeight() + 64);

    // Start the NES emulation
    Bus bus = new Bus();
    bus.addROM(rom);
    bus.reset();

    // Start the emulation loop in a separate thread
    emulationThread = new Thread(new Runnable() {
      @Override
      public void run() {
        long res = 0;
        long elapsedTime, currentTime;
        long previousTime = System.nanoTime();

        while (!Thread.currentThread().isInterrupted()) {

          // Update the times
          currentTime = System.nanoTime();
          elapsedTime = currentTime - previousTime;
          previousTime = currentTime;

          // Update the controllers
          Bus.bus.controller[0] = KeyController.controller0.state;
          Bus.bus.controller[1] = KeyController.controller1.state;

          // If it is time for the next frame, perform clock cycles until the next frame is ready
          // and then update the screen
          if (res > 0) {
            res -= elapsedTime;
          } else {
            res += 1000000000 / 60 - elapsedTime;

            while (!PPU.instance.ready) {
              bus.clock();
            }
            PPU.instance.ready = false;

            try {
              ScreenNES.getInstance().updateScreen(PPU.instance.pixels);
            } catch (Exception e) {
              System.out.println("Error updating screen, skipping 1 frame");
            }
          }

        }

        CPU.instance.reset();
        PPU.instance.reset();
        emulationRunning = false;
      }
    });

    emulationThread.start();

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
