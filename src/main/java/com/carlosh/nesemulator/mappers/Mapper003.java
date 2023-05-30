package com.carlosh.nesemulator.mappers;

import com.carlosh.nesemulator.ROM;

/**
 * Mapper 003: CNROM.
 */
public class Mapper003 implements Mapper {

  private ROM rom;

  public Mapper003(ROM rom) {
    this.rom = rom;
  }

  @Override
  public int cpuRead(int address) {
    return -2;
  }

  @Override
  public int cpuWrite(int address, int data) {
    if (address >= 0x8000 && address <= 0xFFFF) {
      for (int i = 0; i < 8; i++) {
        rom.chrMap[i] = ((((data & 0xFF) * 8) + i) * 1024) & (rom.chrSize - 1);
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
    return MirroringMode.HARDWARE;
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
