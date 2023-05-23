package com.carlosh.nesemulator;

import com.carlosh.nesemulator.mappers.Mapper;
import com.carlosh.nesemulator.mappers.Mapper000;
//import com.carlosh.nesemulator.mappers.Mapper001;
import com.carlosh.nesemulator.mappers.Mapper002;
import com.carlosh.nesemulator.mappers.MirroringMode;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.NoSuchFileException;
import java.util.ArrayList;
import java.util.List;

public class ROM {
  private int[] romBytes;
  private String filename;

  //public List<Integer> prgROM = new ArrayList<>();
  public int[] prgROM;
  private int extraAddress = 0;
  //public List<Integer> chrROM = new ArrayList<>();
  public int[] chrROM;
  private ROM_Header header;
  private Mapper mapper;

  public int[] prgMap;
  public int[] chrMap;

  int[] chrRAM = new int[8192];
  int[] prgRAM = new int[8192];
  boolean hasChrRAM = false;

  private int mapperID = 0;
  private int prgBanks = 0;
  private int chrBanks = 0;

  public int prgSize = 0;
  public int chrSize = 0;

  private boolean valid = false;
  private MirroringMode mirror = MirroringMode.HORIZONTAL;

  private class ROM_Header {
    String name;
    int prgROMSize;
    int chrROMSize;
    int mapper1;
    int mapper2;
    int prgRAMSize;
    int tvSystem1;
    int tvSystem2;
    String unused;
  }

  public ROM(String filename) {
    this.header = new ROM_Header();
    valid = false;

    romBytes = readFile(filename);

    this.filename = filename;

    header = loadHeader(romBytes);


//    System.out.println("ROM name: " + header.name);
//    System.out.println("PRG ROM size: " + header.prgROMSize);
//    System.out.println("CHR ROM size: " + header.chrROMSize);


    int start = 16;
    if ((header.mapper1 & 0x04) != 0) {
      start = 512;
    }


    // Mapper id and mirror
    mapperID = ((header.mapper2 >> 4) << 4) | (header.mapper1 >> 4);
    mirror = (header.mapper1 & 0x01) != 0 ? MirroringMode.VERTICAL : MirroringMode.HORIZONTAL;


    // File type
    int fileType = (header.mapper2 & 0x0C) == 0x08 ? 2 : 1;

    //System.out.println("File type: " + fileType);

    if (fileType == 1) {
      prgBanks = header.prgROMSize;
      prgSize = prgBanks * 16384;

      prgROM = new int[prgSize];

      for (int i = start; i < start + prgBanks * 16384; i++) {
        //prgROM.add(romBytes[i]);
        prgROM[i - start] = romBytes[i];
      }

      chrBanks = header.chrROMSize;
      chrSize = chrBanks * 8192;

      chrROM = new int[chrSize];

      start = start + prgBanks * 16384;
      int end = start + Math.min(chrBanks * 8192, 8192);
      for (int i = start; i < end; i++) {
        //chrROM.add(romBytes[i]);
        chrROM[i - start] = romBytes[i];
      }

    } else if (fileType == 2) {
      prgBanks = ((header.prgRAMSize & 0x07) << 8) | header.prgROMSize;
      prgSize = prgBanks * 16384;

      prgROM = new int[prgSize];

      for (int i = start; i < start + prgBanks * 16384; i++) {
        //prgROM.add(romBytes[i]);
        prgROM[i - start] = romBytes[i];
      }

      //System.out.println("prgBanks: " + prgBanks);

      chrBanks = (((header.prgRAMSize & 0x38) << 8) & 0xFF) | header.chrROMSize;
      int a = 8192 * (header.chrROMSize + ((header.tvSystem1 >> 4) << 8));
      chrSize = Math.min(romBytes.length - 16 - prgROM.length, a);

      chrROM = new int[chrSize];

      start = start + prgBanks * 16384;
      //int end = start + chrBanks * 8192;
      int end = start + chrSize;

      //System.out.println("chrBanks: " + chrBanks);
      //System.out.println(start + " " + end + " " + romBytes.length);

      for (int i = start; i < end; i++) {
        //chrROM.add(romBytes[i]);
        chrROM[i - start] = romBytes[i];
      }

    }

    if (chrSize == 0) {
      chrSize = 8192;
      hasChrRAM = true;
      chrROM = new int[chrSize];
    }

    prgMap = new int[32];
    for (int i = 0; i < 32; ++i) {
      prgMap[i] = (1024 * i) & ((prgBanks * 16384) - 1);
    }
    chrMap = new int[8];
    for (int i = 0; i < 8; ++i) {
      chrMap[i] = (1024 * i) & ((chrSize) - 1);
    }

    // Load mapper
    switch (mapperID) {
      case 0: {
        mapper = new Mapper000(prgBanks, chrBanks, this);
        break;
      }
//      case 1: {
//        mapper = new Mapper001(prgBanks, chrBanks);
//        break;
//      }
      case 2: {
        mapper = new Mapper002(prgBanks, chrBanks, this);
        break;
      }
    }

    valid = true;
  }

