package com.carlosh.nesemulator.mappers;

/**
 * TODO : Mapper 002: UNROM.
 */
public class Mapper002 implements Mapper {

  @Override
  public int[] cpuRead(int address) {
    return new int[0];
  }

  @Override
  public int cpuWrite(int address, int data) {
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

  @Override
  public MirroringMode getMirroringMode() {
    return null;
  }
}
