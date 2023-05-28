package com.carlosh.nesemulator.mappers;

import com.carlosh.nesemulator.ROM;

/**
 * Mapper 001: MMC1.
 */
public class Mapper001 implements Mapper {

  private MirroringMode mirroringMode;

  private int shiftRegister = 0;
  private int controlRegister = 0xc;
  private int chrBank0 = 0;
  private int chrBank1 = 0;
  private int prgBank = 0;
  private int counter = 0;
  private boolean boolSOROM = false;
  private ROM rom;


  public Mapper001(ROM rom) {
    this.rom = rom;
    for (int i = 0; i < 32; i++) {
      rom.prgMap[i] = (1024 * i) & (rom.prgSize - 1);
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

      if ((data & 0x80) != 0) {
        reset();
        return 0;
      }

      // Update shift register
      shiftRegister = (shiftRegister >> 1) + (data & 1) * 16;
      if (++counter < 5) return 0;

      if (address < 0xA000) {
        // Update control register
        controlRegister = shiftRegister & 0x1F;
        mirroringMode = MirroringMode.values()[controlRegister & 3];
      } else if (address < 0xC000) {
        // Update CHR bank 0
        chrBank0 = shiftRegister & 0x1F;
        if (rom.prgSize > 256 * 1024) {
          chrBank0 &= 0x0F;
          boolSOROM = (shiftRegister & 0x10) != 0;
        }
      } else if (address < 0xE000) {
        // Update CHR bank 1
        chrBank1 = shiftRegister & 0x1F;
        if (rom.prgSize > 256 * 1024) chrBank1 &= 0x0F;
      } else {
        // Update PRG bank
        prgBank = shiftRegister & 0x0F;
      }

      updateChrBanks();
      updatePrgBank();
      counter = 0;
      shiftRegister = 0;
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
    return mirroringMode;
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

  private void updateChrBanks() {
    if ((controlRegister & 0x10) == 0) {
      // 8 KB CHR bank, low bit ignored
      for (int i = 0; i < 8; i++) {
        rom.chrMap[i] = (((chrBank0 / 2) * 8 + i) * 1024) % rom.chrSize;
      }
    } else {
      // 4 KB CHR banks
      for (int i = 0; i < 4; i++) {
        rom.chrMap[i] = ((chrBank0 * 4 + i) * 1024) % rom.chrSize;
      }
      for (int i = 0; i < 4; i++) {
        rom.chrMap[i + 4] = ((chrBank1 * 4 + i) * 1024) % rom.chrSize;
      }
    }
  }

  private void updatePrgBank() {
    if ((controlRegister & 0x08) == 0) {
      // 32 KB mode, low bit ignored
      for (int i = 0; i < 32; i++) {
        rom.prgMap[i] = (((prgBank / 2) * 0x8000) + i * 1024) % rom.prgSize;
      }

    } else if ((controlRegister & 0x04) == 0) {
      for (int i = 0; i < 16; i++) {
        rom.prgMap[i] = (1024 * i);
      }
      for (int i = 0; i < 16; i++) {
        rom.prgMap[i + 16] = (prgBank * 0x4000 + i * 1024) % rom.prgSize;
      }

    } else {
      for (int i = 0; i < 16; i++) {
        rom.prgMap[i] = (prgBank * 0x4000 + i * 1024) % rom.prgSize;
      }
      for (int i = 1; i <= 16; i++) {
        int j = rom.prgSize - (1024 * i);
        rom.prgMap[32 - i] = j > 0x40000 ? j - 0x40000 : j;
      }
    }

    if (boolSOROM && (rom.prgSize > 0x40000)) {
      for (int i = 0; i < rom.prgMap.length; i++) {
        rom.prgMap[i] += 0x40000;
      }
    }
  }

  private void reset() {
    shiftRegister = 0;
    counter = 0;
    controlRegister |= 0xc;
    updateChrBanks();
    updatePrgBank();
  }
}
