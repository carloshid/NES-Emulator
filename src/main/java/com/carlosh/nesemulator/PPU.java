package com.carlosh.nesemulator;

import static java.lang.Thread.sleep;

import com.carlosh.nesemulator.ui.ScreenNES;
import java.util.Random;

public class PPU {

  /**
   * The PPU is a singleton.
   */
  public static final PPU instance = new PPU();

  private PPU() {
  }

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
  private LoopyRegister vramAddress = new LoopyRegister();
  private LoopyRegister tramAddress = new LoopyRegister();

  private int fineX = 0x00;
  private int bgNextTileId = 0x00;
  private int bgNextTileAttrib = 0x00;
  private int bgNextTileLsb = 0x00;
  private int bgNextTileMsb = 0x00;
  private int bgShifterPatternLo = 0x0000;
  private int bgShifterPatternHi = 0x0000;
  private int bgShifterAttribLo = 0x0000;
  private int bgShifterAttribHi = 0x0000;

  // Color palette containing 64 colors (10 of them are just black (0x000000))
  private int[] colorPalette2 = new int[] {
      0x545454, 0x001E74, 0x081090, 0x300088, 0x440064, 0x5C0030, 0x540400, 0x3C1800,
      0x202A00, 0x083A00, 0x004000, 0x003C00, 0x00323C, 0x000000, 0x000000, 0x000000,
      0x989698, 0x084CC4, 0x3032EC, 0x5C1EE4, 0x8814B0, 0xA01464, 0x982220, 0x783C00,
      0x545A00, 0x287200, 0x087C00, 0x007628, 0x006678, 0x000000, 0x000000, 0x000000,
      0xECEEEC, 0x4C9AEC, 0x787CEC, 0xB062EC, 0xE454EC, 0xEC58B4, 0xEC6A64, 0xD48820,
      0xA0AA00, 0x74C400, 0x4CD020, 0x38CC6C, 0x38B4CC, 0x3C3C3C, 0x000000, 0x000000,
      0xECEEEC, 0xA8CCEC, 0xBCBCEC, 0xD4B2EC, 0xECAEEC, 0xECAED4, 0xECB4B0, 0xE4C490,
      0xCCD278, 0xB4DE78, 0xA8E290, 0x98E2B4, 0xA0D6E4, 0xA0A2A0, 0x000000, 0x000000
  };

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
  private int cycle = 0;
  private boolean nmi = false;

  private int[][] pixels = new int[ScreenNES.NES_WIDTH][ScreenNES.NES_HEIGHT];
  private int currentX = 0;
  private int currentY = 0;

  private int whichByte = 0;
  private int buffer = 0;



  // TODO
  public int cpuRead(int address, boolean readOnly) {
    int data = 0;
    switch (address) {
      case 0x0000: {
        // Control
        break;
      }
      case 0x0001: {
        // Mask
        break;
      }
      case 0x0002: {
        // Status
        data = (status.value & 0xE0) | (buffer & 0x1F);
        status.setVerticalBlank(false);
        whichByte = 0;
        break;
      }
      case 0x0003: {
        // OAM Address
        break;
      }
      case 0x0004: {
        // OAM Data
        break;
      }
      case 0x0005: {
        // Scroll
        break;
      }
      case 0x0006: {
        // PPU Address
        break;
      }
      case 0x0007: {
        // PPU Data
        data = buffer;
        buffer = ppuRead(vramAddress.value, false);
        if (vramAddress.value >= 0x3F00) {
          data = buffer;
        }
        if (control.getIncrementMode() == 0) {
          vramAddress.value++;
        } else {
          vramAddress.value += 32;
        }
        break;
      }
    }

    //assert data != -2;
    return data;
  }

