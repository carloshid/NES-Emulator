package com.carlosh.nesemulator;

import java.util.concurrent.Callable;

public class CPU {

  /**
   * The CPU is a singleton.
   */
  public static final CPU instance = new CPU();

  private CPU() {
  }

  private Bus bus;

  /**
   * Connect the CPU to the bus.
   *
   * @param bus The bus to connect to.
   */
  public void connectBus(Bus bus) {
    this.bus = bus;
  }

  /**
   * Read from the bus.
   *
   * @param address The address to read from.
   * @return The data read from the bus.
   */
  public int read(int address) {
    return bus.read(address, false);
  }

    /**
     * Write to the bus.
     *
     * @param address The address to write to.
     * @param data The data to write to the bus.
     */
  public void write(int address, int data) {
    bus.write(address, data);
  }

  /**
   * Status register flags, each one is a bit.
   */
  public enum StatusFlag {
    C(0), // Carry
    Z(1), // Zero
    I(2), // Interrupt
    D(3), // Decimal
    B(4), // Break
    U(5), // Unused
    V(6), // Overflow
    N(7); // Negative

    private final int bit;

    /**
     * Create a new status flag.
     *
     * @param bit The bit of the flag.
     */
    StatusFlag(int bit) {
      this.bit = bit;
    }

    /**
     * Get the bit of the flag.
     *
     * @return The bit of the flag.
     */
    public int getBit() {
      return bit;
    }
  }

  int status = 0x00;  // Status register
  int a = 0x00;  // Accumulator register
  int x = 0x00;  // X register
  int y = 0x00;  // Y register
  int stkPtr = 0x00;  // Stack pointer
  int pc = 0x0000;  // Program counter

  private int getStatusFlag(StatusFlag flag) {
    return (status >> flag.bit) & 1;
  }

  private void setStatusFlag(StatusFlag flag, int value) {
    if (value == 0) {
      status &= ~(1 << flag.bit);
    } else {
      status |= (1 << flag.bit);
    }
  }

  // Addressing modes
  public int IMP() {
    fetched = a;
    return 0;
  }

  public int IMM() {
    address_abs = pc++;
    return 0;
  }

  public int ZP0() {
    address_abs = read(pc++);
    address_abs &= 0x00FF;
    return 0;
  }

  public int ZPX() {
    address_abs = (read(pc++) + x);
    address_abs &= 0x00FF;
    return 0;
  }

  public int ZPY() {
    address_abs = (read(pc++) + y);
    address_abs &= 0x00FF;
    return 0;
  }

  public int REL() {
    address_rel = read(pc++);
    if ((address_rel & 0x80) != 0) {
      address_rel |= 0xFF00;
    }
    return 0;
  }

  public int ABS() {
    int low = read(pc++);
    int high = read(pc++);
    address_abs = (high << 8) | low;
    return 0;
  }

  public int ABX() {
    int low = read(pc++);
    int high = read(pc++);
    address_abs = (high << 8) | low;
    address_abs += x;
    if ((address_abs & 0xFF00) != (high << 8)) {
      return 1;
    }
    return 0;
  }

  public int ABY() {
    int low = read(pc++);
    int high = read(pc++);
    address_abs = (high << 8) | low;
    address_abs += y;
    if ((address_abs & 0xFF00) != (high << 8)) {
      return 1;
    }
    return 0;
  }

  public int IND() {
    int ptr_low = read(pc++);
    int ptr_high = read(pc++);
    int ptr = (ptr_high << 8) | ptr_low;

    // Bug in original hardware, keep as it was
    if (ptr_low == 0x00FF) {
      address_abs = (read(ptr & 0xFF00) << 8) | read(ptr);
    } else {
      address_abs = (read(ptr + 1) << 8) | read(ptr);
    }
    return 0;
  }

  public int IZX() {
    int t = read(pc++);
    int low = read((t + x) & 0x00FF);
    int high = read((t + x + 1) & 0x00FF);
    address_abs = (high << 8) | low;
    return 0;
  }

  public int IZY() {
    int t = read(pc++);
    int low = read(t & 0x00FF);
    int high = read((t + 1) & 0x00FF);
    address_abs = (high << 8) | low;
    address_abs += y;
    if ((address_abs & 0xFF00) != (high << 8)) {
      return 1;
    }
    return 0;
  }

