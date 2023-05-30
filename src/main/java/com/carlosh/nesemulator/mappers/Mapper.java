package com.carlosh.nesemulator.mappers;

public interface Mapper {

  /**
   * Reads and returns the value at the mapped address which is determined by the specific mapper.
   * If the mapper does not map the address, it returns -2.
   *
   * @param address The address to be mapped.
   * @return The value at the mapped address or -2 if not mapped.
   */
  int cpuRead(int address);

  /**
   * Writes the value of the data argument into mapped address which is determined by the specific
   * mapper and returns 0. If the mapper does not map the address, it returns -2.
   *
   * @param address The address to be mapped.
   * @param data The data to be written.
   */
  int cpuWrite(int address, int data);
  /**
   * Reads and returns the value at the mapped address which is determined by the specific mapper.
   * If the mapper does not map the address, it returns -2.
   *
   * @param address The address to be mapped.
   * @return The value at the mapped address or -2 if not mapped.
   */
  int ppuRead(int address);

  /**
   * Writes the value of the data argument into mapped address which is determined by the specific
   * mapper and returns 0. If the mapper does not map the address, it returns -2.
   *
   * @param address The address to be mapped.
   * @param data The data to be written.
   */
  int ppuWrite(int address, int data);

  /**
   * Returns the current mirroring mode of the ROM as determined by the specific mapper.
   *
   * @return The mirroring mode.
   */
  MirroringMode getMirroringMode();

  /**
   * Update current scanline if handled by the mapper. Only used for some mappers.
   */
  void clockScanCounter();

  /**
   * Sets the interrupt request variable of the current mapper to the specified state. Only used
   * for some mappers.
   *
   * @param state The state to be set.
   */
  void setIrq(boolean state);

  /**
   * Returns the interrupt request variable of the current mapper. Only used for some mappers.
   *
   * @return Current state of irq.
   */
  boolean getIrq();
}
