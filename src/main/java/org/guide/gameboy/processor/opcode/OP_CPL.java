package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "CPL" instruction.
 *
 * @author Brendan Jones
 */
public class OP_CPL extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * Creates a new CPL instruction.
     *
     * @param target The target binding.
     */
    private OP_CPL(ReadWriteBinding target) {
        super("CPL " + target, 4);
        this.target = requireNonNull(target);
    }

    /**
     * Creates a new CPL instruction with the format {@code CPL Reg8}.
     *
     * @param target The target binding.
     */
    public OP_CPL(Register8 target) {
        this(target.binding());
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        target.write(~target.read());

        flags.write(Flag.N, true);
        flags.write(Flag.H, true);
    }

}
