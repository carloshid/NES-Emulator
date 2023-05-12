package com.carlosh.nesemulator;

import com.carlosh.nesemulator.ui.ScreenNES;
import java.util.Arrays;

public class Bus {
  public static Bus bus;

  private PPU ppu;
  private CPU cpu;
  private ROM rom;
  private int clockCounter = 0;
  public int[] controller = new int[2];
  private int[] controllerState = new int[2];
  int[] ram = new int[2048];
  private int directAddress = 0;
  private int directAddressData = 0;
  boolean dma = false;
  boolean dmaWaiting = true;

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
    } else if (address == 0x4014) {
      directAddress = data << 8;
      dma = true;
    } else if (address >= 0x4016 && address <= 0x4017) {
      //System.out.println("BBBB");
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
      //System.out.println("AAAAAA");
      data = (controllerState[address & 0x0001] & 0x80) > 0 ? 1 : 0;
      controllerState[address & 0x0001] <<= 1;
      //System.out.println("Reading controller: " + data);
      return data;
    }
    return 0x00;
  }

  public Bus() {
    Arrays.fill(ram, 0x00);
    cpu = CPU.instance;
    cpu.connectBus(this);
    ppu = PPU.instance;
    Bus.bus = this;
    controller[0] = 0;
    controller[1] = 0;
    controllerState[0] = 0;
    controllerState[1] = 0;
  }

  public void addROM(ROM rom) {
      this.rom = rom;
      ppu.addROM(rom);
  }

  public void reset() {
    cpu.reset();
    ppu.reset();
    directAddress = 0;
    directAddressData = 0;
    dma = false;
    dmaWaiting = true;
    clockCounter = 0;
  }

  public void clock() {
    ppu.clock();
    if (clockCounter % 3 == 0) {
      // Normal CPU clock
      if (!dma) {
        try {
          cpu.clockCycle();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      // Waiting for dma
      else if (dmaWaiting) {
        dmaWaiting = clockCounter % 2 == 0;
      }
      // DMA
      else {
        if (clockCounter % 2 == 1) {
          ppu.writeToOam(directAddress & 0xFF, directAddressData);
          if ((directAddress & 0xFF) == 0xFF) {
            directAddress = directAddress & 0xFF00;
          } else {
            directAddress++;
          }
          if ((directAddress & 0xFF) == 0) {
            dma = false;
            dmaWaiting = true;
          }
        } else {
          directAddressData = read(directAddress, false);
        }
      }
    }

    if (ppu.getNonMaskableInterrupt()) {
      ppu.setNonMaskableInterrupt(false);
      cpu.nonMaskableInterruptRequest();
    }

    clockCounter++;
  }


}
