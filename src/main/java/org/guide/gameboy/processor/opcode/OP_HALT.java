package org.guide.gameboy.processor.opcode;

import org.guide.gameboy.processor.CPUStatusMode;
import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.flag.FlagRegister;

/**
 * Implements the HALT instruction. This forces the CPU to stop executing instructions until (IE & IF & 1F) != 0. Once
 * an enabled interrupt is requested, the CPU will resume execution like normal.
 * <p>
 * If the {@code IME} flag is set, then the address following the {@code HALT} instruction will be pushed to the stack,
 * program execution will resume from the interrupt vector address, and the corresponding IF bit will be cleared.
 * <p>
 * If the {@code IME} flag is not set then CPU will not jump to the interrupt vector, but instead from the instruction
 * following the HALT, and the corresponding IF bit is not cleared. Note: If an enabled interrupt has already been
 * requested when the HALT instruction is set, then the HALT bug occurs. When this happens, the program counter does
 * not increment when reading the next instruction (the byte following the HALT instruction is read twice).
 *
 * @author Brendan Jones
 */
public class OP_HALT extends Opcode {

    public OP_HALT() {
        super("HALT", 4);
    }

    @Override
    public void execute(Processor cpu, Memory memory, FlagRegister flags) {
        cpu.setStatus(CPUStatusMode.HALTED);
    }

}
