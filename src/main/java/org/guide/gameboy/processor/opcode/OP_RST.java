package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.FlagRegister;
import org.guide.util.StringUtils;

/**
 * Implements the "RST" instruction. This instruction pushes the current address onto the stack and jumps to the target
 * address.
 *
 * @author Brendan Jones
 */
public class OP_RST extends Opcode {

    /**
     * The address to jump to.
     */
    private final int address;

    /**
     * Creates a new RST instruction.
     *
     * @param address The address to jump to.
     */
    public OP_RST(int address) {
        super("RST " + StringUtils.getHex8(address), 16);
        this.address = address;
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        final var pc = cpu.getPC();
        cpu.pushToStack(pc.read());
        pc.write(address);
    }

}
