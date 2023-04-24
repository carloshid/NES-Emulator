package com.carlosh.nesemulator;

import java.util.Arrays;

public class Bus {

  private PPU ppu;
  private CPU cpu;
  private ROM rom;
  private int clockCounter = 0;
  int[] ram = new int[2048];

  public void write (int address, int data) {
    if (rom.cpuWrite(address, data)) {
      return;
    }
    if (address >= 0x0000 && address <= 0x1FFF) {
      ram[address & 0x07FF] = data;
    } else if (address >= 0x2000 && address <= 0x3FFF) {
      ppu.cpuWrite(address & 0x0007, data);
    }
  }

  public int read(int address, boolean readOnly) {
    if (rom.cpuRead(address, readOnly)) {
      return 0x00;
    }
    if (address >= 0x0000 && address <= 0x1FFF) {
      return ram[address & 0x07FF];
    } else if (address >= 0x2000 && address <= 0x3FFF) {
      return ppu.cpuRead(address & 0x0007, readOnly);
    }
    return 0x00;
  }

  public Bus() {
    Arrays.fill(ram, 0x00);
    cpu = CPU.instance;
    cpu.connectBus(this);
  }

  public void addROM(ROM rom) {
      this.rom = rom;
      ppu.addROM(rom);
  }

  public void reset() {
    cpu.reset();
    clockCounter = 0;
  }

  public void clock() {
    ppu.clock();
    if (clockCounter % 3 == 0) {
      try {
        cpu.clockCycle();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    clockCounter++;
  }


}
