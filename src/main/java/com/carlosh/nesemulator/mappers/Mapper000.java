package com.carlosh.nesemulator.mappers;

/**
 * Mapper 000: NROM.
 */
public class Mapper000 implements Mapper {

  private int prgBanks;
  private int chrBanks;

  public Mapper000(int prgBanks, int chrBanks) {
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
    if (address >= 0x8000 && address <= 0xFFFF) {
      return address & (prgBanks > 1 ? 0x7FFF : 0x3FFF);
    }
    return -2;
  }

  @Override
  public int ppuRead(int address) {
    if (address >= 0x0000 && address <= 0x1FFF) {
      return address;
    }
    return -2;
  }

  @Override
  public int ppuWrite(int address) {
    return -2;
  }
}
