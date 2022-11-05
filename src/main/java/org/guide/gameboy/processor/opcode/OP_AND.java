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
 * Implements the "AND" instruction.
 *
 * @author Brendan Jones
 */
public class OP_AND extends Opcode {

    /**
     * The binding to write the result to.
     */
    private final ReadWriteBinding target;

    /**
     * The binding to read from.
     */
    private final ReadBinding source;

    /**
     * Creates a new AND instruction.
     *
     * @param target The target data binding.
     * @param source The source data binding.
     * @param cycles The number of cycles the instruction takes to complete.
     */
    private OP_AND(ReadWriteBinding target, ReadBinding source, int cycles) {
        super("AND " + target + "," + source, cycles);
        this.target = requireNonNull(target, "Target binding cannot be null.");
        this.source = requireNonNull(source, "Source binding cannot be null.");
    }

    /**
     * Creates a new AND instruction with the format: {@code ADD Reg8,Reg8}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_AND(Register8 target, Register8 source) {
        this(target.binding(), source.binding(), 4);
    }

    /**
     * Creates a new AND instruction with the format: {@code AND Reg8,(Reg16)}.
     *
     * @param target The target register.
     * @param source The source register.
     */
    public OP_AND(Register8 target, Register16 source) {
        this(target.binding(), source.addressBinding(), 8);
    }

    /**
     * Creates a new instruction with the format: {@code AND Reg8,d8}.
     *
     * @param target The target register.
     * @param memory The memory source.
     */
    public OP_AND(Register8 target, MemoryBindings memory) {
        this(target.binding(), memory.bindingD8(), 8);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        int result = target.read() & source.read();
        target.write(result);

        flags.write(Flag.Z, result == 0);
        flags.write(Flag.N, false);
        flags.write(Flag.H, true);
        flags.write(Flag.C, false);
    }

}
