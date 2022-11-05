package org.guide.gameboy.processor.interrupts;

/**
 * Represents the possible processor interrupts.
 *
 * @author Brendan Jones
 */
public enum Interrupt {

    /**
     * Indicates that the LCD controller has entered the V-blank period.
     */
    V_BLANK(0x0040, 4, 0),

    /**
     * Indicates that the LCD status register has changed.
     */
    LCD_STAT(0x0048, 0, 0),

    /**
     * Indicates that the timer register has overflowed.
     */
    TIMER(0x0050, 0, 0),

    /**
     * Indicates that a serial transfer has completed.
     */
    SERIAL(0x0058, 0, 0),

    /**
     * Indicates that an input button has been pressed.
     */
    INPUT(0x0060, 0, 0);

    /**
     * The memory address of the interrupt handler.
     */
    private final int address;

    /**
     * The delay before the interrupt can be handled on DMG devices.
     */
    private final int dmgDelay;

    /**
     * The delay before the interrupt can be handled on CGB devices.
     */
    private final int cgbDelay;

    /**
     * Creates a new Interrupt instance.
     *
     * @param address  The address of the handler.
     * @param dmgDelay The number of cycles to wait before handling the interrupt on DMG devices.
     * @param cgbDelay The number of cycles to wait before handling the interrupt on CGB devices.
     */
    Interrupt(int address, int dmgDelay, int cgbDelay) {
        this.address = address;
        this.dmgDelay = dmgDelay;
        this.cgbDelay = cgbDelay;
    }

    /**
     * Gets the address of the handler for this interrupt.
     *
     * @return The handler address.
     */
    public int getAddress() {
        return address;
    }

    /**
     * Gets the number of cycles to wait before handling this interrupt.
     *
     * @param isCGB Whether the device is running in DMG or CGB mode.
     * @return The delay.
     */
    public int getDelay(boolean isCGB) {
        return isCGB ? cgbDelay : dmgDelay;
    }

}
