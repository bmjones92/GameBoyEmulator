package org.guide.gameboy.processor;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.Interrupt;
import org.guide.gameboy.processor.interrupts.InterruptController;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.gameboy.processor.opcode.OpcodeTable;
import org.guide.gameboy.processor.register.PointerRegister;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.FlagRegister;
import org.guide.util.AddressUtils;
import org.guide.util.BitUtils;

import java.nio.ByteBuffer;

import static java.util.Objects.requireNonNull;

/**
 * Implements the Game Boy's CPU. The processor is based on the Zilog Z80 CPU but has some differences. Detailed
 * documentation for the processor can be found <a href="http://marc.rawer.de/Gameboy/Docs/GBCPUman.pdf">here</a>.
 *
 * @author Brendan Jones
 */
public class Processor extends AddressSpace implements SerializableComponent {

    /**
     * The number of cycles that must elapse before the processor exits the "HALTED" state.
     */
    private static final int UNHALT_CYCLES = 12;

    /**
     * The interrupt controller.
     */
    private final InterruptController interrupts;

    /**
     * The "AF" register.
     */
    private final Register16 regAF;

    /**
     * The "BC" register.
     */
    private final Register16 regBC;

    /**
     * The "DE" register.
     */
    private final Register16 regDE;

    /**
     * The "HL" register.
     */
    private final Register16 regHL;

    /**
     * The stack pointer.
     */
    private final PointerRegister regSP;

    /**
     * The program counter.
     */
    private final PointerRegister regPC;

    /**
     * The flag register. Internally this wraps around the "F" register.
     */
    private final FlagRegister flags;

    /**
     * The opcode table containing the instruction set.
     */
    private final OpcodeTable opcodes;

    /**
     * The current status mode of the processor.
     */
    private CPUStatusMode status;

    /**
     * Whether the machine is running in CGB mode or DMG mode.
     */
    private boolean isCGB;

    /**
     * Whether the conditions for the HALT bug are met. In DMG hardware, the instruction following the HALT instruction
     * is skipped when interrupts are disabled.
     */
    private boolean hasHaltBug;

    /**
     * The number of remaining cycles before the machine can exit HALT mode.
     */
    private int unhaltCycles;

    /**
     * The current state of instruction processing.
     */
    private int currentInstructionState;

    /**
     * Creates a new Processor instance.
     *
     * @param memory The system's memory component.
     */
    public Processor(Memory memory) {
        super(memory);
        this.interrupts = new InterruptController(memory);

        this.regAF = new Register16("AF", memory, 0xFF, 0xF0);
        this.regBC = new Register16("BC", memory);
        this.regDE = new Register16("DE", memory);
        this.regHL = new Register16("HL", memory);

        this.regSP = new PointerRegister("SP", memory);
        this.regPC = new PointerRegister("PC", memory);

        this.flags = new FlagRegister(regAF.low());

        this.opcodes = new OpcodeTable(this, memory);

        memory.setAddressSpace(this, AddressUtils.KEY1);
        memory.setAddressSpace(this, AddressUtils.RP);
    }

    @Override
    public void serialize(ByteBuffer out) {
        out.putShort((short) regAF.read());
        out.putShort((short) regBC.read());
        out.putShort((short) regDE.read());
        out.putShort((short) regHL.read());
        out.putShort((short) regSP.read());
        out.putShort((short) regPC.read());

        out.put((byte) status.ordinal());
        out.put((byte) (hasHaltBug ? 1 : 0));
        out.put((byte) unhaltCycles);
        out.put((byte) currentInstructionState);

        interrupts.serialize(out);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        regAF.write(in.getShort());
        regBC.write(in.getShort());
        regDE.write(in.getShort());
        regHL.write(in.getShort());
        regSP.write(in.getShort());
        regPC.write(in.getShort());

        this.status = CPUStatusMode.values()[in.get() & 0xFF];
        this.hasHaltBug = (in.get() & 0xFF) == 1;
        this.unhaltCycles = in.get() & 0xFF;
        this.currentInstructionState = in.get() & 0xFF;

        interrupts.deserialize(in);
    }