  // Opcodes

  /**
   * Addition.
   *
   * @return 1
   */
  public int ADC() {
    int val = fetch();
    // Call the add helper method with the fetched value.
    add(val);
    return 1;
  }

  private void add(int val) {
    int added = a + val + getStatusFlag(StatusFlag.C);
    // Set carry flag if result is greater than 255
    setStatusFlag(StatusFlag.C, added > 255 ? 1 : 0);
    // Set zero flag if the first 8 bits are 0
    setStatusFlag(StatusFlag.Z, (added & 0x00FF) == 0 ? 1 : 0);
    // Set negative flag if the first bit is 1
    setStatusFlag(StatusFlag.N, added & 0x80);
    // Set overflow flag if the sign of a and val are the same and the sign of a and added are different
    boolean overflow = (((a ^ added) & 0x80) != 0) && (((a ^ val) & 0x80) == 0);
    setStatusFlag(StatusFlag.V, overflow ? 1 : 0);
    a = added & 0x00FF;
  }

  /**
   * Bitwise AND.
   *
   * @return 1
   */
  public int AND() {
    fetch();
    a = a & fetched;
    setStatusFlag(StatusFlag.Z, a == 0x00 ? 1 : 0);
    setStatusFlag(StatusFlag.N, a & 0x80);
    return 1;
  }

