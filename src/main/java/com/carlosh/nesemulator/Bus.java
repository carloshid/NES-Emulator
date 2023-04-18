package com.carlosh.nesemulator;

import java.util.Arrays;

public class Bus {

  public CPU cpu;
  int[] ram = new int[64 * 1024];

  public void write (int address, int data) {
    if (address >= 0x0000 && address <= 0xFFFF) {
      ram[address] = data;
    }
  }

  public int read(int address, boolean readOnly) {
    if (address >= 0x0000 && address <= 0xFFFF) {
      return ram[address];
    }
    return 0x00;
  }

  public Bus() {
    Arrays.fill(ram, 0x00);
    cpu = CPU.instance;
    cpu.connectBus(this);
  }


}
