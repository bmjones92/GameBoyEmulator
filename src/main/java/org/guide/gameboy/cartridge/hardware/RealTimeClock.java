package org.guide.gameboy.cartridge.hardware;

/**
 * Represents the real time clock component present on some game cartridges.
 *
 * @author Brendan Jones
 */
public class RealTimeClock {

    /**
     * The current seconds of the clock (0-59). This is mapped to register $08.
     */
    private int seconds;

    /**
     * The current minutes of the clock (0-59). This is mapped to register $09.
     */
    private int minutes;

    /**
     * The current hours of the clock (0-23). This is mapped to register $0A.
     */
    private int hours;

    /**
     * The current day of the clock (0-255). Note that the {@code flags} register stores
     * the upper bit of this value. This is mapped to register $0B.
     */
    private int day;

    /**
     * The current attribute flags of the clock. This is mapped to register $0C.
     * - Bit 0: Upper bit of day value.
     * - Bit 6: Halt (0 = Active, 1 = Stop Timer)
     * - Bit 7: Day Counter Carry Bit (1 = Overflow).
     */
    private int flags;

    public RealTimeClock() {

    }

}
