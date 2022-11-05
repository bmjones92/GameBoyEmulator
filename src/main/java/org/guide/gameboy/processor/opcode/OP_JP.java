package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.opcode.binding.MemoryBindings;
import org.guide.gameboy.processor.opcode.binding.ReadBinding;
import org.guide.gameboy.processor.register.Register16;
import org.guide.gameboy.processor.register.flag.Flag;
import org.guide.gameboy.processor.register.flag.FlagRegister;

import java.util.Objects;

/**
 * Implements the "JP" instruction. This instruction jumps execution to a new address.
 *
 * @author Brendan Jones
 */
public class OP_JP extends JumpOpcode {

    /**
     * The source binding.
     */
    private final ReadBinding source;

    /**
     * Creates a new JP instruction.
     *
     * @param flag         The flag to conditionally jump on, or null to always jump.
     * @param cnd          Whether the flag should be set for the condition to pass.
     * @param source       The source binding.
     * @param jumpCycles   The number of cycles when a jump passes.
     * @param noJumpCycles The number of cycles when a jump fails.
     */
    private OP_JP(Flag flag, boolean cnd, ReadBinding source, int jumpCycles, int noJumpCycles) {
        super("JP " + (flag != null ? flag.toString(cnd) + "," : "") + source, flag, cnd, jumpCycles, noJumpCycles);
        this.source = Objects.requireNonNull(source, "Source binding cannot be null.");
    }

    /**
     * Creates a new JP instruction with the format: {@code JP condition,a16}
     *
     * @param flag   The flag to conditionally jump on, or null to always jump.
     * @param cnd    Whether the flag should be set for the condition to pass.
     * @param memory The memory component to bind to.
     */
    public OP_JP(Flag flag, boolean cnd, MemoryBindings memory) {
        this(flag, cnd, memory.bindingA16(), 16, 12);
    }

    /**
     * Creates a new JP instruction with the format: {@code JP a16}.
     *
     * @param memory The memory component to bind to.
     */
    public OP_JP(MemoryBindings memory) {
        this(null, false, memory.bindingA16(), 16, 12);
    }

    /**
     * Creates a new JP instruction with the format: {@code JP (reg16)}.
     *
     * @param source The source binding.
     */
    public OP_JP(Register16 source) {
        this(null, false, source.binding(), 4, 4);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        int result = source.read();
        if (checkJumped(flags)) {
            final var pc = cpu.getPC();
            pc.write(result);
        }
    }

}
