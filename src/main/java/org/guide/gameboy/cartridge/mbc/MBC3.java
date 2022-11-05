package org.guide.gameboy.cartridge.mbc;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.cartridge.Cartridge;
import org.guide.gameboy.cartridge.hardware.RealTimeClock;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.util.AddressUtils;

import java.nio.ByteBuffer;

/**
 * Represents a "Type 3" memory controller. A specification of this controller can be found
 * <a href="https://gbdev.gg8.se/wiki/articles/Memory_Bank_Controllers#MBC3_.28max_2MByte_ROM_and.2For_64KByte_RAM_and_Timer.29">
 * here
 * </a>
 *
 * @author Brendan Jones
 */
public class MBC3 extends MBC implements SerializableComponent {

    /**
     * The currently selected RAM bank.
     */
    private int currentRAMBank;

    /**
     * The real time clock installed on the cartridge.
     */
    private final RealTimeClock clock;

    /**
     * Creates a new Memory Bank Controller.
     *
     * @param memory The memory component.
     * @param cart   The cartridge.
     * @param rom    The ROM to manage.
     */
    public MBC3(Memory memory, Cartridge cart, byte[] rom) {
        super(memory, cart, rom);
        this.clock = cart.getRTC();
    }

    @Override
    public void reset() {
        super.reset();
        this.currentRAMBank = 0;
    }

    @Override
    public void write(int address, int value) {
        switch (address & 0xE000) {
            case 0x2000:
                onWriteROMBank(value);
                break;
            case 0x4000:
                onWriteRAMTimerRegister(value);
                break;
            case 0x6000:
                onWriteLatchClockData(value);
                break;
            case 0xA000:
                if (!isRTCRegister(address)) {
                    super.write(address, value);
                } else {
                    // TODO Implement real time clock
                }
                break;
            default:
                super.write(address, value);
                break;
        }
    }

    @Override
    public int read(int address) {
        if (isRTCRegister(address)) {
            // TODO Implement RTC
            return 0;
        } else {
            return super.read(address);
        }
    }

    @Override
    public void serialize(ByteBuffer out) {
        super.serialize(out);

        out.put((byte) currentRAMBank);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        super.deserialize(in);

        this.currentRAMBank = in.get() & 0xFF;
    }

    private void onWriteROMBank(int value) {
        value = Math.max(1, value & 0x7F);
        romBanks.setActiveBank(value);
    }

    /**
     * Selects the RAM bank or RTC register to map to CRAM.
     *
     * @param value The bank or register to select.
     */
    private void onWriteRAMTimerRegister(int value) {
        this.currentRAMBank = (value & 0xFF);
        if (value < 0x8) {
            if (ramBanks != null) {
                ramBanks.setActiveBank(currentRAMBank);
            }
        } else {
            // TODO Implement RTC.
        }
    }

    private void onWriteLatchClockData(int value) {
        // TODO Implement RTC.
    }

    private boolean isRTCRegister(int address) {
        return currentRAMBank >= 0x08 && AddressUtils.isAddressCRAM(address);
    }

}
