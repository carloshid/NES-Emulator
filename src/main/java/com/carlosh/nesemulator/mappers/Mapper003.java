package com.carlosh.nesemulator.mappers;

/**
 * TODO : Mapper 003: CxROM.
 */
public class Mapper003 implements Mapper {

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
