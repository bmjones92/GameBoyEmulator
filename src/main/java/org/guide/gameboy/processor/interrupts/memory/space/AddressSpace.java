package org.guide.gameboy.processor.interrupts.memory.space;

import org.guide.gameboy.processor.interrupts.memory.Memory;

import static java.util.Objects.requireNonNull;

/**
 * Address spaces intercept reads and writes to system memory and serve as a base for implementing hardware
 * functionality that utilizes memory-mapped registers.
 *
 * @author Brendan Jones
 */
public class AddressSpace {

    /**
     * The memory component.
     */
    protected final Memory memory;

    /**
     * Creates a new address space that is bound to the specified memory component.
     *
     * @param memory The memory component to bind to.
     */
    public AddressSpace(Memory memory) {
        this.memory = requireNonNull(memory);
    }

    /**
     * Writes data to this address space. Implementations can override this function to implement custom logic when
     * specific hardware registers or address ranges are written to.
     *
     * @param address The address to write to.
     * @param value   The value to write.
     */
    public void write(int address, int value) {
        memory.set(address, value);
    }

    /**
     * Reads data from this address space. Implementations can override this function to implement custom logic when
     * specific hardware registers or address ranges are read from.
     *
     * @param address The address to read from.
     * @return The value that was read.
     */
    public int read(int address) {
        return memory.get(address);
    }

}
