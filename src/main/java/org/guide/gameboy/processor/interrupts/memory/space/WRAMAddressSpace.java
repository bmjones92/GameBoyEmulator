package org.guide.gameboy.processor.interrupts.memory.space;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.MemoryBank;
import org.guide.util.AddressUtils;

import java.nio.ByteBuffer;

/**
 * An address space that manages the "Work RAM" section of system memory. This section of memory
 *
 * @author Brendan Jones
 */
public class WRAMAddressSpace extends AddressSpace implements SerializableComponent {

    /**
     * The starting address for work RAM.
     */
    private static final int WRAM_ADDRESS_START = 0xC000;

    /**
     * The size of each RAM bank.
     */
    private static final int WRAM_BANK_SIZE = 0x1000;

    /**
     * The ending address for work RAM.
     */
    private static final int WRAM_ADDRESS_END = 0xDFFF;

    /**
     * Starting address for echo RAM.
     */
    private static final int ECHO_ADDRESS_START = 0xE000;

    /**
     * Ending address for Echo RAM.
     */
    private static final int ECHO_ADDRESS_END = 0xFDFF;

    /**
     * Address offset between Echo RAM and Work RAM.
     */
    private static final int ADDRESS_OFFSET = ECHO_ADDRESS_START - WRAM_ADDRESS_START;

    /**
     * The switchable RAM banks that can be swapped into the second half of the WRAM address range.
     */
    private final MemoryBank banks;

    /**
     * Whether the emulator is running in CGB mode. This determines whether WRAM allows for switchable
     * RAM banks.
     */
    private boolean isCGB;

    /**
     * Creates a new Work RAM address space and binds it to the specified memory component.
     *
     * @param memory The memory component.
     */
    public WRAMAddressSpace(Memory memory) {
        super(memory);
        this.banks = new MemoryBank(memory, false, true, WRAM_ADDRESS_START, 8, WRAM_BANK_SIZE);

        memory.setAddressSpace(this, WRAM_ADDRESS_START, WRAM_ADDRESS_END);
        memory.setAddressSpace(this, ECHO_ADDRESS_START, ECHO_ADDRESS_END);
        memory.setAddressSpace(this, AddressUtils.SVBK);
    }

    /**
     * Resets the address space to its default state.
     *
     * @param isCGB Whether the machine is running in CGB mode.
     */
    public void reset(boolean isCGB) {
        this.isCGB = isCGB;

        banks.reset(null);
        memory.set(AddressUtils.SVBK, isCGB ? 0xF9 : 0xFF);
    }

    @Override
    public void serialize(ByteBuffer out) {
        banks.serialize(out);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        banks.deserialize(in);
    }

    @Override
    public void write(int address, int value) {
        if (address == AddressUtils.SVBK) {
            if (isCGB) {
                // If attempting to select bank 0, force to bank 1.
                final var bank = Math.max(1, value & 0x7);
                banks.setActiveBank(bank);
            }
        } else if (isEchoRAMAddress(address)) {
            // Writes to Echo RAM are redirected to Work RAM.
            address -= ADDRESS_OFFSET;
        }

        // Update the value at the specified address.
        memory.set(address, value);
    }

    @Override
    public int read(int address) {
        if (!isCGB && address == AddressUtils.SVBK) {
            return 0xFF; // Forced to $FF in DMG mode.
        } else if (isEchoRAMAddress(address)) {
            // Reads from Echo RAM are redirected to Work RAM.
            address -= ADDRESS_OFFSET;
        }

        return memory.get(address);
    }

    /**
     * Checks whether the specified address is Echo RAM.
     *
     * @param address The address.
     * @return Whether the specified address is Echo RAM.
     */
    private static boolean isEchoRAMAddress(int address) {
        return address >= ECHO_ADDRESS_START && address <= ECHO_ADDRESS_END;
    }

}
