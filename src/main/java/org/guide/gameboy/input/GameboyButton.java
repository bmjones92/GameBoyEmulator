package org.guide.gameboy.input;

/**
 * Represents the different buttons present on the Game Boy controller.
 *
 * @author Brendan Jones
 */
public enum GameboyButton {

    /**
     * Line: 4
     * Bit: 0
     */
    A,

    /**
     * Line: 4
     * Bit: 1
     */
    B,

    /**
     * Line: 4
     * Bit: 2
     */
    SELECT,

    /**
     * Line: 4
     * Bit: 3
     */
    START,

    /**
     * Line: 5
     * Bit: 3
     */
    RIGHT,

    /**
     * Line: 5
     * Bit: 2
     */
    LEFT,

    /**
     * Line: 5
     * Bit: 1
     */
    UP,

    /**
     * Line: 5
     * Bit: 0
     */
    DOWN
}
