package org.guide.gui.control;

import javafx.scene.control.ComboBox;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * ComboBox lookup implementation that selects the shortest item matching the input prefix.
 *
 * @author Brendan Jones
 */
public class ShortestMatchLookup implements BiFunction<ComboBox, String, Optional> {

    @Override
    public Optional<?> apply(ComboBox tComboBox, String s) {
        var resultLength = Integer.MAX_VALUE;

        s = s.toLowerCase();

        var result = Optional.empty();
        for (var item : tComboBox.getItems()) {
            final var text = item.toString();

            // Searching for first shortest matching prefix, so don't bother checking any items not shorter than the
            // currently found match.
            if (text.length() >= resultLength) {
                continue;
            }

            if (text.toLowerCase().startsWith(s)) {
                result = Optional.of(item);
                resultLength = text.length();
            }
        }
        return result;
    }

}