  /**
   * Arithmetic shift left.
   *
   * @return 0
   */
  public int ASL() {
    int value = fetch() << 1;
    setStatusFlag(StatusFlag.C, (value & 0xFF00) > 0 ? 1 : 0);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & 0x80);
    if (lookup[opcode].addressMode == (Callable) this::IMP) {
      a = value & 0x00FF;
    } else {
      write(address_abs, value & 0x00FF);
    }
    return 0;
  }

  /**
   * Branch if carry clear.
   *
   * @return 0
   */
  public int BCC() {
    if (getStatusFlag(StatusFlag.C) == 0) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Branch if carry set.
   *
   * @return 0
   */
  public int BCS() {
    if (getStatusFlag(StatusFlag.C) == 1) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Branch if equal.
   *
   * @return 0
   */
  public int BEQ() {
    if (getStatusFlag(StatusFlag.Z) == 1) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Bit test.
   *
   * @return 0.
   */
  public int BIT() {
    int value = a & fetch();
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    setStatusFlag(StatusFlag.V, value & (1 << 6));
    return 0;
  }

  /**
   * Branch if negative.
   *
   * @return 0
   */
  public int BMI() {
    if (getStatusFlag(StatusFlag.N) == 1) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Branch if not equal.
   *
   * @return 0
   */
  public int BNE() {
    if (getStatusFlag(StatusFlag.Z) == 0) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Branch if positive.
   *
   * @return 0
   */
  public int BPL() {
    if (getStatusFlag(StatusFlag.N) == 0) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Break / Interrupt.
   *
   * @return 0
   */
  public int BRK() {
    setStatusFlag(StatusFlag.I, 1);
    write(0x0100 + stkPtr--, (++pc >> 8) & 0x00FF);
    write(0x0100 + stkPtr--, pc & 0x00FF);
    setStatusFlag(StatusFlag.B, 1);
    write(0x0100 + stkPtr--, status);
    setStatusFlag(StatusFlag.B, 0);
    pc = (read(0xFFFE) | (read(0xFFFF) << 8));
    return 0;
  }

  /**
   * Branch if not overflowed.
   *
   * @return 0
   */
  public int BVC() {
    if (getStatusFlag(StatusFlag.V) == 0) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Branch if overflowed.
   *
   * @return 0
   */
  public int BVS() {
    if (getStatusFlag(StatusFlag.V) == 1) {
      cycles++;
      address_abs = pc + address_rel;
      if ((pc & 0xFF00) != (address_abs & 0xFF00)) {
        cycles++;
      }
      pc = address_abs;
    }
    return 0;
  }

  /**
   * Clear the carry bit.
   *
   * @return 0
   */
  public int CLC() {
    setStatusFlag(StatusFlag.C, 0);
    return 0;
  }

  /**
   * Clear the decimal bit.
   *
   * @return 0
   */
  public int CLD() {
    setStatusFlag(StatusFlag.D, 0);
    return 0;
  }

  /**
   * Clear the interrupts bit.
   *
   * @return 0
   */
  public int CLI() {
    setStatusFlag(StatusFlag.I, 0);
    return 0;
  }

  /**
   * Clear the overflow bit.
   *
   * @return 0
   */
  public int CLV() {
    setStatusFlag(StatusFlag.V, 0);
    return 0;
  }

  /**
   * Compare with accumulator.
   *
   * @return 1
   */
  public int CMP() {
    int value = a - fetch();
    setStatusFlag(StatusFlag.C, a >= value ? 1 : 0);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    return 1;
  }

  /**
   * Compare with X register.
   *
   * @return 0
   */
  public int CPX() {
    int value = x - fetch();
    setStatusFlag(StatusFlag.C, x >= value ? 1 : 0);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    return 0;
  }

  /**
   * Compare with Y register.
   *
   * @return 0
   */
  public int CPY() {
    int value = y - fetch();
    setStatusFlag(StatusFlag.C, y >= value ? 1 : 0);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    return 0;
  }

  /**
   * Decrement.
   *
   * @return 0
   */
  public int DEC() {
    int value = fetch() - 1;
    write(address_abs, value & 0x00FF);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    return 0;
  }

  /**
   * Decrement X.
   *
   * @return 0
   */
  public int DEX() {
    x--;
    setStatusFlag(StatusFlag.Z, x == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, x & (1 << 7));
    return 0;
  }

  /**
   * Decrement Y.
   *
   * @return 0
   */
  public int DEY() {
    y--;
    setStatusFlag(StatusFlag.Z, y == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, y & (1 << 7));
    return 0;
  }

  /**
   * Exclusive OR (with accumulator).
   *
   * @return 1
   */
  public int EOR() {
    a = a ^ fetch();
    setStatusFlag(StatusFlag.Z, a == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, a & (1 << 7));
    return 1;
  }

  /**
   * Increment.
   *
   * @return 0
   */
  public int INC() {
    int value = fetch() + 1;
    write(address_abs, value & 0x00FF);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    return 0;
  }

  /**
   * Increment X.
   *
   * @return 0
   */
  public int INX() {
    x++;
    setStatusFlag(StatusFlag.Z, x == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, x & (1 << 7));
    return 0;
  }

  /**
   * Increment Y.
   *
   * @return 0
   */
  public int INY() {
    y++;
    setStatusFlag(StatusFlag.Z, y == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, y & (1 << 7));
    return 0;
  }

  /**
   * Jump.
   *
   * @return 0
   */
  public int JMP() {
    pc = address_abs;
    return 0;
  }

  /**
   * Jump subroutine.
   *
   * @return 0
   */
  public int JSR() {
    write(0x0100 + stkPtr--, (--pc >> 8) & 0x00FF);
    write(0x0100 + stkPtr--, pc & 0x00FF);
    pc = address_abs;
    return 0;
  }

  /**
   * Load accumulator.
   *
   * @return 1
   */
  public int LDA() {
    a = fetch();
    setStatusFlag(StatusFlag.Z, a == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, a & (1 << 7));
    return 1;
  }

  /**
   * Load X.
   *
   * @return 1
   */
  public int LDX() {
    x = fetch();
    setStatusFlag(StatusFlag.Z, x == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, x & (1 << 7));
    return 1;
  }

  /**
   * Load Y.
   *
   * @return 1
   */
  public int LDY() {
    y = fetch();
    setStatusFlag(StatusFlag.Z, y == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, y & (1 << 7));
    return 1;
  }

  /**
   * Logical shift right.
   *
   * @return 0
   */
  public int LSR() {
    int value = fetch() >> 1;
    setStatusFlag(StatusFlag.C, (value & 0x0001) != 0 ? 1 : 0);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    if (lookup[opcode].addressMode == (Callable) this::IMP) {
      a = value & 0x00FF;
    } else {
      write(address_abs, value & 0x00FF);
    }
    return 0;
  }

  /**
   * No operation.
   *
   * @return 0 or 1 depending on the current opcode
   */
  public int NOP() {
    if (opcode == 0x1C || opcode == 0x3C || opcode == 0x5C || opcode == 0x7C || opcode == 0xDC || opcode == 0xFC) {
      return 1;
    }
    return 0;
  }

  /**
   * OR with accumulator.
   *
   * @return 1
   */
  public int ORA() {
    a = a | fetch();
    setStatusFlag(StatusFlag.Z, a == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, a & (1 << 7));
    return 1;
  }

  /**
   * Push the accumulator to the stack.
   *
   * @return 0
   */
  public int PHA() {
    write((0x0100 + stkPtr--), a);
    return 0;
  }

  /**
   * Push processor status to the stack.
   *
   * @return 0
   */
  public int PHP() {
    write((0x0100 + stkPtr--), status | (1 << StatusFlag.B.ordinal()) | (1 << StatusFlag.U.ordinal()));
    setStatusFlag(StatusFlag.B, 0);
    setStatusFlag(StatusFlag.U, 0);
    return 0;
  }

  /**
   * Pop the accumulator from the stack.
   *
   * @return 0
   */
  public int PLA() {
    a = read(0x0100 + ++stkPtr);
    setStatusFlag(StatusFlag.Z, a == 0x00 ? 1 : 0);
    setStatusFlag(StatusFlag.N, (a & 0x80) != 0 ? 1 : 0);
    return 0;
  }

  /**
   * Pop processor status from the stack.
   *
   * @return 0
   */
  public int PLP() {
    status = read(0x0100 + ++stkPtr);
    setStatusFlag(StatusFlag.U, 1);
    return 0;
  }

  /**
   * Rotate left.
   *
   * @return 0
   */
  public int ROL() {
    int value = (fetch() << 1) | getStatusFlag(StatusFlag.C);
    setStatusFlag(StatusFlag.C, (value & 0xFF00) != 0 ? 1 : 0);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    if (lookup[opcode].addressMode == (Callable) this::IMP) {
      a = value & 0x00FF;
    } else {
      write(address_abs, value & 0x00FF);
    }
    return 0;
  }

  /**
   * Rotate right.
   *
   * @return 0
   */
  public int ROR() {
    int value = (fetch() >> 1) | (getStatusFlag(StatusFlag.C) << 7);
    setStatusFlag(StatusFlag.C, (value & 0x0001) != 0 ? 1 : 0);
    setStatusFlag(StatusFlag.Z, (value & 0x00FF) == 0 ? 1 : 0);
    setStatusFlag(StatusFlag.N, value & (1 << 7));
    if (lookup[opcode].addressMode == (Callable) this::IMP) {
      a = value & 0x00FF;
    } else {
      write(address_abs, value & 0x00FF);
    }
    return 0;
  }

  /**
   * Return from interrupt.
   *
   * @return 0
   */
  public int RTI() {
    status = read(0x0100 + ++stkPtr);
    status &= ~(1 << StatusFlag.B.ordinal());
    status &= ~(1 << StatusFlag.U.ordinal());
    pc = read(0x0100 + ++stkPtr);
    pc |= (read(0x0100 + ++stkPtr) << 8);
    return 0;
  }

  /**
   * Return from subroutine.
   *
   * @return 0
   */
  public int RTS() {
    pc = read(0x0100 + ++stkPtr);
    pc |= (read(0x0100 + ++stkPtr) << 8);
    pc++;
    return 0;
  }

  /**
   * Subtraction.
   *
   * @return 1
   */
  public int SBC() {
    int val = fetch() ^ 0x00FF;
    // Call the add helper method with the inverse of the fetched value.
    add(val);
    return 1;
  }

  public int SEC() {
    return 0;
  }

  public int SED() {
    return 0;
  }

  public int SEI() {
    return 0;
  }

  public int STA() {
    return 0;
  }

  public int STX() {
    return 0;
  }

  public int STY() {
    return 0;
  }

  public int TAX() {
    return 0;
  }

  public int TAY() {
    return 0;
  }

  public int TSX() {
    return 0;
  }

  public int TXA() {
    return 0;
  }

  public int TXS() {
    return 0;
  }

  public int TYA() {
    return 0;
  }

  public int XXX() {
    return 0;
  }

  /**
   * Perform one clock cycle.
   *
   * @throws Exception if an error occurs when calling the address mode or opcode methods
   */
  public void clockCycle() throws Exception {
    if (cycles == 0) {
      opcode = read(pc++);

      cycles = lookup[opcode].cycles;

      int additionalCycles1 = (int) lookup[opcode].addressMode.call();
      int additionalCycles2 = (int) lookup[opcode].opcode.call();

      cycles += (additionalCycles1 & additionalCycles2);
    }
    cycles--;
  }

  /**
   * Reset the CPU.
   */
  public void reset() {
    a = 0x00;
    x = 0x00;
    y = 0x00;
    stkPtr = 0xFD;
    status = 0x00;
    pc = (read(0xFFFD) << 8) | read(0xFFFC);
    address_abs = 0x0000;
    address_rel = 0x00;
    fetched = 0x00;
    cycles = 8;
  }

  /**
   * Interrupt request received. Only execute if the 'ignore interrupts' bit is not set.
   */
  public void interruptRequest() {
    if (getStatusFlag(StatusFlag.I) == 0) {
      write((0x0100 + stkPtr--), (pc >> 8) & 0x00FF);
      write((0x0100 + stkPtr--), pc & 0x00FF);
      setStatusFlag(StatusFlag.B, 0);
      setStatusFlag(StatusFlag.U, 1);
      setStatusFlag(StatusFlag.I, 1);
      write((0x0100 + stkPtr--), status);
      pc = (read(0xFFFF) << 8) | read(0xFFFE);
      cycles = 7;
    }
  }

  /**
   * Non-maskable interrupt request received. Execute regardless of the 'ignore interrupts' bit.
   */
  public void nonMaskableInterruptRequest() {
    write((0x0100 + stkPtr--), (pc >> 8) & 0x00FF);
    write((0x0100 + stkPtr--), pc & 0x00FF);
    setStatusFlag(StatusFlag.B, 0);
    setStatusFlag(StatusFlag.U, 1);
    setStatusFlag(StatusFlag.I, 1);
    write((0x0100 + stkPtr--), status);
    pc = (read(0xFFFB) << 8) | read(0xFFFA);
    cycles = 8;
  }

  /**
   * Value to store the fetched value.
   */
  public int fetched = 0x00;

  /**
   * Fetch the value from the address pointed to by the absolute address variable, unless the
   * address mode is IMP (implied).
   *
   * @return the fetched value
   */
  public int fetch() {
    if (!(lookup[opcode].addressMode == (Callable) this::IMP)) {
      fetched = read(address_abs);
    }
    return fetched;
  }

  public int address_abs = 0x0000;  // Absolute address
  public int address_rel = 0x00;  // Relative address
  public int opcode = 0x00; // Current opcode
  public int cycles = 0;  // Remaining cycles for the current instruction

  /**
   * A CPU instruction. Each instruction has a name, an opcode, an address mode, and a number of
   * cycles required to complete it. The opcode and address mode are methods of the CPU class which
   * are cast to the Callable interface. The name is a string with the name of the opcode.
   */
  public class Instruction {
    public String name;
    public Callable opcode;
    public Callable addressMode;
    public int cycles = 0;

    public Instruction(String name, Callable opcode, Callable addressMode, int cycles) {
      this.name = name;
      this.opcode = opcode;
      this.addressMode = addressMode;
      this.cycles = cycles;
    }
  }

  /**
   * Lookup table for the CPU instructions. Represents a 16x16 table of instructions. Some
   * entries in the table do not contain an instruction and are null. The lookup table is
   * initialized in the CPU constructor.
   */
  public Instruction[] lookup = new Instruction[256];

  private void initializeInstructions() {
    lookup[0x00] = new Instruction("BRK", this::BRK, this::IMP, 7);
    lookup[0x01] = new Instruction("ORA", this::ORA, this::IZX, 6);
    lookup[0x05] = new Instruction("ORA", this::ORA, this::ZP0, 3);
    lookup[0x06] = new Instruction("ASL", this::ASL, this::ZP0, 5);
    lookup[0x08] = new Instruction("PHP", this::PHP, this::IMP, 3);
    lookup[0x09] = new Instruction("ORA", this::ORA, this::IMM, 2);
    lookup[0x0A] = new Instruction("ASL", this::ASL, this::IMP, 2);
    lookup[0x0D] = new Instruction("ORA", this::ORA, this::ABS, 4);
    lookup[0x0E] = new Instruction("ASL", this::ASL, this::ABS, 6);
    lookup[0x10] = new Instruction("BPL", this::BPL, this::REL, 2);
    lookup[0x11] = new Instruction("ORA", this::ORA, this::IZY, 5);
    lookup[0x15] = new Instruction("ORA", this::ORA, this::ZPX, 4);
    lookup[0x16] = new Instruction("ASL", this::ASL, this::ZPX, 6);
    lookup[0x18] = new Instruction("CLC", this::CLC, this::IMP, 2);
    lookup[0x19] = new Instruction("ORA", this::ORA, this::ABY, 4);
    lookup[0x1D] = new Instruction("ORA", this::ORA, this::ABX, 4);
    lookup[0x1E] = new Instruction("ASL", this::ASL, this::ABX, 7);
    lookup[0x20] = new Instruction("JSR", this::JSR, this::ABS, 6);
    lookup[0x21] = new Instruction("AND", this::AND, this::IZX, 6);
    lookup[0x24] = new Instruction("BIT", this::BIT, this::ZP0, 3);
    lookup[0x25] = new Instruction("AND", this::AND, this::ZP0, 3);
    lookup[0x26] = new Instruction("ROL", this::ROL, this::ZP0, 5);
    lookup[0x28] = new Instruction("PLP", this::PLP, this::IMP, 4);
    lookup[0x29] = new Instruction("AND", this::AND, this::IMM, 2);
    lookup[0x2A] = new Instruction("ROL", this::ROL, this::IMP, 2);
    lookup[0x2C] = new Instruction("BIT", this::BIT, this::ABS, 4);
    lookup[0x2D] = new Instruction("AND", this::AND, this::ABS, 4);
    lookup[0x2E] = new Instruction("ROL", this::ROL, this::ABS, 6);
    lookup[0x30] = new Instruction("BMI", this::BMI, this::REL, 2);
    lookup[0x31] = new Instruction("AND", this::AND, this::IZY, 5);
    lookup[0x35] = new Instruction("AND", this::AND, this::ZPX, 4);
    lookup[0x36] = new Instruction("ROL", this::ROL, this::ZPX, 6);
    lookup[0x38] = new Instruction("SEC", this::SEC, this::IMP, 2);
    lookup[0x39] = new Instruction("AND", this::AND, this::ABY, 4);
    lookup[0x3D] = new Instruction("AND", this::AND, this::ABX, 4);
    lookup[0x3E] = new Instruction("ROL", this::ROL, this::ABX, 7);
    lookup[0x40] = new Instruction("RTI", this::RTI, this::IMP, 6);
    lookup[0x41] = new Instruction("EOR", this::EOR, this::IZX, 6);
    lookup[0x45] = new Instruction("EOR", this::EOR, this::ZP0, 3);
    lookup[0x46] = new Instruction("LSR", this::LSR, this::ZP0, 5);
    lookup[0x48] = new Instruction("PHA", this::PHA, this::IMP, 3);
    lookup[0x49] = new Instruction("EOR", this::EOR, this::IMM, 2);
    lookup[0x4A] = new Instruction("LSR", this::LSR, this::IMP, 2);
    lookup[0x4C] = new Instruction("JMP", this::JMP, this::ABS, 3);
    lookup[0x4D] = new Instruction("EOR", this::EOR, this::ABS, 4);
    lookup[0x4E] = new Instruction("LSR", this::LSR, this::ABS, 6);
    lookup[0x50] = new Instruction("BVC", this::BVC, this::REL, 2);
    lookup[0x51] = new Instruction("EOR", this::EOR, this::IZY, 5);
    lookup[0x55] = new Instruction("EOR", this::EOR, this::ZPX, 4);
    lookup[0x56] = new Instruction("LSR", this::LSR, this::ZPX, 6);
    lookup[0x58] = new Instruction("CLI", this::CLI, this::IMP, 2);
    lookup[0x59] = new Instruction("EOR", this::EOR, this::ABY, 4);
    lookup[0x5D] = new Instruction("EOR", this::EOR, this::ABX, 4);
    lookup[0x5E] = new Instruction("LSR", this::LSR, this::ABX, 7);
    lookup[0x60] = new Instruction("RTS", this::RTS, this::IMP, 6);
    lookup[0x61] = new Instruction("ADC", this::ADC, this::IZX, 6);
    lookup[0x65] = new Instruction("ADC", this::ADC, this::ZP0, 3);
    lookup[0x66] = new Instruction("ROR", this::ROR, this::ZP0, 5);
    lookup[0x68] = new Instruction("PLA", this::PLA, this::IMP, 4);
    lookup[0x69] = new Instruction("ADC", this::ADC, this::IMM, 2);
    lookup[0x6A] = new Instruction("ROR", this::ROR, this::IMP, 2);
    lookup[0x6C] = new Instruction("JMP", this::JMP, this::IND, 5);
    lookup[0x6D] = new Instruction("ADC", this::ADC, this::ABS, 4);
    lookup[0x6E] = new Instruction("ROR", this::ROR, this::ABS, 6);
    lookup[0x70] = new Instruction("BVS", this::BVS, this::REL, 2);
    lookup[0x71] = new Instruction("ADC", this::ADC, this::IZY, 5);
    lookup[0x75] = new Instruction("ADC", this::ADC, this::ZPX, 4);
    lookup[0x76] = new Instruction("ROR", this::ROR, this::ZPX, 6);
    lookup[0x78] = new Instruction("SEI", this::SEI, this::IMP, 2);
    lookup[0x79] = new Instruction("ADC", this::ADC, this::ABY, 4);
    lookup[0x7D] = new Instruction("ADC", this::ADC, this::ABX, 4);
    lookup[0x7E] = new Instruction("ROR", this::ROR, this::ABX, 7);
    lookup[0x81] = new Instruction("STA", this::STA, this::IZX, 6);
    lookup[0x84] = new Instruction("STY", this::STY, this::ZP0, 3);
    lookup[0x85] = new Instruction("STA", this::STA, this::ZP0, 3);
    lookup[0x86] = new Instruction("STX", this::STX, this::ZP0, 3);
    lookup[0x88] = new Instruction("DEY", this::DEY, this::IMP, 2);
    lookup[0x8A] = new Instruction("TXA", this::TXA, this::IMP, 2);
    lookup[0x8C] = new Instruction("STY", this::STY, this::ABS, 4);
    lookup[0x8D] = new Instruction("STA", this::STA, this::ABS, 4);
    lookup[0x8E] = new Instruction("STX", this::STX, this::ABS, 4);
    lookup[0x90] = new Instruction("BCC", this::BCC, this::REL, 2);
    lookup[0x91] = new Instruction("STA", this::STA, this::IZY, 6);
    lookup[0x94] = new Instruction("STY", this::STY, this::ZPX, 4);
    lookup[0x95] = new Instruction("STA", this::STA, this::ZPX, 4);
    lookup[0x96] = new Instruction("STX", this::STX, this::ZPY, 4);
    lookup[0x98] = new Instruction("TYA", this::TYA, this::IMP, 2);
    lookup[0x99] = new Instruction("STA", this::STA, this::ABY, 5);
    lookup[0x9A] = new Instruction("TXS", this::TXS, this::IMP, 2);
    lookup[0x9D] = new Instruction("STA", this::STA, this::ABX, 5);
    lookup[0xA0] = new Instruction("LDY", this::LDY, this::IMM, 2);
    lookup[0xA1] = new Instruction("LDA", this::LDA, this::IZX, 6);
    lookup[0xA2] = new Instruction("LDX", this::LDX, this::IMM, 2);
    lookup[0xA4] = new Instruction("LDY", this::LDY, this::ZP0, 3);
    lookup[0xA5] = new Instruction("LDA", this::LDA, this::ZP0, 3);
    lookup[0xA6] = new Instruction("LDX", this::LDX, this::ZP0, 3);
    lookup[0xA8] = new Instruction("TAY", this::TAY, this::IMP, 2);
    lookup[0xA9] = new Instruction("LDA", this::LDA, this::IMM, 2);
    lookup[0xAA] = new Instruction("TAX", this::TAX, this::IMP, 2);
    lookup[0xAC] = new Instruction("LDY", this::LDY, this::ABS, 4);
    lookup[0xAD] = new Instruction("LDA", this::LDA, this::ABS, 4);
    lookup[0xAE] = new Instruction("LDX", this::LDX, this::ABS, 4);
    lookup[0xB0] = new Instruction("BCS", this::BCS, this::REL, 2);
    lookup[0xB1] = new Instruction("LDA", this::LDA, this::IZY, 5);
    lookup[0xB4] = new Instruction("LDY", this::LDY, this::ZPX, 4);
    lookup[0xB5] = new Instruction("LDA", this::LDA, this::ZPX, 4);
    lookup[0xB6] = new Instruction("LDX", this::LDX, this::ZPY, 4);
    lookup[0xB8] = new Instruction("CLV", this::CLV, this::IMP, 2);
    lookup[0xB9] = new Instruction("LDA", this::LDA, this::ABY, 4);
    lookup[0xBA] = new Instruction("TSX", this::TSX, this::IMP, 2);
    lookup[0xBC] = new Instruction("LDY", this::LDY, this::ABX, 4);
    lookup[0xBD] = new Instruction("LDA", this::LDA, this::ABX, 4);
    lookup[0xBE] = new Instruction("LDX", this::LDX, this::ABY, 4);
    lookup[0xC0] = new Instruction("CPY", this::CPY, this::IMM, 2);
    lookup[0xC1] = new Instruction("CMP", this::CMP, this::IZX, 6);
    lookup[0xC4] = new Instruction("CPY", this::CPY, this::ZP0, 3);
    lookup[0xC5] = new Instruction("CMP", this::CMP, this::ZP0, 3);
    lookup[0xC6] = new Instruction("DEC", this::DEC, this::ZP0, 5);
    lookup[0xC8] = new Instruction("INY", this::INY, this::IMP, 2);
    lookup[0xC9] = new Instruction("CMP", this::CMP, this::IMM, 2);
    lookup[0xCA] = new Instruction("DEX", this::DEX, this::IMP, 2);
    lookup[0xCC] = new Instruction("CPY", this::CPY, this::ABS, 4);
    lookup[0xCD] = new Instruction("CMP", this::CMP, this::ABS, 4);
    lookup[0xCE] = new Instruction("DEC", this::DEC, this::ABS, 6);
    lookup[0xD0] = new Instruction("BNE", this::BNE, this::REL, 2);
    lookup[0xD1] = new Instruction("CMP", this::CMP, this::IZY, 5);
    lookup[0xD5] = new Instruction("CMP", this::CMP, this::ZPX, 4);
    lookup[0xD6] = new Instruction("DEC", this::DEC, this::ZPX, 6);
    lookup[0xD8] = new Instruction("CLD", this::CLD, this::IMP, 2);
    lookup[0xD9] = new Instruction("CMP", this::CMP, this::ABY, 4);
    lookup[0xDD] = new Instruction("CMP", this::CMP, this::ABX, 4);
    lookup[0xDE] = new Instruction("DEC", this::DEC, this::ABX, 7);
    lookup[0xE0] = new Instruction("CPX", this::CPX, this::IMM, 2);
    lookup[0xE1] = new Instruction("SBC", this::SBC, this::IZX, 6);
    lookup[0xE4] = new Instruction("CPX", this::CPX, this::ZP0, 3);
    lookup[0xE5] = new Instruction("SBC", this::SBC, this::ZP0, 3);
    lookup[0xE6] = new Instruction("INC", this::INC, this::ZP0, 5);
    lookup[0xE8] = new Instruction("INX", this::INX, this::IMP, 2);
    lookup[0xE9] = new Instruction("SBC", this::SBC, this::IMM, 2);
    lookup[0xEA] = new Instruction("NOP", this::NOP, this::IMP, 2);
    lookup[0xEC] = new Instruction("CPX", this::CPX, this::ABS, 4);
    lookup[0xED] = new Instruction("SBC", this::SBC, this::ABS, 4);
    lookup[0xEE] = new Instruction("INC", this::INC, this::ABS, 6);
    lookup[0xF0] = new Instruction("BEQ", this::BEQ, this::REL, 2);
    lookup[0xF1] = new Instruction("SBC", this::SBC, this::IZY, 5);
    lookup[0xF5] = new Instruction("SBC", this::SBC, this::ZPX, 4);
    lookup[0xF6] = new Instruction("INC", this::INC, this::ZPX, 6);
    lookup[0xF8] = new Instruction("SED", this::SED, this::IMP, 2);
    lookup[0xF9] = new Instruction("SBC", this::SBC, this::ABY, 4);
    lookup[0xFD] = new Instruction("SBC", this::SBC, this::ABX, 4);
    lookup[0xFE] = new Instruction("INC", this::INC, this::ABX, 7);
  }


}
