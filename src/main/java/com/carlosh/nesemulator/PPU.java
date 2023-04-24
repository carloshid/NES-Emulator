package com.carlosh.nesemulator;

import static java.lang.Thread.sleep;

import com.carlosh.nesemulator.ui.ScreenNES;
import java.util.Random;

public class PPU {

  private ROM rom;
  private int nameTable[][] = new int[2][2048];
  private int paletteTable[] = new int[32];

  private int count = 0;

  private int[][] pixels = new int[ScreenNES.NES_WIDTH][ScreenNES.NES_HEIGHT];
  private int currentX = 0;
  private int currentY = 0;

  // TODO
  public int cpuRead(int address, boolean readOnly) {
    return 0x00;
  }

  // TODO
  public void cpuWrite(int address, int data) {
    System.out.println("ROM: " + address + " " + data);
  }

  // TODO
  public int ppuRead(int address, boolean readOnly) {
    address &= 0x3FFF;
    if (rom.ppuRead(address, readOnly)) {
      return 0x00;
    }
    return 0x00;
  }

  // TODO
  public void ppuWrite(int address, int data) {
    address &= 0x3FFF;
    if (rom.ppuWrite(address, data)) {
      return;
    }
    System.out.println("ROM: " + address + " " + data);
  }

  public void addROM(ROM rom) {
    this.rom = rom;
  }

  // TODO
  public void clock() {
    drawPixel(currentX, currentY, ((new Random().nextInt()) % 2) == 0 ? 0xFFFFFF : 0x000000);

    currentX++;
    if (currentX >= ScreenNES.NES_WIDTH) {
      currentX = 0;
      currentY++;
      if (currentY >= ScreenNES.NES_HEIGHT) {
        currentY = 0;
        ScreenNES.getInstance().updateScreen(pixels);
        try {
          System.out.println(++count);
          sleep(17);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }

  }


  private void drawPixel(int x, int y, int color) {
    pixels[x][y] = color;
  }




}
