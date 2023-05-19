package com.carlosh.nesemulator.mappers;

/**
 * TODO : Mapper 001: MMC1.
 */
public class Mapper001 implements Mapper {

  private int prgBanks;
  private int chrBanks;
  private int[] RAM = new int[32768];
  private int controlRegister;
  private int loadRegister;
  private int loadCount;
  private int CHR_Bank_4KB_Low;
  private int CHR_Bank_4KB_High;
  private int CHR_Bank_8KB;
  private int PRG_Bank_16KB_Low;
  private int PRG_Bank_16KB_High;
  private int PRG_Bank_32KB;
  private MirroringMode mirroringMode;



  public Mapper001(int prgBanks, int chrBanks) {
    this.prgBanks = prgBanks;
    this.chrBanks = chrBanks;
  }

  @Override
  public int[] cpuRead(int address) {
    if (address >= 0x6000 && address <= 0x7FFF) {
      int mappedData = RAM[address & 0x1FFF];

      return new int[] { -1, mappedData };

    } else if (address >= 0x8000) {
      int mappedAddress;
      if ((controlRegister & 0b010000) != 0 && address <= 0xBFFF) {
        mappedAddress = (PRG_Bank_16KB_Low * 16384) + (address & 0x3FFF);
      } else if ((controlRegister & 0b010000) != 0 && address <= 0xFFFF) {
        mappedAddress = (PRG_Bank_16KB_High * 16384) + (address & 0x3FFF);
      } else {
        mappedAddress = (PRG_Bank_32KB * 32768) + (address & 0x7FFF);
      }
      return new int[] { mappedAddress, 0 };
    }

    return new int[] {-2, 0};
  }

  @Override
  public int cpuWrite(int address, int data) {
    if (address >= 0x6000 && address <= 0x7FFF) {
      RAM[address & 0x1FFF] = data;
      return -1;

    } else if (address >= 0x8000) {
      if ((data & 0x80) != 0) {
        loadRegister = 0;
        loadCount = 0;
        controlRegister |= 0x0C;

      } else {
        loadRegister >>= 1;
        loadRegister |= (data & 0x01) << 4;
        loadCount++;

        if (loadCount == 5) {
          int targetRegister = (address >> 13) & 0x03;

          switch (targetRegister) {
            case 0:
              controlRegister = loadRegister & 0x1F;

              int i = controlRegister & 0x03;
              mirroringMode = MirroringMode.values()[i];
              break;
            case 1:
              if ((controlRegister & 0b10000) != 0) {
                CHR_Bank_4KB_Low = loadRegister & 0x1F;
              } else {
                CHR_Bank_8KB = loadRegister & 0x1E;
              }
              break;
            case 2:
              if ((controlRegister & 0b10000) != 0) {
                CHR_Bank_4KB_High = loadRegister & 0x1F;
              }
              break;
            case 3:
              int j = (controlRegister >> 2) & 0x03;

              if (j < 2) {
                PRG_Bank_32KB = (loadRegister & 0x0E) >> 1;
              } else if (j == 2) {
                PRG_Bank_16KB_Low = 0;
                PRG_Bank_16KB_High = loadRegister & 0x0F;
              } else {
                PRG_Bank_16KB_Low = loadRegister & 0x0F;
                PRG_Bank_16KB_High = prgBanks - 1;
              }
              break;
          }

          loadRegister = 0;
          loadCount = 0;
        }
      }

    }

    return -2;
  }

  @Override
  public int ppuRead(int address) {
    if (address < 0x2000 && chrBanks ==0) {
      return address;
    } else if (address < 0x2000) {
      int mappedAddress;
      if ((controlRegister & 0b10000) != 0) {
        if (address <= 0x0FFF) {
          mappedAddress = (CHR_Bank_4KB_Low * 4096) + (address & 0x0FFF);
        } else {
          mappedAddress = (CHR_Bank_4KB_High * 4096) + (address & 0x0FFF);
        }
      } else {
        mappedAddress = (CHR_Bank_8KB * 8192) + (address & 0x1FFF);
      }
      return mappedAddress;
    }
    return -2;
  }

  @Override
  public int ppuWrite(int address) {
    if (address < 0x2000 && chrBanks == 0) return address;
    else if (address < 0x2000) return 0;
    else return -2;
  }

  @Override
  public MirroringMode getMirroringMode() {
    return null;
  }
}
