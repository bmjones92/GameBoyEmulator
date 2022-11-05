package org.guide.gui.control.memory;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.guide.util.StringUtils;

/**
 * An individual entry within the memory viewer table.
 *
 * @author Brendan Jones
 */
public class MemoryTableEntry {

    /**
     * The number of values to store in a single entry.
     */
    public static final int NUM_VALUES = 0x10;

    /**
     * The property to display in the base address column of the table.
     */
    private final StringProperty baseAddress;

    /**
     * The properties to display in the value columns of the table.
     */
    private final StringProperty[] values = new SimpleStringProperty[NUM_VALUES];

    /**
     * Creates a new MemoryTableEntry instance.
     *
     * @param baseAddress The base address for the entry.
     */
    public MemoryTableEntry(int baseAddress) {
        this.baseAddress = new SimpleStringProperty(StringUtils.getHex16(baseAddress));
        for (int i = 0; i < values.length; ++i) {
            values[i] = new SimpleStringProperty(StringUtils.getHex8(0x00));
        }
    }

    /**
     * Gets the base address for this entry.
     *
     * @return The base address.
     */
    public StringProperty baseAddressProperty() {
        return baseAddress;
    }

    /**
     * Gets the specified value for this entry.
     *
     * @param index The index of the value.
     * @return The value.
     */
    public StringProperty valueProperty(int index) {
        return values[index];
    }

}