  // TODO
  public void cpuWrite(int address, int data) {
    //System.out.println("CPU write: " + Integer.toHexString(address) + " " + Integer.toHexString(data));
    switch (address) {
      case 0x0000: {
        // Control
        control.write(data);
        tramAddress.setNametableX(control.getNametableX());
        tramAddress.setNametableY(control.getNametableY());
        break;
      }
      case 0x0001: {
        System.out.println("Writing to mask");
        // Mask
        mask.write(data);
        break;
      }
      case 0x0002: {
        // Status
        break;
      }
      case 0x0003: {
        // OAM Address
        break;
      }
      case 0x0004: {
        // OAM Data
        break;
      }
      case 0x0005: {
        // Scroll
        if (whichByte == 0) {
          fineX = data & 0x07;
          tramAddress.setCoarseX(data >> 3);
          whichByte = 1;
        } else {
          tramAddress.setFineY(data & 0x07);
          tramAddress.setCoarseY(data >> 3);
          whichByte = 0;
        }
        break;
      }
      case 0x0006: {
        // PPU Address
        //tramAddress.value &= 0xFF00;
        if (whichByte == 0) {
          tramAddress.value = ((data & 0x3F) << 8) | (tramAddress.value & 0x00FF);
          whichByte = 1;
        } else {
          tramAddress.value = (tramAddress.value & 0xFF00) | data;
          vramAddress.value = tramAddress.value;
          whichByte = 0;
        }
        break;
      }
      case 0x0007: {
        // PPU Data
        ppuWrite(vramAddress.value, data);
        if (control.getIncrementMode() == 0) {
          vramAddress.value++;
        } else {
          vramAddress.value += 32;
        }
        break;
      }
    }
  }

