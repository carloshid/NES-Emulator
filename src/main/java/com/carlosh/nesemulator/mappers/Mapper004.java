package com.carlosh.nesemulator.mappers;

/**
 * TODO : Mapper 004: MMC3.
 */
public class Mapper004 implements Mapper {

  @Override
  public int[] cpuRead(int address) {
    return new int[0];
  }

  @Override
  public int cpuWrite(int address) {
    return 0;
  }

  @Override
  public int ppuRead(int address) {
    return 0;
  }

  @Override
  public int ppuWrite(int address) {
    return 0;
  }
}
