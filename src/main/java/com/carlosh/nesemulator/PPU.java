package com.carlosh.nesemulator;

import static java.lang.Thread.sleep;

import com.carlosh.nesemulator.ui.ScreenNES;
import java.util.Random;

public class PPU {

  private ROM rom;
  private int[][] nameTable = new int[2][2048];
  private int[] paletteTable = new int[32];
  private int[][] patternTable = new int[2][4096];

  private int[][] patternTable0;
  private int[][] patternTable1;

  // Registers
  private ControlRegister control = new ControlRegister();
  private MaskRegister mask = new MaskRegister();
  private StatusRegister status = new StatusRegister();

  // Color palette containing 64 colors (10 of them are just black (0x000000))
  private int[] colorPalette = new int[] {
      0x545454, 0x001E74, 0x081090, 0x300088, 0x440064, 0x5C0030, 0x540400, 0x3C1800,
      0x202A00, 0x083A00, 0x004000, 0x003C00, 0x00323C, 0x000000, 0x000000, 0x000000,
      0x989698, 0x084CC4, 0x3032EC, 0x5C1EE4, 0x8814B0, 0xA01464, 0x982220, 0x783C00,
      0x545A00, 0x287200, 0x087C00, 0x007628, 0x006678, 0x000000, 0x000000, 0x000000,
      0xECEEEC, 0x4C9AEC, 0x787CEC, 0xB062EC, 0xE454EC, 0xEC58B4, 0xEC6A64, 0xD48820,
      0xA0AA00, 0x74C400, 0x4CD020, 0x38CC6C, 0x38B4CC, 0x3C3C3C, 0x000000, 0x000000,
      0xECEEEC, 0xA8CCEC, 0xBCBCEC, 0xD4B2EC, 0xECAEEC, 0xECAED4, 0xECB4B0, 0xE4C490,
      0xCCD278, 0xB4DE78, 0xA8E290, 0x98E2B4, 0xA0D6E4, 0xA0A2A0, 0x000000, 0x000000
  };

  private int count = 0;

  private int[][] pixels = new int[ScreenNES.NES_WIDTH][ScreenNES.NES_HEIGHT];
  private int currentX = 0;
  private int currentY = 0;

  private int whichByte = 0;
  private int buffer = 0;
  private int addressPPU = 0;

  // TODO
  public int cpuRead(int address, boolean readOnly) {
    int data = -1;
    switch (address) {
      case 0x0000: {
        // Control
      }
      case 0x0001: {
        // Mask
      }
      case 0x0002: {
        // Status
        status.write(status.value | 0x7F);
        data = (status.value & 0xE0) | (buffer & 0x1F);
        status.write(status.value & 0x7F);
        whichByte = 0;
      }
      case 0x0003: {
        // OAM Address
      }
      case 0x0004: {
        // OAM Data
      }
      case 0x0005: {
        // Scroll
      }
      case 0x0006: {
        // PPU Address
      }
      case 0x0007: {
        // PPU Data
        data = buffer;
        buffer = ppuRead(addressPPU, readOnly);
        if (addressPPU > 0x3F00) {
          data = buffer;
        }
        addressPPU++;
      }
    }

    assert data != -1;
    return data;
  }

  // TODO
  public void cpuWrite(int address, int data) {
    switch (address) {
      case 0x0000: {
        // Control
        control.write(data);
      }
      case 0x0001: {
        // Mask
        mask.write(data);
      }
      case 0x0002: {
        // Status
      }
      case 0x0003: {
        // OAM Address
      }
      case 0x0004: {
        // OAM Data
      }
      case 0x0005: {
        // Scroll
      }
      case 0x0006: {
        // PPU Address
        addressPPU &= 0xFF00;
        if (whichByte == 0) {
          addressPPU |= (data << 8);
          whichByte = 1;
        } else {
          addressPPU |= data;
          whichByte = 0;
        }
      }
      case 0x0007: {
        // PPU Data
        ppuWrite(addressPPU++, data);
      }
    }
  }

  // TODO
  public int ppuRead(int address, boolean readOnly) {
    address &= 0x3FFF;
    if (rom.ppuRead(address, readOnly)) {
      return 0x00;
    }
    // Pattern memory
    else if (address <= 0x1FFF) {
      return patternTable[(address & 0x1000) >> 12][address & 0x0FFF];
    }
    // Name table memory
    else if (address <= 0x3EFF) {

    }
    // Palette memory
    else {
      address &= 0x001F;
      if (address == 0x0010) {
        address = 0x0000;
      } else if (address == 0x0014) {
        address = 0x0004;
      } else if (address == 0x0018) {
        address = 0x0008;
      } else if (address == 0x001C) {
        address = 0x000C;
      }
      return paletteTable[address];
    }

    return 0x00;
  }

