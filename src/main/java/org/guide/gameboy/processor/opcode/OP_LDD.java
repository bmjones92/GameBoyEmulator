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
 * Implements the "LDD" instruction. This instruction is similar to {@link OP_LD}, but additionally decrements the
 * source binding after loading its value into the target binding.
 *
 * @author Brendan Jones
 */
public class OP_LDD extends Opcode {

    /**
     * The target binding.
     */
    private final WriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * The decrement binding.
     */
    private final ReadWriteBinding decrement;

    /**
     * Creates a new LDD instruction.
     *
     * @param target    The target binding.
     * @param source    The source binding.
     * @param decrement The decrement binding.
     */
    private OP_LDD(WriteBinding target, ReadBinding source, ReadWriteBinding decrement) {
        super("LDD " + target + "," + source, 8);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
        this.decrement = requireNonNull(decrement);
    }

    /**
     * Creates a new LDD instruction with the format {@code LDD (Reg8),Reg16}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_LDD(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), source.binding());
    }

    /**
     * Creates a new LDD instruction with the format {@code LDD (Reg16),Reg8}.
     *
     * @param target The target binding.
     * @param source The source binding.
     */
    public OP_LDD(Register16 target, Register8 source) {
        this(target.addressBinding(), source.binding(), target.binding());
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        target.write(source.read());
        decrement.write(decrement.read() - 1);
    }

}