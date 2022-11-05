package org.guide.gameboy.processor.opcode.binding;

import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.register.PointerRegister;

/**
 * Contains a collection of memory bindings that can be used for implementing processor instructions.
 *
 * @author Brendan Jones
 */
public class MemoryBindings {

    /**
     * The A16 memory binding.
     */
    private final MemoryBindingA16 bindingA16;

    /**
     * The (A16) memory binding.
     */
    private final MemoryBindingA16Address bindingA16Address;

    /**
     * The Pointer Register memory binding.
     */
    private final MemoryBindingA16Pointer bindingA16Pointer;

    /**
     * The (A8) memory binding.
     */
    private final MemoryBindingA8Address bindingA8Address;

    /**
     * The D16 memory binding.
     */
    private final MemoryBindingD16 bindingD16;

    /**
     * The D8 memory binding.
     */
    private final MemoryBindingD8 bindingD8;

    /**
     * The R8 memory binding.
     */
    private final MemoryBindingR8 bindingR8;

    /**
     * Creates new memory bindings.
     *
     * @param memory The memory component to bind to.
     * @param pc     The program counter to bind to.
     */
    public MemoryBindings(Memory memory, PointerRegister pc) {
        this.bindingA16 = new MemoryBindingA16(memory, pc);
        this.bindingA16Address = new MemoryBindingA16Address(memory, pc);
        this.bindingA16Pointer = new MemoryBindingA16Pointer(memory, pc);
        this.bindingA8Address = new MemoryBindingA8Address(memory, pc);
        this.bindingD16 = new MemoryBindingD16(memory, pc);
        this.bindingD8 = new MemoryBindingD8(memory, pc);
        this.bindingR8 = new MemoryBindingR8(memory, pc);
    }

    /**
     * Gets the "A16" memory binding.
     *
     * @return The "A16" memory binding.
     */
    public MemoryBindingA16 bindingA16() {
        return bindingA16;
    }

    /**
     * Gets the "(A16)" memory binding.
     *
     * @return The "(A16)" memory binding.
     */
    public MemoryBindingA16Address bindingA16Address() {
        return bindingA16Address;
    }

    /**
     * Gets the "pointer register" memory binding.
     *
     * @return The "pointer register" memory binding.
     */
    public MemoryBindingA16Pointer bindingA16Pointer() {
        return bindingA16Pointer;
    }

    /**
     * Gets the "(A8)" memory binding.
     *
     * @return The "(A8)" memory binding.
     */
    public MemoryBindingA8Address bindingA8Address() {
        return bindingA8Address;
    }

    /**
     * Gets the "D16" memory binding.
     *
     * @return The "D16" memory binding.
     */
    public MemoryBindingD16 bindingD16() {
        return bindingD16;
    }

    /**
     * Gets the "D8" memory binding.
     *
     * @return The "D8" memory binding.
     */
    public MemoryBindingD8 bindingD8() {
        return bindingD8;
    }

    /**
     * Gets the "R8" memory binding.
     *
     * @return The "R8" memory binding.
     */
    public MemoryBindingR8 bindingR8() {
        return bindingR8;
    }

}