  // TODO
  public void ppuWrite(int address, int data) {
    address &= 0x3FFF;
    if (rom.ppuWrite(address, data)) {
      return;
    }
    // Pattern memory
    else if (address <= 0x1FFF) {
      patternTable[(address & 0x1000) >> 12][address & 0x0FFF] = data;
    }
    // Name table memory
    else if (address <= 0x3EFF) {
      address &= 0x0FFF;
      // TODO

    }
    // Palette memory
    else {
      address &= 0x001F;
      if (address == 0x0010) {
        address = 0x0000;
      } else if (address == 0x0014) {
        address = 0x0004;
      } else if (address == 0x0018) {
        address = 0x0008;
      } else if (address == 0x001C) {
        address = 0x000C;
      }
      paletteTable[address] = data;
    }
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

  /**
   * Get an updated pattern table.
   *
   * @param i Pattern table number
   * @param palette Palette number
   * @return Updated pattern table
   */
  public int[][] getPatternTable(int i, int palette) {
    // Pattern table to be returned
    int [][] patternTable = new int[256][256];

    // Least and most significant bits for each pixel
    int lsb;
    int msb;

    int offset;
    int base;

    for (int x = 0; x < 16; x++) {
      for (int y = 0; y < 16; y++) {
        offset = y * 256 + x * 16;

        for (int row = 0; row < 8; row++) {
          base = i * 0x1000 + offset + row;
          lsb = ppuRead(base, false);
          msb = ppuRead(base + 0x0008, false);

          for (int col = 0; col < 8; col++) {
            int pixel = (msb & 0x01) << 1 | (lsb & 0x01);
            lsb >>= 1;
            msb >>= 1;

            int x_pos = x * 8 + 7 - col;
            int y_pos = y * 8 + row;
            int color = getPaletteColor(palette, pixel);

            patternTable[x_pos][y_pos] = color;
          }
        }

      }
    }

    if (i == 0) {
      patternTable0 = patternTable;
    } else {
      patternTable1 = patternTable;
    }
    return patternTable;
  }

  private int getPaletteColor(int palette, int pixel) {
    int val = ppuRead(0x3F00 + palette * 4 + pixel, false);
    return colorPalette[val & 0x3F];
  }

  // Inner class for the PPU's control register.
  private class ControlRegister {
    private int value;

    public ControlRegister() {
      value = 0x00;
    }

    public void write(int data) {
      value = data;
    }

    public int getNametableX() {
      return value & 0x01;
    }

    public int getNametableY() {
      return (value & 0x02) >> 1;
    }

    public int getIncrementMode() {
      return (value & 0x04) >> 2;
    }

    public int getPatternSprite() {
      return (value & 0x08) >> 3;
    }

    public int getPatternBackground() {
      return (value & 0x10) >> 4;
    }

    public int getSpriteSize() {
      return (value & 0x20) >> 5;
    }

    public int getSlaveMode() {
      return (value & 0x40) >> 6;
    }

    public int getEnableNMI() {
      return (value & 0x80) >> 7;
    }
  }

  // Inner class for the PPU's mask register.
  private class MaskRegister {
    private int value;

    public MaskRegister() {
      value = 0x00;
    }

    public void write(int data) {
      value = data;
    }

    public int getGrayscale() {
      return value & 0x01;
    }

    public int getShowLeftBackground() {
      return (value & 0x02) >> 1;
    }

    public int getShowLeftSprites() {
      return (value & 0x04) >> 2;
    }

    public int getShowBackground() {
      return (value & 0x08) >> 3;
    }

    public int getShowSprites() {
      return (value & 0x10) >> 4;
    }

    public int getEmphasizeRed() {
      return (value & 0x20) >> 5;
    }

    public int getEmphasizeGreen() {
      return (value & 0x40) >> 6;
    }

    public int getEmphasizeBlue() {
      return (value & 0x80) >> 7;
    }
  }

  // Inner class for the PPU's status register.
  private class StatusRegister {
    private int value;

    public StatusRegister() {
      value = 0x00;
    }

    public void write(int data) {
      value = data;
    }

    public int getVerticalBlank() {
      return (value & 0x80) >> 7;
    }

    public int getSpriteZeroHit() {
      return (value & 0x40) >> 6;
    }

    public int getSpriteOverflow() {
      return (value & 0x20) >> 5;
    }
  }





}