    @Override
    public void write(int address, int value) {
        // The speed-switching register is onl available on CGB hardware.
        if (!isCGB && address == AddressUtils.KEY1) {
            return;
        }
        super.write(address, value);
    }

    @Override
    public int read(int address) {
        // The speed-switching register is onl available on CGB hardware.
        if (!isCGB && address == AddressUtils.KEY1) {
            return 0xFF;
        }
        return super.read(address);
    }

    /**
     * Resets the processor to its default state.
     *
     * @param isCGB Whether the machine is running in CGB or DMG mode.
     */
    public void reset(boolean isCGB) {
        this.isCGB = isCGB;
        if (isCGB) {
            regAF.write(0x1180);
            regBC.write(0x0000);
            regDE.write(0xFF56);
            regHL.write(0x000D);
        } else {
            regAF.write(0x1180);
            regBC.write(0x0000);
            regDE.write(0x0008);
            regHL.write(0x007C);
        }

        regSP.write(0xFFFE);
        regPC.write(0x0100);

        // Reset interrupts.
        interrupts.reset(isCGB);

        this.status = CPUStatusMode.RUNNING;
        this.currentInstructionState = 0;
    }

    /**
     * Ticks the processor a single time.
     *
     * @return The number of processor cycles that elapsed during the tick.
     */
    public int tick() {
        var elapsedCycles = 0;

        // The processor is currently halted.
        if (status == CPUStatusMode.HALTED) {
            elapsedCycles = 4;

            // When exiting HALT mode, there's a small delay before the hardware starts running again.
            if (unhaltCycles > 0) {
                unhaltCycles = Math.max(0, unhaltCycles - elapsedCycles);
                if (unhaltCycles == 0) {
                    status = CPUStatusMode.RUNNING;
                }
            }

            // HALT mode is exited when a system interrupt is received.
            if (status == CPUStatusMode.HALTED && unhaltCycles == 0 && interrupts.isInterruptPending()) {
                unhaltCycles = UNHALT_CYCLES;
            }
        }

        // Update the processor if it is currently running.
        if (status == CPUStatusMode.RUNNING) {
            // Handle the highest-priority pending interrupt if one exists.
            final var interrupt = interrupts.getPendingInterrupt();
            if (interrupt != null) {
                dispatchInterrupt(interrupt);
                elapsedCycles += 20;
            }

            // Read the Opcode part of the next instruction. If the HALT bug is present, then the program counter will
            // not increment.
            var opcode = memory.readUnsigned(hasHaltBug ? regPC.read() : regPC.readAndMove(1));
            this.hasHaltBug = false;

            // The $CB opcode indicates that the instruction is located on the extension table. Instructions located on
            // this table are identified with a 16-bit opcode ($CB##), but internally all instructions are tightly
            // packed, so we just set the 9th bit for the instructions.
            if (opcode == 0xCB) {
                opcode = 0x100 | memory.readUnsigned(regPC.readAndMove(1));
            }

            // Fetch and execute the instruction.
            final var instruction = opcodes.get(opcode);
            instruction.execute(this, memory, flags);

            // Update the elapsed cycles.
            elapsedCycles += instruction.getExecutionCycles();
        }

        // Tick the interrupt controller.
        interrupts.tick(elapsedCycles);

        return elapsedCycles;
    }

    /**
     * Pushes a value to the program stack.
     *
     * @param value The value to push.
     */
    public void pushToStack(int value) {
        memory.writeShort(regSP.moveAndRead(-2), value);
    }

    /**
     * Pops a value from the program stack.
     *
     * @return The value that was popped.
     */
    public int popFromStack() {
        return memory.readUnsignedShort(regSP.readAndMove(2));
    }

    /**
     * Prepares the hardware to handle the interrupt.
     *
     * @param interrupt The interrupt to handle.
     */
    private void dispatchInterrupt(Interrupt interrupt) {
        // Clear the interrupt on the interrupt controller.
        interrupts.setMasterEnable(false, 0);
        interrupts.setRequested(interrupt, false);

        // Move execution to the interrupt handler.
        pushToStack(regPC.read());
        regPC.write(interrupt.getAddress());
    }

