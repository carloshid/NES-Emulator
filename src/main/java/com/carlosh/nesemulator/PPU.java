package com.carlosh.nesemulator;

public class PPU {

  private ROM rom;
  private int nameTable[][] = new int[2][2048];
  private int paletteTable[] = new int[32];

  // TODO
  public int cpuRead(int address, boolean readOnly) {
    return 0x00;
  }

  // TODO
  public void cpuWrite(int address, int data) {
    System.out.println("ROM: " + address + " " + data);
  }

  // TODO
  public int ppuRead(int address, boolean readOnly) {
    address &= 0x3FFF;
    if (rom.ppuRead(address, readOnly)) {
      return 0x00;
    }
    return 0x00;
  }

  // TODO
  public void ppuWrite(int address, int data) {
    address &= 0x3FFF;
    if (rom.ppuWrite(address, data)) {
      return;
    }
    System.out.println("ROM: " + address + " " + data);
  }

  public void addROM(ROM rom) {
    this.rom = rom;
  }

  public void clock() {

  }

}
