package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "CALL" instruction.
 *
 * @author Brendan Jones
 */
public class OP_CALL extends JumpOpcode {

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new CALL instruction.
     *
     * @param flag         The flag to compare to.
     * @param value        Whether the flag should be set.
     * @param source       The source binding.
     * @param jumpCycles   The number of cycles when the jump passes.
     * @param noJumpCycles The number of cycles when the jump fails.
     */
    private OP_CALL(Flag flag, boolean value, ReadBinding source, int jumpCycles, int noJumpCycles) {
        super("CALL " + (flag == null ? "" : flag.toString(value) + ",") + source, flag, value, jumpCycles,
                noJumpCycles);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new CALL instruction with the format {@code CALL flag,a16}.
     *
     * @param flag   The conditional flag to jump on.
     * @param value  Whether the flag should be set.
     * @param source The source bindings.
     */
    public OP_CALL(Flag flag, boolean value, MemoryBindings source) {
        this(flag, value, source.bindingA16(), 24, 12);
    }

    /**
     * Creates a new CALL instruction with the format {@code CALL a16}.
     *
     * @param source The source bindings.
     */
    public OP_CALL(MemoryBindings source) {
        this(null, false, source.bindingA16(), 24, 24);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var targetAddress = source.read();
        if (checkJumped(flags)) {
            final var pc = cpu.getPC();
            cpu.pushToStack(pc.read());
            pc.write(targetAddress);
        }
    }

}
