package org.guide.gui.control.memory;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

import java.lang.reflect.Field;

/**
 * Implementation for the cells used by the debug memory viewer. These cells can be edited and are formatted as 8-bit
 * hexadecimal values.
 *
 * @author Brendan Jones
 */
public class MemoryTableCell extends TextFieldTableCell<MemoryTableEntry, String> {

    /**
     * The string converter to use for table cells.
     */
    private static final StringConverter<String> CONVERTER = new DefaultStringConverter();

    /**
     * The list of style classes to apply to all cells.
     */
    private static final ObservableList<String> defaultStyleClass = FXCollections.observableArrayList();

    /**
     * Unfortunately JavaFX doesn't expose the text field to subclasses, so we need to do some reflection hacks to
     * gain direct access to it.
     */
    private static Field TEXT_FIELD;

    /**
     * The column index of this cell.
     */
    private final int columnIndex;

    /**
     * The text field instance.
     */
    private TextField textField;

    /**
     * Creates a new MemoryTableCell instance.
     *
     * @param columnIndex The cell's column within the table.
     */
    public MemoryTableCell(int columnIndex) {
        super(CONVERTER);
        this.columnIndex = columnIndex;

        synchronized (defaultStyleClass) {
            if (defaultStyleClass.isEmpty()) {
                defaultStyleClass.addAll(getStyleClass());
            }
        }
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        getStyleClass().setAll(defaultStyleClass);

        int address = getTableRow().getIndex() * MemoryTableEntry.NUM_VALUES + columnIndex;
        if (address >= 0x0000 && address <= 0xFFFF) {
            getStyleClass().add(MemorySection.getSectionForAddress(address).styleClass());
        }

        setText(item);
        setEditable(true);
    }

    @Override
    public void startEdit() {
        super.startEdit();

        if (textField == null) {
            initializeTextField();
        }
    }

    @Override
    public void commitEdit(String text) {
        if (text.length() < 2) {
            text = "0".repeat(2 - text.length()) + text;
        }
        super.commitEdit(text);
    }

    /**
     * Initializes the text field.
     */
    private void initializeTextField() {
        try {
            // Unfortunately the base class doesn't expose the text field to subclasses. We can get around this
            // limitation by using reflection to access it anyway.
            if (TEXT_FIELD == null) {
                TEXT_FIELD = TextFieldTableCell.class.getDeclaredField("textField");
                TEXT_FIELD.setAccessible(true);
            }

            // Now we can access the text field directly.
            this.textField = (TextField) TEXT_FIELD.get(this);
            if (textField != null) {
                textField.textProperty().addListener(this::onTextFieldChanged);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Called whenever the text property of the text field changes.
     *
     * @param obs      The observer.
     * @param oldValue The old value of the text field.
     * @param newValue The new value of the text field.
     */
    private void onTextFieldChanged(ObservableValue<? extends String> obs, String oldValue, String newValue) {
        if (newValue.isEmpty()) {
            return; // Nothing to do since empty string is valid.
        }

        if (obs instanceof final StringProperty text) {
            // New entry is too long so restore old.
            if (newValue.length() > 2) {
                text.set(oldValue);
            } else {
                newValue = newValue.toUpperCase();

                // Ensure that the user entered only valid characters.
                for (var i = 0; i < newValue.length(); ++i) {
                    final var ch = newValue.charAt(i);
                    if (!Character.isDigit(ch) && ch < 'A' || ch > 'F') {
                        text.set(oldValue);
                        return;
                    }
                }
                text.set(newValue);
            }
        }
    }

}
