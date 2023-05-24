package com.carlosh.nesemulator.mappers;

import com.carlosh.nesemulator.ROM;

/**
 * Mapper 003: CNROM.
 */
public class Mapper003 implements Mapper {

  private int prgBanks;
  private int chrBanks;

  private ROM rom;

  public Mapper003(int prgBanks, int chrBanks, ROM rom) {
    this.prgBanks = prgBanks;
    this.chrBanks = chrBanks;
    this.rom = rom;
    for (int i = 0; i < 32; ++i) {
      rom.prgMap[i] = (1024 * i) & (rom.prgSize - 1);
    }
    for (int i = 0; i < 8; ++i) {
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
      for (int i = 0; i < 8; ++i) {
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
  public int ppuWrite(int address) {
    return -2;
  }

  @Override
  public MirroringMode getMirroringMode() {
    return MirroringMode.HARDWARE;
  }
}
