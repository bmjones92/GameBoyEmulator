package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "PUSH" instruction. This instruction loads a value from the source binding and pushes it to the stack.
 *
 * @author Brendan Jones
 */
public class OP_PUSH extends Opcode {

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new PUSH instruction.
     *
     * @param source The source binding.
     */
    private OP_PUSH(ReadBinding source) {
        super("PUSH " + source, 16);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new PUSH instruction with the format {@code PUSH Reg16}.
     *
     * @param target The target register.
     */
    public OP_PUSH(Register16 target) {
        this(target.binding());
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var value = source.read();
        cpu.pushToStack(value);
    }

}
