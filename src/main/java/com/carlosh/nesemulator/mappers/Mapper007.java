package com.carlosh.nesemulator.mappers;

import com.carlosh.nesemulator.ROM;

/**
 * Mapper 007: AxROM.
 */
public class Mapper007 implements Mapper {

  private ROM rom;

  MirroringMode mirror = MirroringMode.SINGLESCREENHIGH;

  public Mapper007(ROM rom) {
    this.rom = rom;
  }

  @Override
  public int cpuRead(int address) {
    return -2;
  }

  @Override
  public int cpuWrite(int address, int data) {
    if (address >= 0x8000 && address <= 0xFFFF) {
      for (int i = 0; i < 32; i++) {
        rom.prgMap[i] = ((((data & 0x0F) * 32) + i) * 1024) & (rom.prgSize - 1);
      }
      if ((data & 0x10) != 0) {
        mirror = MirroringMode.SINGLESCREENLOW;
      } else {
        mirror = MirroringMode.SINGLESCREENHIGH;
      }

      return 0;
    }
    return -2;
  }

  @Override
  public int ppuRead(int address) {
    return -2;
  }

  @Override
  public int ppuWrite(int address, int data) {
    return -2;
  }

  @Override
  public MirroringMode getMirroringMode() {
    return mirror;
  }

  @Override
  public void clockScanCounter() {

  }

  @Override
  public void setIrq(boolean state) {

  }

  @Override
  public boolean getIrq() {
    return false;
  }

}
