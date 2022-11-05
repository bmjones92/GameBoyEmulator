package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import static java.util.Objects.requireNonNull;

/**
 * Implements the "JR" instruction. This instruction jumps execution to a new address relative to the current address.
 *
 * @author Brendan Jones
 */
public class OP_JR extends JumpOpcode {

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new JR instruction.
     *
     * @param flag         The conditional flag to test when deciding to jump, or null to jump unconditionally.
     * @param value        Whether the conditional flag should be set or not.
     * @param source       The source register.
     * @param jumpCycles   The number of cycles when the jump passes.
     * @param noJumpCycles The number of cycles when the jump fails.
     */
    private OP_JR(Flag flag, boolean value, ReadBinding source, int jumpCycles, int noJumpCycles) {
        super("JR " + (flag != null ? flag.toString(value) + "," : "") + source, flag, value, jumpCycles, noJumpCycles);
        this.source = requireNonNull(source);
    }

    /**
     * Creates a new JR instruction with the format: {@code JR flag,r8}.
     *
     * @param flag   The conditional flag to test when deciding to jump, or null to jump unconditionally.
     * @param value  Whether necessary flag state for the jump to pass.
     * @param memory The memory component.
     */
    public OP_JR(Flag flag, boolean value, MemoryBindings memory) {
        this(flag, value, memory.bindingR8(), 12, 8);
    }

    /**
     * Creates a new JR instruction with the format: {@code JR r8}.
     *
     * @param memory The memory component.
     */
    public OP_JR(MemoryBindings memory) {
        this(null, false, memory.bindingR8(), 12, 12);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var offset = source.read();
        if (checkJumped(flags)) {
            final var pc = cpu.getPC();
            pc.write(pc.read() + offset);
        }
    }

}