  // TODO
  public int ppuRead(int address, boolean readOnly) {
    address &= 0x3FFF;
    int romData = rom.ppuRead(address);
    if (romData != -2) {
      return romData;
    }
    // Pattern memory
    else if (address <= 0x1FFF) {
      return patternTable[(address & 0x1000) >> 12][address & 0x0FFF];
    }
    // Name table memory
    else if (address <= 0x3EFF) {
      address &= 0x0FFF;
      // Vertical mirroring
      if (rom.getMirror() == ROM.Mirror.VERTICAL) {
        int bit = (address & 0x0400) >> 10;
        return nameTable[bit][address & 0x03FF];
      }
      // Horizontal mirroring
      else if (rom.getMirror() == ROM.Mirror.HORIZONTAL) {
        int bit = (address & 0x0800) >> 11;
        return nameTable[bit][address & 0x03FF];
      }
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
      return paletteTable[address] & (mask.getGrayscale() != 0 ? 0x30 : 0x3F);
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
      // Vertical mirroring
      if (rom.getMirror() == ROM.Mirror.VERTICAL) {
        int bit = (address & 0x0400) >> 10;
        nameTable[bit][address & 0x03FF] = data;
      }
      // Horizontal mirroring
      else if (rom.getMirror() == ROM.Mirror.HORIZONTAL) {
        int bit = (address & 0x0800) >> 11;
        nameTable[bit][address & 0x03FF] = data;
      }
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

  public boolean getNonMaskableInterrupt() {
    return nmi;
  }

  public void setNonMaskableInterrupt(boolean val) {
    nmi = val;
  }

  public void clock() {
    if (currentY == 0 && currentX == 0) {
      currentX = 1;
    }

    if (currentY == -1 && currentX == 1) {
      status.setVerticalBlank(false);
    }

    if ((currentX >= 2 && currentX < 258) || (currentX >= 321 && cycle < 338) && currentY < 240) {
      updateBackgroundShifters();
      prepareBackground((currentX - 1) % 8);
    }

    if (currentX == 256 && currentY < 240) {
      incrementY();
    }

    if (currentX == 257 && currentY < 240) {
      loadBackgroundShifters();
      copyX();
    }

    if ((currentX == 338 || currentX == 340) && currentY < 240) {
      bgNextTileId = ppuRead(0x2000 | (vramAddress.value & 0x0FFF), false);
    }

    if (currentY == -1 && currentX >= 280 && currentX < 305) {
      copyY();
    }

    if (currentY == 241 && currentX == 1) {
      status.setVerticalBlank(true);
      if (control.getEnableNMI() == 1) {
        nmi = true;
      }
    }

    // Render background
    int bgPixel = 0x00;
    int bgPal = 0x00;
    int color = 0x000000;
    if (mask.getShowBackground() == 1) {
      //System.out.println("getting color");
      int bitMux = 0x8000 >> fineX;
      int p0Pixel = (bgShifterPatternLo & bitMux) > 0 ? 1 : 0;
      int p1Pixel = (bgShifterPatternHi & bitMux) > 0 ? 1 : 0;
      bgPixel = (p1Pixel << 1) | p0Pixel;
      int bgPal0 = (bgShifterAttribLo & bitMux) > 0 ? 1 : 0;
      int bgPal1 = (bgShifterAttribHi & bitMux) > 0 ? 1 : 0;
      bgPal = (bgPal1 << 1) | bgPal0;
      //System.out.println(p0Pixel + " " + p1Pixel + " " + bgPal0 + " " + bgPal1);
    }
    color = getPaletteColor(bgPal, bgPixel);
    if (color != 0x545454) {
      System.out.println(color);
    }

    drawPixel(currentX - 1, currentY, color);

    currentX++;
    if (currentX >= 341) {
      currentX = 0;
      currentY++;
      if (currentY >= 261) {
        currentY = -1;
        ScreenNES.getInstance().updateScreen(pixels);
        //try {
        //  //System.out.println(++count);
        //  //sleep(17);
        //} catch (InterruptedException e) {
        //  throw new RuntimeException(e);
        //}
      }
    }

  }


  private void drawPixel(int x, int y, int color) {
    if (x >= 0 && x < ScreenNES.NES_WIDTH && y >= 0 && y < ScreenNES.NES_HEIGHT) {
      pixels[x][y] = color;
    }
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
    //System.out.println(palette + " " + pixel);
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

    public void setNametableX(boolean val) {
      value = val ? value | 0x01 : value & 0xFE;
    }

    public void setNametableY(boolean val) {
      value = val ? value | 0x02 : value & 0xFD;
    }

    public void setIncrementMode(boolean val) {
      value = val ? value | 0x04 : value & 0xFB;
    }

    public void setPatternSprite(boolean val) {
      value = val ? value | 0x08 : value & 0xF7;
    }

    public void setPatternBackground(boolean val) {
      value = val ? value | 0x10 : value & 0xEF;
    }

    public void setSpriteSize(boolean val) {
      value = val ? value | 0x20 : value & 0xDF;
    }

    public void setSlaveMode(boolean val) {
      value = val ? value | 0x40 : value & 0xBF;
    }

    public void setEnableNMI(boolean val) {
      value = val ? value | 0x80 : value & 0x7F;
    }
  }

  // Inner class for the PPU's mask register.
  private class MaskRegister {
    private int value;

    public MaskRegister() {
      value = 0x00;
      this.setShowBackground(true);
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

    public void setGrayscale(boolean val) {
      value = val ? value | 0x01 : value & 0xFE;
    }

    public void setShowLeftBackground(boolean val) {
      value = val ? value | 0x02 : value & 0xFD;
    }

    public void setShowLeftSprites(boolean val) {
      value = val ? value | 0x04 : value & 0xFB;
    }

    public void setShowBackground(boolean val) {
      value = val ? value | 0x08 : value & 0xF7;
    }

    public void setShowSprites(boolean val) {
      value = val ? value | 0x10 : value & 0xEF;
    }

    public void setEmphasizeRed(boolean val) {
      value = val ? value | 0x20 : value & 0xDF;
    }

    public void setEmphasizeGreen(boolean val) {
      value = val ? value | 0x40 : value & 0xBF;
    }

    public void setEmphasizeBlue(boolean val) {
      value = val ? value | 0x80 : value & 0x7F;
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

    public void setVerticalBlank(boolean val) {
      value = val ? value | 0x80 : value & 0x7F;
    }

    public void setSpriteZeroHit(boolean val) {
      value = val ? value | 0x40 : value & 0xBF;
    }

    public void setSpriteOverflow(boolean val) {
      value = val ? value | 0x20 : value & 0xDF;
    }
  }

  // Inner class for the PPU's loopy register.
  private class LoopyRegister {
    private int value;

    public LoopyRegister() {
      value = 0x00;
    }

    public void write(int data) {
      value = data;
    }

    public int getCoarseX() {
      return value & 0x001F;
    }

    public int getCoarseY() {
      return (value & 0x03E0) >> 5;
    }

    public int getNametableX() {
      return (value & 0x0400) >> 10;
    }

    public int getNametableY() {
      return (value & 0x0800) >> 11;
    }

    public int getFineY() {
      return (value & 0x7000) >> 12;
    }

    public void setCoarseX(int val) {
      value = (value & 0xFFE0) | (val & 0x001F);
    }

    public void setCoarseY(int val) {
      value = (value & 0xFC1F) | ((val & 0x001F) << 5);
    }

    public void setNametableX(int val) {
      value = (value & 0xFBFF) | ((val & 0x0001) << 10);
    }

    public void setNametableY(int val) {
      value = (value & 0xF7FF) | ((val & 0x0001) << 11);
    }

    public void setFineY(int val) {
      value = (value & 0x8FFF) | ((val & 0x0007) << 12);
    }
  }

  private void incrementX() {
    if ((mask.getShowBackground() + mask.getShowSprites()) != 0) {
      if (vramAddress.getCoarseX() == 31) {
        vramAddress.setCoarseX(0);
        vramAddress.setNametableX((vramAddress.getNametableX() + 1) % 2);
      } else {
        vramAddress.setCoarseX(vramAddress.getCoarseX() + 1);
      }
    }
  }

  private void incrementY() {
    if ((mask.getShowBackground() + mask.getShowSprites()) != 0) {
      if (vramAddress.getFineY() < 7) {
        vramAddress.setFineY(vramAddress.getFineY() + 1);
      } else {
        vramAddress.setFineY(0);
        if (vramAddress.getCoarseY() == 29) {
          vramAddress.setCoarseY(0);
          vramAddress.setNametableY((vramAddress.getNametableY() + 1) % 2);
        } else if (vramAddress.getCoarseY() == 31) {
          vramAddress.setCoarseY(0);
        } else {
          vramAddress.setCoarseY(vramAddress.getCoarseY() + 1);
        }
      }
    }
  }

  private void copyX() {
    if ((mask.getShowBackground() + mask.getShowSprites()) != 0) {
      vramAddress.setNametableX(tramAddress.getNametableX());
      vramAddress.setCoarseX(tramAddress.getCoarseX());
    }
  }

  private void copyY() {
    if ((mask.getShowBackground() + mask.getShowSprites()) != 0) {
      vramAddress.setFineY(tramAddress.getFineY());
      vramAddress.setNametableY(tramAddress.getNametableY());
      vramAddress.setCoarseY(tramAddress.getCoarseY());
    }
  }

  private void loadBackgroundShifters() {
    bgShifterPatternLo = (bgShifterPatternLo & 0xFF00) | bgNextTileLsb;
    bgShifterPatternHi = (bgShifterPatternHi & 0xFF00) | bgNextTileMsb;
    bgShifterAttribLo = (bgShifterAttribLo & 0xFF00) | ((bgNextTileAttrib & 0b01) != 0 ? 0xFF : 0x00);
    bgShifterAttribHi = (bgShifterAttribHi & 0xFF00) | ((bgNextTileAttrib & 0b10) != 0 ? 0xFF : 0x00);
  }

  private void updateBackgroundShifters() {
    if (mask.getShowBackground() != 0) {
      bgShifterPatternLo <<= 1;
      bgShifterPatternHi <<= 1;
      bgShifterAttribLo <<= 1;
      bgShifterAttribHi <<= 1;
    }
  }

  private void prepareBackground(int step) {
    assert step >= 0 && step <= 7;
    if (step == 0) {
      loadBackgroundShifters();
      bgNextTileId = ppuRead(0x2000 | (vramAddress.value & 0x0FFF), false);
    } else if (step == 2) {
      bgNextTileAttrib = ppuRead(0x23C0 | (vramAddress.getNametableY() << 11)
          | (vramAddress.getNametableX() << 10) | ((vramAddress.getCoarseY() >> 2) << 3)
          | (vramAddress.getCoarseX() >> 2), false);
      if ((vramAddress.getCoarseY() & 0x02) != 0) {
        bgNextTileAttrib >>= 4;
      }
      if ((vramAddress.getCoarseX() & 0x02) != 0) {
        bgNextTileAttrib >>= 2;
      }
      bgNextTileAttrib &= 0x03;
    } else if (step == 4) {
      bgNextTileLsb = ppuRead((control.getPatternBackground() << 12)
          + (bgNextTileId << 4) + vramAddress.getFineY(), false);
    } else if (step == 6) {
      bgNextTileMsb = ppuRead((control.getPatternBackground() << 12)
          + (bgNextTileId << 4) + vramAddress.getFineY() + 8, false);
    } else if (step == 7) {
      incrementX();
    }
  }

  public void reset() {
    fineX = 0;
    whichByte = 0;
    buffer = 0;
    currentX = 0;
    currentY = 0;
    bgNextTileAttrib = 0;
    bgNextTileId = 0;
    bgNextTileLsb = 0;
    bgNextTileMsb = 0;
    bgShifterAttribHi = 0;
    bgShifterAttribLo = 0;
    bgShifterPatternHi = 0;
    bgShifterPatternLo = 0;
    status.value = 0;
    mask.value = 0;
    control.value = 0;
    tramAddress.value = 0;
    vramAddress.value = 0;
  }





}
