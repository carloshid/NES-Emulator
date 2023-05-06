package com.carlosh.nesemulator;

import java.util.Arrays;

public class Bus {

  private PPU ppu;
  private CPU cpu;
  private ROM rom;
  private int clockCounter = 0;
  public int[] controller = new int[2];
  private int[] controllerState = new int[2];
  int[] ram = new int[2048];

  public void write (int address, int data) {
    if (rom.cpuWrite(address, data)) {
      return;
    }
    if (address >= 0x0000 && address <= 0x1FFF) {
      ram[address & 0x07FF] = data;
    } else if (address >= 0x2000 && address <= 0x3FFF) {
      //System.out.println("Writing");
      //System.out.println("Writing ppu.cpuWrite: " + (address & 0x0007) + " " + data);
      ppu.cpuWrite(address & 0x0007, data);
    } else if (address >= 0x4016 && address <= 0x4017) {
      controllerState[address & 0x0001] = controller[address & 0x0001];
    }
  }

  public int read(int address, boolean readOnly) {
    int data = -3;

    int romData = rom.cpuRead(address, readOnly);
    if (romData != -2) {
      //System.out.println("Reading romData: " + romData);
      return romData;
    }
    if (address >= 0x0000 && address <= 0x1FFF) {
      //System.out.println("Reading ram: " + ram[address & 0x07FF]);
      data = ram[address & 0x07FF];
      //System.out.println("Reading ram 2: " + data);
      return data;
    } else if (address >= 0x2000 && address <= 0x3FFF) {
      data = ppu.cpuRead(address & 0x0007, readOnly);
      //System.out.println("Reading ram 3: " + data + " Address: " + (address & 0x0007));
      return data;
    } else if (address >= 0x4016 && address <= 0x4017) {
      data = (controllerState[address & 0x0001] & 0x80) > 0 ? 1 : 0;
      controllerState[address & 0x0001] <<= 1;
      //System.out.println("Reading ram 4: " + data);
      return data;
    }
    return 0x00;
  }

  public Bus() {
    Arrays.fill(ram, 0x00);
    cpu = CPU.instance;
    cpu.connectBus(this);
    ppu = PPU.instance;
  }

  public void addROM(ROM rom) {
      this.rom = rom;
      ppu.addROM(rom);
  }

  public void reset() {
    cpu.reset();
    ppu.reset();
    clockCounter = 0;
  }

  public void clock() {
    ppu.clock();
    if (clockCounter % 3 == 0) {
      try {
        cpu.clockCycle();
        //System.out.println("CPU CYCLE");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    if (ppu.getNonMaskableInterrupt()) {
      ppu.setNonMaskableInterrupt(false);
      cpu.nonMaskableInterruptRequest();
    }

    clockCounter++;
  }


}
