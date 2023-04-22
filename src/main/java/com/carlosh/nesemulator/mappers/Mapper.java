package com.carlosh.nesemulator.mappers;

public interface Mapper {
  int cpuRead(int address);
  int cpuWrite(int address);
  int ppuRead(int address);
  int ppuWrite(int address);
}
