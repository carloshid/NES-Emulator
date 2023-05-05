package com.carlosh.nesemulator;

import com.carlosh.nesemulator.mappers.Mapper;
import com.carlosh.nesemulator.mappers.Mapper000;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

public class ROM {
  private int[] romBytes;
  private String filename;

  private List<Integer> prgROM = new ArrayList<>();
  private List<Integer> chrROM = new ArrayList<>();
  private ROM_Header header;
  private Mapper mapper;

  private int mapperID = 0;
  private int prgBanks = 0;
  private int chrBanks = 0;

  private boolean valid = false;
  private Mirror mirror = Mirror.HORIZONTAL;

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

  public enum Mirror {
    HORIZONTAL,
    VERTICAL,
    ONESCREEN_LO,
    ONESCREEN_HI
  }

  public ROM(String filename) {
    this.header = new ROM_Header();
    valid = false;


    romBytes = readFile(filename);

    this.filename = filename;

    header = loadHeader(romBytes);


    int start = 16;
    if ((header.mapper1 & 0x04) != 0) {
      start = 512;
    }

    // Mapper id and mirror
    mapperID = ((header.mapper2 >> 4) << 4) | (header.mapper1 >> 4);
    mirror = (header.mapper1 & 0x01) != 0 ? Mirror.VERTICAL : Mirror.HORIZONTAL;

    // File type
    int fileType = (header.mapper2 & 0x0C) == 0x08 ? 2 : 1;

    if (fileType == 1) {
      prgBanks = header.prgROMSize;
      for (int i = start; i < start + prgBanks * 16384; i++) {
        prgROM.add(romBytes[i]);
      }
      chrBanks = header.chrROMSize;
      start = start + prgBanks * 16384;
      int end = start + Math.min(chrBanks * 8192, 8192);
      for (int i = start; i < end; i++) {
        chrROM.add(romBytes[i]);
      }
    } else if (fileType == 2) {
      prgBanks = ((header.prgRAMSize & 0x07) << 8) | header.prgROMSize;
      for (int i = start; i < start + prgBanks * 16384; i++) {
        prgROM.add(romBytes[i]);
      }
      chrBanks = ((header.prgRAMSize & 0x38) << 8) | header.chrROMSize;
      start = start + prgBanks * 16384;
      int end = start + chrBanks * 8192;
      for (int i = start; i < end; i++) {
        chrROM.add(romBytes[i]);
      }
    }
    // Load mapper
    switch (mapperID) {
      case 0: {
        mapper = new Mapper000(prgBanks, chrBanks);
        break;
      }
      // TODO: Add more mappers
    }

    valid = true;
  }

  public int cpuRead(int address, boolean readOnly) {
    int mappedAddress = mapper.cpuRead(address);
    if (mappedAddress != -1) {
      return prgROM.get(mappedAddress);
    }
    return -1;
  }

  public boolean cpuWrite(int address, int data) {
    int mappedAddress = mapper.cpuWrite(address);
    if (mappedAddress != -1) {
      prgROM.set(mappedAddress, data);
      return true;
    }
    return false;
  }

  public boolean ppuRead(int address, boolean readOnly) {
    int mappedAddress = mapper.ppuRead(address);
    if (mappedAddress != -1) {
      int data = chrROM.get(mappedAddress);
      return true;
    }
    return false;
  }

  public boolean ppuWrite(int address, int data) {
    int mappedAddress = mapper.ppuWrite(address);
    if (mappedAddress != -1) {
      chrROM.set(mappedAddress, data);
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

  public Mirror getMirror() {
    return mirror;
  }


}
