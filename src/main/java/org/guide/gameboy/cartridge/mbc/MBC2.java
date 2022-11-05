package org.guide.gameboy.cartridge.mbc;

import org.guide.gameboy.cartridge.Cartridge;
import org.guide.gameboy.processor.interrupts.memory.Memory;

import java.nio.ByteBuffer;

/**
 * Represents a "Type 2" memory controller. A specification of this controller can be found
 * <a href="https://gbdev.gg8.se/wiki/articles/Memory_Bank_Controllers#MBC2_.28max_256KByte_ROM_and_512x4_bits_RAM.29">
 * here
 * </a>
 *
 * @author Brendan Jones
 */
public class MBC2 extends MBC {

    public MBC2(Memory memory, Cartridge cart, byte[] rom) {
        super(memory, cart, rom);
    }

    @Override
    public void serialize(ByteBuffer out) {
        throw new UnsupportedOperationException("MBC2 functionality is not yet implemented.");
    }

    @Override
    public void deserialize(ByteBuffer in) {
        throw new UnsupportedOperationException("MBC2 functionality is not yet implemented.");
    }

    @Override
    public void write(int address, int value) {
        throw new UnsupportedOperationException("MBC2 functionality is not yet implemented.");
    }

}
