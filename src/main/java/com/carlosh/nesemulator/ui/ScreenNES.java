package com.carlosh.nesemulator.ui;

import com.carlosh.nesemulator.KeyController;
import java.awt.image.BufferedImage;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class ScreenNES extends Canvas {

  private static ScreenNES instance;

  public static final int NES_WIDTH = 256;
  public static final int NES_HEIGHT = 240;

  public static final int SCALE = 4;

  public ScreenNES() {
    super(NES_WIDTH * SCALE, NES_HEIGHT * SCALE);
    instance = this;
    this.setFocusTraversable(true);
//    this.setOnKeyPressed(e -> {
//      System.out.println("A");
//      KeyController.instance.keyPressed(e);
//    });
    this.setOnKeyPressed(KeyController.controller0::keyPressed);
    this.setOnKeyReleased(KeyController.controller0::keyReleased);
  }

  Color black = Color.rgb(0, 0, 0);
  Color white = Color.rgb((0xFFFFFF >> 16) & 0xff, (0xFFFFFF >> 8) & 0xff, 0xFFFFFF & 0xff);

  public void updateScreen2(int[][] pixels) {

    //Platform.runLater(() -> {
    GraphicsContext gc = getGraphicsContext2D();

    BufferedImage image = new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < NES_WIDTH; x++) {
      for (int y = 0; y < NES_HEIGHT; y++) {
        int pixel = pixels[x][y];
        image.setRGB(x, y, pixel);
      }
    }

    Image fxImage = SwingFXUtils.toFXImage(image, null);
    gc.drawImage(fxImage, 0, 0, NES_WIDTH * SCALE, NES_HEIGHT * SCALE);

  }

  public void updateScreen(int[][] pixels) {
    //Platform.runLater(() -> {
      GraphicsContext gc = getGraphicsContext2D();

      BufferedImage image = new BufferedImage(NES_WIDTH * SCALE, NES_HEIGHT * SCALE, BufferedImage.TYPE_INT_RGB);
      for (int x = 0; x < NES_WIDTH; x++) {
        for (int y = 0; y < NES_HEIGHT; y++) {

          int startx = (x * SCALE);
          int starty = (y * SCALE);

          for (int i = startx; i < startx + SCALE; i++) {
            for (int j = starty; j < starty + SCALE; j++) {
              image.setRGB(i, j, pixels[x][y]);
            }
          }
        }
      }
      Image fxImage = SwingFXUtils.toFXImage(image, null);
      gc.drawImage(fxImage, 0, 0);
    //});



  }



  public static ScreenNES getInstance() {
    return instance;
  }
}
