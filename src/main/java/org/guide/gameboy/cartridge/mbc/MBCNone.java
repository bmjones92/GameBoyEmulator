package org.guide.gameboy.cartridge.mbc;

import org.guide.gameboy.cartridge.Cartridge;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.util.AddressUtils;

/**
 * Represents a memory controller that does not require ROM banking. A specification for this controller can be found
 * <a href="https://gbdev.gg8.se/wiki/articles/Memory_Bank_Controllers#None_.2832KByte_ROM_only.29">
 * here
 * </a>
 *
 * @author Brendan Jones
 */
public class MBCNone extends MBC {

    /**
     * Creates a new Memory Bank Controller.
     *
     * @param memory The memory component.
     * @param cart   The cartridge.
     * @param rom    The ROM data.
     */
    public MBCNone(Memory memory, Cartridge cart, byte[] rom) {
        super(memory, cart, rom);
    }

    @Override
    public void write(int address, int value) {
        if (ramBanks != null && AddressUtils.isAddressCRAM(address)) {
            memory.set(address, value);
        }
    }

}
