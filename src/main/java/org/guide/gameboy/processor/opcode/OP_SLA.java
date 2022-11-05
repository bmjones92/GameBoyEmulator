package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "SLA" instruction. This instruction shifts a target's bits to the left. The most-significant bit is
 * pushed into the carry flag, and the least-significant bit is set to 0.
 *
 * @author Brendan Jones
 */
public class OP_SLA extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Creates a new SLA instruction.
     *
     * @param target The target binding.
     * @param cycles How many cycles the instruction takes to complete.
     */
    private OP_SLA(ReadWriteBinding target, int cycles) {
        super("SLA " + target, cycles);
        this.target = requireNonNull(target);
    }

    /**
     * Creates a new SLA instruction with the format {@code SLA Reg8}.
     *
     * @param target The target register.
     */
    public OP_SLA(Register8 target) {
        this(target.binding(), 4);
    }

    /**
     * Creates a new SLA instruction with the format {@code SLA (Reg8)}.
     *
     * @param target The target register.
     */
    public OP_SLA(Register16 target) {
        this(target.addressBinding(), 12);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        var result = target.read() << 1;

        // Check the carry flag.
        flags.write(Flag.C, (result & 0x100) == 0x100);

        // Write the result and read it back after it has been cast.
        target.write(result);
        result = target.read();

        // Update the remaining registers.
        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
    }

}
