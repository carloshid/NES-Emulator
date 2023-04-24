package com.carlosh.nesemulator;

public class Main {

  public static void main(String[] args) {
    ROM rom = new ROM("filename"); // Change to the filename of the rom file
    Bus bus = new Bus();
    bus.addROM(rom);
    bus.reset();
  }
}
