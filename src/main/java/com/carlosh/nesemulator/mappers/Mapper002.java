package com.carlosh.nesemulator.mappers;

/**
 * TODO : Mapper 002: UNROM.
 */
public class Mapper002 implements Mapper {

  private int prgBanks;
  private int chrBanks;

  private int nPRGBanksSelectedLow = 0;
  private int nPRGBanksSelectedHigh = 0;

  public Mapper002(int prgBanks, int chrBanks) {
    this.prgBanks = prgBanks;
    this.chrBanks = chrBanks;
  }

  @Override
  public int[] cpuRead(int address) {
    if (address >= 0x8000 && address <= 0xFFFF) {
      int mappedAddress;
      if (address <= 0xBFFF) {
        mappedAddress = (nPRGBanksSelectedLow * 0x4000) + (address & 0x3FFF);
      } else {
        mappedAddress = (nPRGBanksSelectedHigh * 0x4000) + (address & 0x3FFF);
      }
      return new int[] { mappedAddress, 0 };
    }

    return new int[] {-2, 0};
  }

  @Override
  public int cpuWrite(int address, int data) {
    if (address >= 0x8000 && address <= 0xFFFF) {
      if ((data & 0x80) != 0) {
        nPRGBanksSelectedLow = prgBanks - 1;
        nPRGBanksSelectedHigh = prgBanks - 1;
      } else {
        nPRGBanksSelectedLow = data & 0x7;
      }
    }
    return -2;
  }


  @Override
  public int ppuRead(int address) {
    return (address < 0x2000) ? address : -2;
  }

  @Override
  public int ppuWrite(int address) {
    return (address < 0x2000 && chrBanks == 0) ? address : -2;
  }

  @Override
  public MirroringMode getMirroringMode() {
    return MirroringMode.HARDWARE;
  }
}
