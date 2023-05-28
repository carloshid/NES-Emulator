package com.carlosh.nesemulator.mappers;

import com.carlosh.nesemulator.ROM;

/**
 * Mapper 002: UNROM.
 */
public class Mapper002 implements Mapper {

  private ROM rom;

  public Mapper002(ROM rom) {
    this.rom = rom;
    for (int i = 0; i < 16; i++) {
      rom.prgMap[i] = (1024 * i) & (rom.prgSize - 1);
    }
    for (int i = 1; i <= 16; i++) {
      rom.prgMap[32 - i] = rom.prgSize - (1024 * i);
    }
    for (int i = 0; i < 8; i++) {
      rom.chrMap[i] = (1024 * i) & (rom.chrSize - 1);
    }
  }

  @Override
  public int cpuRead(int address) {
    return -2;
  }

  @Override
  public int cpuWrite(int address, int data) {
    if (address >= 0x8000 && address <= 0xFFFF) {
      int bank = data & 0x0F;
      for (int i = 0; i < 16; i++) {
        rom.prgMap[i] = (((bank * 16) + i) * 1024) & (rom.prgSize - 1);
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
  public int ppuWrite(int address) {
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
