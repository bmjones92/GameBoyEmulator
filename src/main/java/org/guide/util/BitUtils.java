package org.guide.util;

/**
 * A collection of utilities for working with bits.
 *
 * @author Brendan Jones
 */
public class BitUtils {

    /**
     * The bitmask for bit 0.
     */
    public static final int BIT_0 = (1);

    /**
     * The bitmask for bit 1.
     */
    public static final int BIT_1 = (1 << 1);

    /**
     * The bitmask for bit 2.
     */
    public static final int BIT_2 = (1 << 2);

    /**
     * The bitmask for bit 3.
     */
    public static final int BIT_3 = (1 << 3);

    /**
     * The bitmask for bit 4.
     */
    public static final int BIT_4 = (1 << 4);

    /**
     * The bitmask for bit 5.
     */
    public static final int BIT_5 = (1 << 5);

    /**
     * The bitmask for bit 6.
     */
    public static final int BIT_6 = (1 << 6);

    /**
     * The bitmask for bit 7.
     */
    public static final int BIT_7 = (1 << 7);

    /**
     * The number of bytes per kilobit.
     */
    private static final int BYTES_PER_KBIT = 128;

    /**
     * Calculates the number of bytes in the specified number of kilobits.
     *
     * @param kbits The number of kilobits.
     * @return The number of bytes.
     */
    public static final int fromKBits(int kbits) {
        return kbits * BYTES_PER_KBIT;
    }

    /**
     * Calculates the number of bytes in the specified number of megabits.
     *
     * @param mbits The number of megabits.
     * @return The number of bytes.
     */
    public static final int fromMBits(int mbits) {
        return fromKBits(mbits * 1024);
    }

    /**
     * Checks whether the specified bit is set.
     *
     * @param value The value to check.
     * @param bit   The bit to check.
     * @return Whether the bit is set.
     */
    public static boolean isSet(int value, int bit) {
        return (value & (1 << bit)) != 0;
    }

    /**
     * Gets the value of the specified bit.
     *
     * @param value The value to check.
     * @param bit   The bit to check.
     * @return 1 if the bit is set, otherwise 0.
     */
    public static int getBit(int value, int bit) {
        return isSet(value, bit) ? 1 : 0;
    }

    /**
     * Sets the specified bit.
     *
     * @param value The value to set the bit on.
     * @param bit   The bit to set.
     * @return The result of setting the bit.
     */
    public static int setBit(int value, int bit) {
        return value | (1 << bit);
    }

    /**
     * Clears the specified bit.
     *
     * @param value The value to clear the bit on.
     * @param bit   The bit to clear.
     * @return The result of clearing the bit.
     */
    public static int clearBit(int value, int bit) {
        return value & ~(1 << bit);
    }

    /**
     * Utility class so no creating instances.
     */
    private BitUtils() {
    }

}