  public int cpuRead(int address, boolean readOnly) {
    int mappedResult = mapper.cpuRead(address);

    if (mappedResult != -2) {
      return mappedResult;
    }

    if (address >= 0x8000) {
      int bank = (address & 0x7FFF) / 0x400;
      int offset = address & 0x3FF;

      //return prgROM.get(prgMap[bank] + offset);
      return prgROM[prgMap[bank] + offset];
    }

    return 0;

    //int mappedAddress = mapper.cpuRead(address);
//    if (mappedResult[0] == -1) {
//      //return extraAddress;
//      return mappedResult[1];
//    } else if (mappedResult[0] != -2) {
//      return prgROM.get(mappedResult[0]);
//    }
//    return -2;
  }

  public void cpuWrite(int address, int data) {
    int mappedAddress = mapper.cpuWrite(address, data);
    if (mappedAddress != -2) {
      return;
    }
    if (address >= 0x6000 && address < 0x8000) {
      prgRAM[address & 0x1FFF] = data;
    }
  }

  public int ppuRead(int address) {
    int mappedAddress = mapper.ppuRead(address);
    if (mappedAddress != -2) {
      return mappedAddress;
    }
    if (address < 0x2000) {
      int bank = address >> 10;
      int offset = address & 1023;

      int a = chrMap[bank] + offset;

      return chrRAM[a];
    }

    return -2;
  }

  public boolean ppuWrite(int address, int data) {
    int mappedAddress = mapper.ppuWrite(address);
    if (mappedAddress != -2) {
      return true;
    }
    address &= 0x3FFF;
    if (address < 0x2000 & hasChrRAM) {
      int bank = address >> 10;
      int offset = address & 1023;

      int a = chrMap[bank] + offset;

      chrRAM[a] = data;

      return true;
    }
    return false;
  }

  private static int[] readFile(String filename) {
    File file = new File(filename);
    byte[] bytes = new byte[(int) file.length()];
    FileInputStream stream;
    try {
      stream = new FileInputStream(file);
      int res = stream.read(bytes);
      stream.close();
    } catch (Exception e) {
      e.printStackTrace();
    }

    int[] romBytes = new int[bytes.length];
    for (int i = 0; i < bytes.length; i++) {
      romBytes[i] = bytes[i] & 0xFF;
    }

    return romBytes;
  }

  private ROM_Header loadHeader(int[] romBytes) {
    ROM_Header newHeader = new ROM_Header();
    StringBuilder name = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      name.append((char) romBytes[i]);
    }
    newHeader.name = name.toString();
    newHeader.unused = "";

    newHeader.prgROMSize = romBytes[4];
    newHeader.chrROMSize = romBytes[5];
    newHeader.mapper1 = romBytes[6];
    newHeader.mapper2 = romBytes[7];
    newHeader.prgRAMSize = romBytes[8];
    newHeader.tvSystem1 = romBytes[9];
    newHeader.tvSystem2 = romBytes[10];

    return newHeader;
  }

  public MirroringMode getMirror() {
    MirroringMode mode = mapper.getMirroringMode();
    if (mode == MirroringMode.HARDWARE) return mirror;
    else return mode;
  }


}
