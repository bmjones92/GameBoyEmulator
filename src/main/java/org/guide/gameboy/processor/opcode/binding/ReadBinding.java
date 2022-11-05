package org.guide.gameboy.processor.opcode.binding;

/**
 * A binding that allows for data to be read from a data source.
 *
 * @author Brendan Jones
 */
public interface ReadBinding {

    /**
     * Reads a value from the data source.
     *
     * @return The data value.
     */
    int read();

}
