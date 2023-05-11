package com.carlosh.nesemulator.mappers;

/**
 * TODO : Mapper 001: MMC1.
 */
public class Mapper001 implements Mapper {

  private int prgBanks;
  private int chrBanks;

  public Mapper001(int prgBanks, int chrBanks) {
    this.prgBanks = prgBanks;
    this.chrBanks = chrBanks;
  }

  @Override
  public int[] cpuRead(int address) {
    if (address >= 0x8000 && address <= 0xFFFF) {
      int mappedAddress = address & (prgBanks > 1 ? 0x7FFF : 0x3FFF);
      return new int[] { mappedAddress, 0 };
    }

    return new int[] {-2, 0};
  }

  @Override
  public int cpuWrite(int address) {
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
}
