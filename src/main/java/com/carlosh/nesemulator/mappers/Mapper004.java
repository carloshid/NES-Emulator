package com.carlosh.nesemulator.mappers;

import com.carlosh.nesemulator.CPU;
import com.carlosh.nesemulator.ROM;
import java.util.Arrays;

/**
 * Mapper 004: MMC3.
 */
public class Mapper004 implements Mapper {

  private MirroringMode mirroringMode = MirroringMode.HARDWARE;
  private ROM rom;
  private boolean bit6 = false; // Bit 6 of the last value written to $8000
  private boolean bit7 = false; // Bit 7 of the last value written to $8000
  private int[] bankSelect = new int[8];
  private int currentBankN = 0;

  private int irqCounterReload = 0;
  private int irqCounter = 0;
  private boolean irqEnable = false;
  private boolean irqReload = false;
  private boolean interrupted = false;

  public Mapper004(ROM rom) {
    this.rom = rom;
    for (int i = 1; i <= 32; i++) {
      rom.prgMap[32 - i] = - (1024 * i) + rom.prgSize;
    }
    Arrays.fill(rom.prgMap, 0, 8, 0);
    mapPrgBanks();
  }

  private void mapPrgBanks() {
    // Bit 6 of the last value written to $8000 swaps the PRG windows at $8000 and $C000.
    // The MMC3 uses one map if bit 6 was cleared to 0 (value & $40 == $00) and another
    // if set to 1 (value & $40 == $40).
    if (bit6) {
      for (int i = 0; i < 8; ++i) {
        rom.prgMap[i] = ((rom.prgSize - 16384) + 1024 * i);
        rom.prgMap[i + 16] = (1024 * (i + (bankSelect[6] * 8))) % rom.prgSize;
      }
    } else {
      for (int i = 0; i < 8; ++i) {
        rom.prgMap[i] = (1024 * (i + (bankSelect[6] * 8))) % rom.prgSize;
        rom.prgMap[i + 16] = ((rom.prgSize - 16384) + 1024 * i);
      }
    }
  }

  private void mapChrBanks() {
    // Bit 7 of the last value written to $8000 swaps the CHR banks.
    // The MMC3 uses one map if bit 7 was cleared to 0 (value & $80 == $00) and another
    // if set to 1 (value & $80 == $80).
    if (bit7) {
      rom.chrMap[0] = (1024 * bankSelect[2]) % rom.chrSize;
      rom.chrMap[1] = (1024 * bankSelect[3]) % rom.chrSize;
      rom.chrMap[2] = (1024 * bankSelect[4]) % rom.chrSize;
      rom.chrMap[3] = (1024 * bankSelect[5]) % rom.chrSize;
      rom.chrMap[4] = (1024 * (bankSelect[0] / 2) * 2 ) % rom.chrSize;
      rom.chrMap[5] = (1024 * ((bankSelect[0] / 2) * 2 + 1)) % rom.chrSize;
      rom.chrMap[6] = (1024 * (bankSelect[1] / 2) * 2 ) % rom.chrSize;
      rom.chrMap[7] = (1024 * ((bankSelect[1] / 2) * 2 + 1)) % rom.chrSize;
    } else {
      rom.chrMap[0] = (1024 * (bankSelect[0] / 2) * 2 ) % rom.chrSize;
      rom.chrMap[1] = (1024 * ((bankSelect[0] / 2) * 2 + 1)) % rom.chrSize;
      rom.chrMap[2] = (1024 * (bankSelect[1] / 2) * 2 ) % rom.chrSize;
      rom.chrMap[3] = (1024 * ((bankSelect[1] / 2) * 2 + 1)) % rom.chrSize;
      rom.chrMap[4] = (1024 * bankSelect[2]) % rom.chrSize;
      rom.chrMap[5] = (1024 * bankSelect[3]) % rom.chrSize;
      rom.chrMap[6] = (1024 * bankSelect[4]) % rom.chrSize;
      rom.chrMap[7] = (1024 * bankSelect[5]) % rom.chrSize;
    }
  }

  @Override
  public int cpuRead(int address) {
    return -2;
  }

  @Override
  public int cpuWrite(int address, int data) {

    if (address >= 0x8000 && address <= 0xFFFF) {

      if ((address & 1) != 0) {
        // Odd registers
        if (address <= 0x9FFF) {
          if (currentBankN < 6) {
            // CHR banks
            bankSelect[currentBankN] = data;
            mapChrBanks();
          } else if (currentBankN == 6) {
            // PRG bank at $8000-$9FFF
            bankSelect[6] = data;
            mapPrgBanks();
          } else if (currentBankN == 7) {
            // PRG bank at $A000-$BFFF
            for (int i = 0; i < 8; i++) {
              rom.prgMap[i + 8] = ((data * 8 + i) * 1024) % rom.prgSize;
            }
          }
        }

        else if (address >= 0xC000 && address <= 0xDFFF) {
          irqReload = true;
        } else if (address >= 0xE000) {
          irqEnable = true;
        }
      }

      else {
        // Even registers
        if (address <= 0x9FFF) {
          currentBankN = data & 7;
          bit6 = ((data & 0x40) != 0);
          bit7 = ((data & 0x80) != 0);
          mapChrBanks();
          mapPrgBanks();
        } else if (address <= 0xBFFF) {
          mirroringMode = (data & 1) != 0 ? MirroringMode.HORIZONTAL : MirroringMode.VERTICAL;
        }

        else if (address <= 0xDFFF) {
          irqCounterReload = data;
        } else {
          if (interrupted) {
            --irqCounter;
          }
          interrupted = false;
          irqEnable = false;
        }
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
  public void clockScanCounter() {
    if (irqCounter == 0 || irqReload) {
      irqCounter = irqCounterReload;
      irqReload = false;
    }
    else {
      --irqCounter;
    }
    if (irqCounter == 0 && irqEnable) {
      irq = true;
    }
  }

  private boolean irq = false;

  @Override
  public void setIrq(boolean state) {
    irq = state;
  }

  @Override
  public boolean getIrq() {
    return irq;
  }

  @Override
  public MirroringMode getMirroringMode() {
    return mirroringMode;
  }
}
