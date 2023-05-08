package com.carlosh.nesemulator;

public class Updater {
  public static long previousTime, elapsedTime, currentTime, previousElapsedTime;

  public static void update() {
    currentTime = System.nanoTime();
    elapsedTime = currentTime - previousTime;
    previousTime = currentTime;

    previousElapsedTime = elapsedTime;


  }
}