    /**
     * Set the status mode for the processor.
     *
     * @param status The status mode.
     */
    public void setStatus(CPUStatusMode status) {
        switch (requireNonNull(status)) {
            case RUNNING -> this.status = status;
            case HALTED -> setStatusHalted();
            case STOPPED -> setStatusStopped();
        }
    }

    /**
     * Sets the status mode to halted.
     */
    private void setStatusHalted() {
        // There's a hardware bug that caused instructions to be skipped when the machine was halted under certain
        // circumstances, so we need to handle that here.
        if (interrupts.getMasterEnabled() || !interrupts.isInterruptPending()) {
            this.status = CPUStatusMode.HALTED;
        } else {
            // FIXME The bug might only occur on DMG hardware, so maybe change this to only
            //       be true if !isCGB? Need to test.
            this.hasHaltBug = true;
        }
    }

    /**
     * Sets the status mode to stopped. On CGB hardware, this is also responsible for switching the processor speed.
     */
    private void setStatusStopped() {
        var key1 = memory.getUnsigned(AddressUtils.KEY1);
        if (BitUtils.isSet(key1, 0)) { // Speed switch requested.
            key1 = BitUtils.isSet(key1, 7) ? 0x7E : 0xFE;
            memory.set(AddressUtils.KEY1, key1);
        } else {
            this.status = CPUStatusMode.STOPPED;
        }
    }

    /**
     * Get the processor's current status mode.
     *
     * @return The status mode.
     */
    public CPUStatusMode getStatus() {
        return status;
    }

    /**
     * Checks whether the processor is running in double speed mode.
     *
     * @return Whether the processor is running in double speed mode.
     */
    public boolean isDoubleSpeed() {
        final var key1 = memory.getUnsigned(AddressUtils.KEY1);
        return isCGB && BitUtils.isSet(key1, 7);
    }

    /**
     * Gets the "AF" register.
     *
     * @return The "AF" register.
     */
    public Register16 getAF() {
        return regAF;
    }

    /**
     * Gets the "BC" register.
     *
     * @return The "BC" register.
     */
    public Register16 getBC() {
        return regBC;
    }

    /**
     * Gets the "DE" register.
     *
     * @return The "DE" register.
     */
    public Register16 getDE() {
        return regDE;
    }

    /**
     * Gets the "HL" register.
     *
     * @return The "HL" register.
     */
    public Register16 getHL() {
        return regHL;
    }

    /**
     * Gets the "A" register.
     *
     * @return The "A" register.
     */
    public Register8 getA() {
        return regAF.high();
    }

    /**
     * Gets the "F" register.
     *
     * @return The "F" register.
     */
    public Register8 getF() {
        return regAF.low();
    }

    /**
     * Gets the "B" register.
     *
     * @return The "B" register.
     */
    public Register8 getB() {
        return regBC.high();
    }

    /**
     * Gets the "C" register.
     *
     * @return The "C" register.
     */
    public Register8 getC() {
        return regBC.low();
    }

    /**
     * Gets the "D" register.
     *
     * @return The "D" register.
     */
    public Register8 getD() {
        return regDE.high();
    }

    /**
     * Gets the "E" register.
     *
     * @return The "E" register.
     */
    public Register8 getE() {
        return regDE.low();
    }

    /**
     * Gets the "H" register.
     *
     * @return The "H" register.
     */
    public Register8 getH() {
        return regHL.high();
    }

    /**
     * Gets the "L" register.
     *
     * @return The "L" register.
     */
    public Register8 getL() {
        return regHL.low();
    }

    /**
     * Gets the stack pointer.
     *
     * @return The stack pointer.
     */
    public PointerRegister getSP() {
        return regSP;
    }

    /**
     * Gets the program counter.
     *
     * @return The program counter.
     */
    public PointerRegister getPC() {
        return regPC;
    }

    /**
     * Gets the flag register.
     *
     * @return The flag register.
     */
    public FlagRegister getFlags() {
        return flags;
    }

    /**
     * Gets the interrupt controller.
     *
     * @return The interrupt controller.
     */
    public InterruptController getInterrupts() {
        return interrupts;
    }

}
