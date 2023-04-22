package com.carlosh.nesemulator;

import com.carlosh.nesemulator.mappers.Mapper;
import com.carlosh.nesemulator.mappers.Mapper000;
import java.util.List;

public class ROM {

  private List<Integer> prgROM;
  private List<Integer> chrROM;
  private ROM_Header header;
  private Mapper mapper;

  private int mapperID = 0;
  private int prgBanks = 0;
  private int chrBanks = 0;

  private class ROM_Header {
    String name;
    int prgROMSize;
    int chrROMSize;
    int mapper1;
    int mapper2;
    int prgRAMSize;
    int tvSystem1;
    String unused;
  }

  public ROM(String name) {
    this.header = new ROM_Header();

    // TODO : Open file
    // TODO :Read file header

    // Load mapper
    switch (mapperID) {
      case 0: {
        mapper = new Mapper000(prgBanks, chrBanks);
        break;
      }
    }

    // TODO : close file
  }

  public boolean cpuRead(int address, boolean readOnly) {
    int mappedAddress = mapper.cpuRead(address);
    if (mappedAddress != -1) {
      int data = prgROM.get(mappedAddress);
      return true;
    }
    return false;
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
}
