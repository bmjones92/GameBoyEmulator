package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.opcode.binding.WriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "LDI" instruction. This instruction is similar to {@link OP_LD}, but additionally increments the
 * source binding after loading its value into the target binding.
 */
public class OP_LDI extends Opcode {

    /**
     * The target binding.
     */
    private final WriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * The increment binding.
     */
    private final ReadWriteBinding increment;

    /**
     * Creates a new LDI instruction.
     *
     * @param target    The target binding.
     * @param source    The source binding.
     * @param increment The increment binding.
     */
    private OP_LDI(WriteBinding target, ReadBinding source, ReadWriteBinding increment) {
        super("LDI " + target + "," + source, 8);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
        this.increment = requireNonNull(increment);
    }

    /**
     * Creates a new LDI instruction with the format {@code LDI Reg8,(Reg16)}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_LDI(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), source.binding());
    }

    /**
     * Creates a new LDI instruction with the format {@code LDI (Reg16),Reg8}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_LDI(Register16 target, Register8 source) {
        this(target.addressBinding(), source.binding(), target.binding());
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        target.write(source.read());
        increment.write(increment.read() + 1);
    }

}
