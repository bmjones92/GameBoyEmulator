package org.guide.gui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.guide.emulator.Emulator;

import static java.util.Objects.requireNonNull;

/**
 * Base implementation for all controllers.
 *
 * @author Brendan Jones
 */
public abstract class AbstractController {

    /**
     * The root path for all FXML files.
     */
    protected static final String ROOT_PATH = "fxml";

    /**
     * The path to the default style sheet.
     */
    protected static final String DEFAULT_STYLE = ROOT_PATH + "/Style.css";

    /**
     * The stage being controlled by this controller.
     */
    protected final Stage stage;

    /**
     * The emulator instance.
     */
    protected final Emulator emulator;

    /**
     * Creates a new abstract controller instance.
     *
     * @param stage      The stage the controller is responsible for.
     * @param emulator   The emulator instance.
     * @param layoutPath The path to the layout file.
     * @param stylePath  The path to the style file.
     * @throws Exception If the layout or style could not be loaded.
     */
    public AbstractController(Stage stage, Emulator emulator, String layoutPath, String stylePath) throws Exception {
        this.stage = requireNonNull(stage, "Stage cannot be null");
        this.emulator = requireNonNull(emulator, "Emulator cannot be null");

        final var loader = new FXMLLoader();
        loader.setController(this);

        final var root = loader.<Parent>load(getClass().getResourceAsStream("/" + layoutPath));
        root.getStylesheets().add(getClass().getResource("/" + stylePath).toExternalForm());

        final var scene = new Scene(root);
        stage.setScene(scene);
        stage.sizeToScene();
    }

    /**
     * Attempts to close the stage this controller is responsible for.
     */
    public void requestClose() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::requestClose);
            return;
        }

        stage.close();
    }

    /**
     * Gets the stage this controller is responsible for.
     *
     * @return The stage.
     */
    public Stage getStage() {
        return stage;
    }

}
