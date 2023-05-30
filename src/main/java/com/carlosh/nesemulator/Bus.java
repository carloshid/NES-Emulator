package com.carlosh.nesemulator;

import java.util.Arrays;

/**
 * The bus class represents the NES system. References the different hardware components and stores
 * the system memory.
 */
public class Bus {
  public static Bus bus;

  private final PPU ppu;
  private final CPU cpu;
  private ROM rom;
  public long clockCounter = 0;
  public int[] controller = new int[2];
  private final int[] controllerState = new int[2];
  private final int[] ram = new int[2048];
  private int directAddress = 0;
  private int directAddressData = 0;
  private boolean dma = false;
  private boolean dmaWaiting = true;

  /**
   * Write some data to a specific memory address.
   *
   * @param address The address to write to.
   * @param data The data to write.
   */
  public void write (int address, int data) {
    if (address >= 0x4019) {
      rom.cpuWrite(address, data);
    }
    else if (address >= 0x0000 && address <= 0x1FFF) {
      ram[address & 0x07FF] = data;
    } else if (address >= 0x2000 && address <= 0x3FFF) {
      ppu.cpuWrite(address & 0x0007, data);
    } else if (address == 0x4014) {
      directAddress = data << 8;
      dma = true;
    } else if (address >= 0x4016 && address <= 0x4017) {
      controllerState[address & 0x0001] = controller[address & 0x0001];
    }
  }

  /**
   * Read the data stored at a specific memory address.
   *
   * @param address The address to read from.
   * @return The data stored at the address.
   */
  public int read(int address) {
    int data = -3;
    if (address >= 0x4019) {
      return rom.cpuRead(address);
    }
    else if (address >= 0x0000 && address <= 0x1FFF) {
      data = ram[address & 0x07FF];
      return data;
    } else if (address >= 0x2000 && address <= 0x3FFF) {
      data = ppu.cpuRead(address & 0x0007);
      return data;
    } else if (address >= 0x4016 && address <= 0x4017) {
      data = (controllerState[address & 0x0001] & 0x80) > 0 ? 1 : 0;
      controllerState[address & 0x0001] <<= 1;
      return data;
    }
    return 0x00;
  }

  /**
   * Constructor for the Bus class.
   */
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

  /**
   * Add a reference to the ROM currently loaded.
   *
   * @param rom The ROM to add.
   */
  public void addROM(ROM rom) {
      this.rom = rom;
      ppu.addROM(rom);
  }

  /**
   * Reset the whole system.
   */
  public void reset() {
    cpu.reset();
    ppu.reset();
    directAddress = 0;
    directAddressData = 0;
    dma = false;
    dmaWaiting = true;
    clockCounter = 0;
  }

  /**
   * Perform one clock cycle. The CPU performs one clock cycle every 3 PPU clock cycles.
   */
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
          directAddressData = read(directAddress);
        }
      }
    }

    if (ppu.getNonMaskableInterrupt()) {
      ppu.setNonMaskableInterrupt(false);
      cpu.nonMaskableInterruptRequest();
    }

    if (rom.getMapper().getIrq()) {
      rom.getMapper().setIrq(false);
      cpu.interruptRequest();
    }

    clockCounter++;
  }

}
