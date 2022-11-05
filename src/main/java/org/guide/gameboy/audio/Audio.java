package org.guide.gameboy.audio;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.util.AddressUtils;

import java.nio.ByteBuffer;

/**
 * The Audio component of the Game Boy is responsible for producing and playing audio.
 *
 * @author Brendan Jones
 */
public class Audio extends AddressSpace implements SerializableComponent {

    /**
     * The starting address for audio RAM.
     */
    private static final int ADDRESS_RAM_START = 0xFF30;

    /**
     * The ending address for audio RAM.
     */
    private static final int ADDRESS_RAM_END = 0xFF40;

    /**
     * Creates a new Audio component.
     *
     * @param memory The memory component.
     */
    public Audio(Memory memory) {
        super(memory);

        // Register this address space for all sound registers.
        memory.setAddressSpace(this, AddressUtils.NR10);
        memory.setAddressSpace(this, AddressUtils.NR11);
        memory.setAddressSpace(this, AddressUtils.NR12);
        memory.setAddressSpace(this, AddressUtils.NR13);
        memory.setAddressSpace(this, AddressUtils.NR14);
        memory.setAddressSpace(this, AddressUtils.NR21);
        memory.setAddressSpace(this, AddressUtils.NR22);
        memory.setAddressSpace(this, AddressUtils.NR23);
        memory.setAddressSpace(this, AddressUtils.NR24);
        memory.setAddressSpace(this, AddressUtils.NR30);
        memory.setAddressSpace(this, AddressUtils.NR31);
        memory.setAddressSpace(this, AddressUtils.NR32);
        memory.setAddressSpace(this, AddressUtils.NR33);
        memory.setAddressSpace(this, AddressUtils.NR34);
        memory.setAddressSpace(this, AddressUtils.NR41);
        memory.setAddressSpace(this, AddressUtils.NR42);
        memory.setAddressSpace(this, AddressUtils.NR43);
        memory.setAddressSpace(this, AddressUtils.NR44);
        memory.setAddressSpace(this, AddressUtils.NR50);
        memory.setAddressSpace(this, AddressUtils.NR51);
        memory.setAddressSpace(this, AddressUtils.NR52);

        memory.setAddressSpace(this, ADDRESS_RAM_START, ADDRESS_RAM_END);
    }

    @Override
    public void write(int address, int value) {
        if (address == AddressUtils.NR52) {
            // FIXME Temporary workaround to get around certain games relying on audio timing to work.
            value &= 0x70;
        }
        memory.set(address, value);
    }

    @Override
    public int read(int address) {
        return memory.get(address);
    }

    @Override
    public void serialize(ByteBuffer out) {
        // TODO
    }

    @Override
    public void deserialize(ByteBuffer in) {
        // TODO
    }

}
