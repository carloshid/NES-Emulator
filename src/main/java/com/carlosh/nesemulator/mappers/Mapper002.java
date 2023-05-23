//package com.carlosh.nesemulator.mappers;
//
//import com.carlosh.nesemulator.ROM;
//
///**
// * TODO : Mapper 002: UNROM.
// */
//public class Mapper002 implements Mapper {
//
//  private int prgBanks;
//  private int chrBanks;
//
//  private ROM rom;
//
//  public Mapper002(int prgBanks, int chrBanks, ROM rom) {
//    this.prgBanks = prgBanks;
//    this.chrBanks = chrBanks;
//    this.rom = rom;
//
//    for (int i = 0; i < 16; ++i) {
//      rom.prgMap[i] = (1024 * i) & (rom.prgSize - 1);
//    }
//    for (int i = 1; i <= 16; ++i) {
//      rom.prgMap[32 - i] = rom.prgSize - (1024 * i);
//    }
//    for (int i = 0; i < 8; ++i) {
//      rom.chrMap[i] = (1024 * i) & (rom.chrSize - 1);
//    }
//  }
//
//  @Override
//  public int[] cpuRead(int address) {
//    if (address >= 0x8000) {
//      int d = rom.prgROM.get(rom.prgMap[((address & 0x7fff)) >> 10] + (address & 1023));
//      return new int[] {-1, d};
//    } else if (address >= 0x6000 && rom.chrROM.size() == 0) {
//      return new int[] {-2, 1};
//    }
//    return new int[] {-2, 0};
//
//  }
//
//  @Override
//  public int cpuWrite(int address, int data) {
//    if (address >= 0x8000 && address <= 0xFFFF) {
//      int bank = data & 0x0F;
//      for (int i = 0; i < 16; ++i) {
//        rom.prgMap[i] = (((bank * 16) + i) * 1024) & (rom.prgSize - 1);
//      }
//      return -3;
//    }
//
//    return -2;
//  }
//
//
//  @Override
//  public int ppuRead(int address) {
//    return (address < 0x2000) ? address : -2;
//  }
//
//  @Override
//  public int ppuWrite(int address) {
//    return (address < 0x2000 && chrBanks == 0) ? address : -2;
//  }
//
//  @Override
//  public MirroringMode getMirroringMode() {
//    return MirroringMode.HARDWARE;
//  }
//}
