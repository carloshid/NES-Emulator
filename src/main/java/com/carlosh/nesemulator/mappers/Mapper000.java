package com.carlosh.nesemulator.mappers;

import com.carlosh.nesemulator.ROM;

/**
 * Mapper 000: NROM.
 */
public class Mapper000 implements Mapper {
  private ROM rom;

  public Mapper000(ROM rom) {
    this.rom = rom;
    int[] temp = new int[65536];
    System.arraycopy(rom.prgROM, 0, temp, 0x8000, rom.prgROM.length);
    if (rom.prgSize <= 16384) System.arraycopy(rom.prgROM, 0, temp, 0xc000, rom.prgROM.length);
    rom.prgROM = temp;
  }

  @Override
  public int cpuRead(int address) {
    if (address >= 0x8000 && address <= 0xFFFF) {
      //return rom.prgROM.get(address);
      return rom.prgROM[address];
    }
    return 0;
  }

  @Override
  public int cpuWrite(int address, int data) {
    return -2;
  }

  @Override
  public int ppuRead(int address) {
    if (address >= 0x0000 && address <= 0x1FFF) {
      return rom.chrROM[address];
    }
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
