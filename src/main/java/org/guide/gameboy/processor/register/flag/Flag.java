package org.guide.gameboy.processor.register.flag;

/**
 * The different flags in the Flag register.
 *
 * @author Brendan Jones
 */
public enum Flag {

    /**
     * The "zero" flag. This is set when the result of an arithmetic operation is zero, or when two values match
     * when using the "CP" instruction.
     */
    Z(7),

    /**
     * The "subtract" flag. This is set when a subtraction operation is performed in the last math instruction.
     */
    N(6),

    /**
     * The "half carry" flag. This is set if a carry occurred from the lower nybble in the last math instruction.
     */
    H(5),

    /**
     * The "carry" flag. This is set if a carry occurred from the last math instruction, or if register A is the
     * smaller value when executing the "CP" instruction.
     */
    C(4);

    /**
     * The bitmask for the flag.
     */
    private final int mask;

    /**
     * Creates a new Flag instance.
     *
     * @param bit The bit.
     */
    Flag(int bit) {
        this.mask = 1 << bit;
    }

    /**
     * Gets the bitmask for this flag.
     *
     * @return The bitmask.
     */
    public int mask() {
        return mask;
    }

    /**
     * Returns a string representing the data in this sequence. A new String object is allocated and initialized to
     * contain the character sequence currently represented by this object. This String is then returned. Subsequent
     * changes to this sequence do not affect the contents of the String.
     *
     * @param value Whether to return the represention if the flag is set or unset.
     * @return The string value.
     */
    public String toString(boolean value) {
        final var b = new StringBuilder();
        if (!value) {
            b.append('N');
        }
        return b.append(this).toString();
    }

}
