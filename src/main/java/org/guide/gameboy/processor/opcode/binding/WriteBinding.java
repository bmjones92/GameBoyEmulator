package org.guide.gameboy.processor.opcode.binding;

/**
 * A binding that allows for data to be written to a data source.
 *
 * @author Brendan Jones
 */
public interface WriteBinding {

    /**
     * Writes a value to the data source.
     *
     * @param value The data value.
     */
    void write(int value);

}
