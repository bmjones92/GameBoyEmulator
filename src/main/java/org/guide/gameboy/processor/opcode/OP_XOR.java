package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.opcode.binding.ReadWriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.Register8;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "XOR" instruction. This instruction computes the exclusive or of the target and source and loads the
 * result into the target.
 *
 * @author Brendan Jones
 */
public class OP_XOR extends Opcode {

    /**
     * The target binding.
     */
    private final ReadWriteBinding target;

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new XOR instruction.
     *
     * @param target The target binding.
     * @param source The source binding.
     * @param cycles How many cycles the instruction takes to complete.
     */
    private OP_XOR(ReadWriteBinding target, ReadBinding source, int cycles) {
        super("XOR " + target + "," + source, cycles);
        this.target = requireNonNull(target);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new XOR instruction with the format {@code XOR Reg8,Reg8}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_XOR(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), 4);
    }

    /**
     * Creates a new XOR instruction with the format {@code XOR Reg8,(Reg16)}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_XOR(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new XOR instruction with the format {@code XOR Reg8,d8}.
     *
     * @param target The target register.
     * @param memory The memory bindings.
     */
    public OP_XOR(Register8 target, MemoryBindings memory) {
        this(target.binding(), memory.bindingD8(), 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var result = target.read() ^ source.read();
        target.write(result);

        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, false);
        flags.write(Flag.C, false);
    }
}
