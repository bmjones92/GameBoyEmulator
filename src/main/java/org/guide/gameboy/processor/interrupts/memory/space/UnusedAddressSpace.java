package org.guide.gameboy.processor.interrupts.memory.space;

import org.guide.gameboy.processor.interrupts.memory.Memory;

/**
 * An address space that manages the unusable section of system memory.
 *
 * @author Brendan Jones
 */
public class UnusedAddressSpace extends AddressSpace {

    /**
     * The start of the address range.
     */
    private static final int ADDRESS_START = 0xFEA0;

    /**
     * The partition address.
     */
    private static final int ADDRESS_PARTITION = 0xFEC0;

    /**
     * The end of the address range.
     */
    private static final int ADDRESS_END = 0xFEFF;

    /**
     * Creates a new unused address space and binds it to the specified memory component.
     *
     * @param memory The memory component.
     */
    public UnusedAddressSpace(Memory memory) {
        super(memory);

        memory.setAddressSpace(this, ADDRESS_START, ADDRESS_END);
    }

    /**
     * Resets the memory addresses managed by this address space to their default values.
     */
    public void reset() {
        for (var address = ADDRESS_START; address < ADDRESS_PARTITION; ++address) {
            memory.set(address, address - ADDRESS_START);
        }

        for (var address = ADDRESS_PARTITION; address <= ADDRESS_END; ++address) {
            memory.set(address, 0x20 + (address & 0xF));
        }
    }

    @Override
    public void write(int address, int value) {
        if (address >= ADDRESS_START && address < ADDRESS_PARTITION) {
            memory.set(address, value);
        } else {
            for (var i = ADDRESS_PARTITION + (value & 0xF); i <= ADDRESS_END; i += 0x10) {
                memory.set(i, value);
            }
        }
    }

}
