package com.carlosh.nesemulator;

import static java.lang.Thread.sleep;

import com.carlosh.nesemulator.ui.ScreenNES;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

  public static void main(String[] args) {

    launch(args);

    /*
    ROM rom = new ROM("filename"); // Change to the filename of the rom file
    Bus bus = new Bus();
    bus.addROM(rom);
    bus.reset();

     */
  }

  public void start(Stage stage) throws Exception {
    ScreenNES screen = new ScreenNES();
    int[][] pixels = new int[ScreenNES.NES_WIDTH][ScreenNES.NES_HEIGHT];
    for (int x = 0; x < ScreenNES.NES_WIDTH; x++) {
      for (int y = 0; y < ScreenNES.NES_HEIGHT; y++) {
        pixels[x][y] = 0xFFFFFF & (x * y);
      }
    }
    screen.updateScreen(pixels);

    StackPane root = new StackPane();
    root.getChildren().add(screen);

    Scene scene = new Scene(root);
    stage.setTitle("NES Emulator");

    stage.setScene(scene);
    stage.show();

    PPU ppu = new PPU();
    Thread t = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          ppu.clock();
        }
      }
    });

    t.start();
  }
}
