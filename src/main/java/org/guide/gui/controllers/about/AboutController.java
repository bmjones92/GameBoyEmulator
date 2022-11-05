package org.guide.gui.controllers.about;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import org.guide.emulator.Emulator;
import org.guide.gui.controllers.AbstractController;

/**
 * The controller implementation for the "About" screen.
 *
 * @author Brendan Jones
 */
public class AboutController extends AbstractController {

    /**
     * The list of third-party software that was used by the emulator.
     */
    private static final ObservableList<ThirdPartySoftware> software = FXCollections.observableArrayList(
            new ThirdPartySoftware("Logback", "1.4.4", "https://logback.qos.ch/"),
            new ThirdPartySoftware("SLF4J", "2.0.3", "https://www.slf4j.org/"),
            new ThirdPartySoftware("GSON", "2.10", "https://github.com/google/gson"),
            new ThirdPartySoftware("ControlsFX", "11.1.2", "https://github.com/controlsfx/controlsfx"),
            new ThirdPartySoftware("FX-GSON", "4.0.1", "https://github.com/joffrey-bion/fx-gson"),
            new ThirdPartySoftware("LWJGL", "3.3.1", "https://lwjgl.org"),
            new ThirdPartySoftware("LWJGL-GLFW", "3.3.1", "https://lwjgl.org"),
            new ThirdPartySoftware("LWJGL-OpenGL", "3.3.1", "https://lwjgl.org"),
            new ThirdPartySoftware("LWJGL-STB", "3.3.1", "https://lwjgl.org")
    );

    @FXML
    private TableView<ThirdPartySoftware> tblSoftware;

    public AboutController(Emulator emulator) throws Exception {
        super(new Stage(), emulator, ROOT_PATH + "/About.fxml", DEFAULT_STYLE);
        stage.setTitle("Third Party Software");
        stage.setResizable(false);

        tblSoftware.setItems(software);

        // Columns need to be hard-coded due to a change in how JavaFX handles table columns.
        final var cols = tblSoftware.getColumns();
        cols.clear();

        // Initialize the name column.
        final var colName = new TableColumn<ThirdPartySoftware, String>("Name");
        colName.setCellValueFactory(value -> value.getValue().nameProperty());
        cols.add(colName);

        // Initialize the version column.
        final var colVersion = new TableColumn<ThirdPartySoftware, String>("Version");
        colVersion.setCellValueFactory(value -> value.getValue().versionProperty());
        cols.add(colVersion);

        // Initialize the website column.
        final var colWebsite = new TableColumn<ThirdPartySoftware, String>("Website");
        colWebsite.setCellValueFactory(value -> value.getValue().websiteProperty());
        colWebsite.setCellFactory(value -> new HyperlinkCell<>());
        cols.add(colWebsite);
    }

    @FXML
    void onCloseRequested(ActionEvent e) {
        stage.close();
    }

}
