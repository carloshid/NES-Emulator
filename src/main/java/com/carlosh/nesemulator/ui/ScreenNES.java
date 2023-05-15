package com.carlosh.nesemulator.ui;

import com.carlosh.nesemulator.KeyController;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class ScreenNES extends Canvas {

  private static ScreenNES instance;

  public static final int NES_WIDTH = 256;
  public static final int NES_HEIGHT = 240;

  public static final double SCALE = 4;

  private static int[][] previousPixels = new int[NES_WIDTH][NES_HEIGHT];
  private boolean firstFrame = true;

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

  public void updateScreen(int[][] pixels) {
    boolean equal = true;
    for (int x = 0; x < NES_WIDTH; x++) {
      for (int y = 0; y < NES_HEIGHT; y++) {
        if (pixels[x][y] != previousPixels[x][y]) {
          equal = false;
          previousPixels[x][y] = pixels[x][y];
        }
      }
    }
    if (equal) return;

    GraphicsContext gc = getGraphicsContext2D();
    gc.setImageSmoothing(false);

    BufferedImage image = new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
    for (int x = 0; x < NES_WIDTH; x++) {
      for (int y = 0; y < NES_HEIGHT; y++) {
        int pixel = pixels[x][y];
        image.setRGB(x, y, pixel);
      }
    }

    Image fxImage = SwingFXUtils.toFXImage(image, null);
    gc.drawImage(fxImage, 0, 0, NES_WIDTH * SCALE, NES_HEIGHT * SCALE);

    image.flush();

  }

//  public void updateScreen6(int[][] pixels) {
//    GraphicsContext gc = getGraphicsContext2D();
//    gc.setImageSmoothing(false);
//
//    if (firstFrame) {
//      BufferedImage image = new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
//      for (int x = 0; x < NES_WIDTH; x++) {
//        for (int y = 0; y < NES_HEIGHT; y++) {
//          int pixel = pixels[x][y];
//          previousPixels[x][y] = pixel;
//          image.setRGB(x, y, pixel);
//        }
//      }
//
//      Image fxImage = SwingFXUtils.toFXImage(image, null);
//      gc.drawImage(fxImage, 0, 0, NES_WIDTH * SCALE, NES_HEIGHT * SCALE);
//
//      image.flush();
//
//      firstFrame = false;
//      return;
//    }
//
////    boolean equal = true;
////    for (int x = 0; x < NES_WIDTH; x++) {
////      for (int y = 0; y < NES_HEIGHT; y++) {
////        if (pixels[x][y] != previousPixels[x][y]) {
////          equal = false;
////        }
////      }
////    }
////    if (equal) return;
//
//    for (int x = 0; x < NES_WIDTH; x++) {
//      for (int y = 0; y < NES_HEIGHT; y++) {
//        int pixelNew = pixels[x][y];
//        int pixelOld = previousPixels[x][y];
//
//        if (pixelNew != pixelOld) {
//          previousPixels[x][y] = pixelNew;
////            for (int i = x; i < x + SCALE; i++) {
////              for (int j = y; j < y + SCALE; j++) {
////                gc.getPixelWriter().setColor(x, y, Color.rgb((pixelNew >> 16) & 0xff, (pixelNew >> 8) & 0xff, pixelNew & 0xff));
////              }
////            }
//          gc.setFill(Color.rgb((pixelNew >> 16) & 0xff, (pixelNew >> 8) & 0xff, pixelNew & 0xff));
//          gc.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
//        }
//      }
//    }
//
//  }

//  public void updateScreen(int[][] pixels) {
//    if (firstFrame) {
//      firstFrame = false;
//      updateScreen3(pixels);
//      return;
//    }
//
//    GraphicsContext gc = getGraphicsContext2D();
//    gc.setImageSmoothing(false);
//
//    //BufferedImage image = new BufferedImage(NES_WIDTH, NES_HEIGHT, BufferedImage.TYPE_INT_RGB);
//    for (int x = 0; x < NES_WIDTH; x++) {
//      for (int y = 0; y < NES_HEIGHT; y++) {
//        int pixelNew = pixels[x][y];
//        int pixelOld = previousPixels[x][y];
//
//        if (pixelNew != pixelOld) {
//          previousPixels[x][y] = pixelNew;
//          gc.getPixelWriter().setColor(x, y, Color.rgb((pixelNew >> 16) & 0xff, (pixelNew >> 8) & 0xff, pixelNew & 0xff));
//          gc.getPixelWriter().set
//        }
//      }
//    }
//
//    //Image fxImage = SwingFXUtils.toFXImage(image, null);
//    //gc.drawImage(fxImage, 0, 0, NES_WIDTH * SCALE, NES_HEIGHT * SCALE);
//
//    //image.flush();
//
//  }

 // public void updateScreen2(int[][] pixels) {
//    GraphicsContext gc = getGraphicsContext2D();
//    gc.setImageSmoothing(false);
//
//    BufferedImage image = new BufferedImage(NES_WIDTH * SCALE, NES_HEIGHT * SCALE, BufferedImage.TYPE_INT_RGB);
//    for (int x = 0; x < NES_WIDTH; x++) {
//      for (int y = 0; y < NES_HEIGHT; y++) {
//
//        int startx = (x * SCALE);
//        int starty = (y * SCALE);
//        int[] pixelsCurrent = new int[SCALE * SCALE];
//        int counter = 0;
//
//        for (int i = startx; i < startx + SCALE; i++) {
//          for (int j = starty; j < starty + SCALE; j++) {
//            //image.setRGB(i, j, pixels[x][y]);
//            pixelsCurrent[counter++] = pixels[x][y];
//          }
//        }
//        image.setRGB(startx, starty, SCALE, SCALE, pixelsCurrent, 0, 0);
//      }
//    }
//    Image fxImage = SwingFXUtils.toFXImage(image, null);
//    gc.drawImage(fxImage, 0, 0);

 // }



  public static ScreenNES getInstance() {
    return instance;
  }
}
