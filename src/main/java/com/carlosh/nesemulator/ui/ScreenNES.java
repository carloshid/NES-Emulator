package com.carlosh.nesemulator.ui;

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

  public ScreenNES() {
    super(NES_WIDTH, NES_HEIGHT);
    instance = this;
  }

  Color black = Color.rgb(0, 0, 0);
  Color white = Color.rgb((0xFFFFFF >> 16) & 0xff, (0xFFFFFF >> 8) & 0xff, 0xFFFFFF & 0xff);

  public void updateScreen(int[][] pixels) {

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

    gc.drawImage(fxImage, 0, 0);

  }
  public static ScreenNES getInstance() {
    return instance;
  }
}
