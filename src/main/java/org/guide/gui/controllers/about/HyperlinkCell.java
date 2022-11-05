package org.guide.gui.controllers.about;

import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import org.guide.App;

/**
 * Table cell implementation that displays a hyperlink.
 *
 * @param <S> The type of element displayed in each row of the table.
 * @param <T> The type of element displayed in the cell.
 */
public class HyperlinkCell<S, T> extends TableCell<S, T> {

    /**
     * The hyperlink element to display.
     */
    private final Hyperlink hyperlink;

    /**
     * Creates a new Hyperlink cell.
     */
    public HyperlinkCell() {
        this.hyperlink = new Hyperlink("");
        hyperlink.setOnAction(e -> App.get().getHostServices().showDocument(hyperlink.getText()));
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            hyperlink.setText(item.toString());
            setGraphic(hyperlink);
        } else {
            setGraphic(null);
        }
    }

}
