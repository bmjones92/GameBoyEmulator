package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.WriteBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "POP" instruction. This instruction pops a frame from the stack pointer and loads it into the target
 * binding.
 *
 * @author Brendan Jones
 */
public class OP_POP extends Opcode {

    /**
     * The target binding.
     */
    private final WriteBinding target;

    /**
     * Creates a new POP instruction.
     *
     * @param target The target binding.
     */
    private OP_POP(WriteBinding target) {
        super("POP " + target, 12);
        this.target = requireNonNull(target);
    }

    /**
     * Creates a new POP instruction with the format {@code POP Reg16}.
     *
     * @param target The target register.
     */
    public OP_POP(Register16 target) {
        this(target.binding());
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        target.write(cpu.popFromStack());
    }

}
