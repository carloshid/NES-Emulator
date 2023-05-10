package com.carlosh.nesemulator.mappers;

/**
 * TODO : Mapper 001: MMC1.
 */
public class Mapper001 implements Mapper {
  @Override
  public int cpuRead(int address) {
    return 0;
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
