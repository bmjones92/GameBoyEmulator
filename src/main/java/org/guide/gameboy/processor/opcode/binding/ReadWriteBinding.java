package org.guide.gameboy.processor.opcode.binding;

/**
 * A binding that allows for data to be read from and written to a data source.
 *
 * @author Brendan Jones
 */
public interface ReadWriteBinding extends ReadBinding, WriteBinding {

    @Override
    int read();

    @Override
    void write(int value);

}
