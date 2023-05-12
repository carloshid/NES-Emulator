package com.carlosh.nesemulator;

import static java.lang.Thread.sleep;

import com.carlosh.nesemulator.ui.ScreenNES;
import java.util.concurrent.Callable;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

  public static void main(String[] args) {

    //CPU cpu = CPU.instance;

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

    StackPane root = new StackPane(screen);
    //root.getChildren().add(screen);

    Scene scene = new Scene(root);
    stage.setTitle("NES Emulator");

    stage.setScene(scene);
    stage.show();

    ROM rom = new ROM("rom.nes"); // Change to the filename of the rom file
    Bus bus = new Bus();
    bus.addROM(rom);
    bus.reset();
    //PPU ppu = new
    // PPU();
    //ppu.addROM(rom);


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
}
