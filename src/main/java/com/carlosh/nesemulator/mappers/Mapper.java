package com.carlosh.nesemulator.mappers;

public interface Mapper {
  /**
   * Returns 2 integers. The first integer represents the mapped address. The second integer
   * represents the data. The mapper either returns the data or the mapped address. If the mapped
   * address is returned, the value of the second integer is ignored. If the data is returned, the
   * value of the first integer is -1. If the mapper does not handle the address, the value of the
   * first integer is -2.
   *
   * @param address The address to be mapped.
   * @return An array of 2 integers.
   */
  int[] cpuRead(int address);
  int cpuWrite(int address);
  int ppuRead(int address);
  int ppuWrite(int address);
}
