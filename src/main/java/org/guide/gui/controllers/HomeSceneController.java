package org.guide.gui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.StatusBar;
import org.guide.emulator.Emulator;
import org.guide.emulator.event.CartridgeLoadedEvent;
import org.guide.emulator.event.CartridgeUnloadedEvent;
import org.guide.gameboy.cartridge.CartridgeException;
import org.guide.gui.controllers.about.AboutController;
import org.guide.gui.controllers.settings.SettingsController;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * The controller implementation for the "Home" screen.
 *
 * @author Brendan Jones
 */
public class HomeSceneController extends AbstractController {

    /**
     * The controller for the "memory viewer" window.
     */
    private MemoryViewerController memoryViewer;

    /**
     * The controller for the "settings" window.
     */
    private SettingsController settings;

    /**
     * The controller for the "about" window.
     */
    private AboutController about;

    /**
     * The controller for the "cartridge" window.
     */
    private CartridgeController cartridge;

    /**
     * The file chooser used for loading ROMs.
     */
    private final FileChooser fileChooser = new FileChooser();

    @FXML
    private CheckMenuItem chkPauseResume;

    @FXML
    private MenuItem mnuCartridgeHeader;

    @FXML
    private Menu mnuRecentROMs;

    @FXML
    private Menu mnuQuickSlots;

    @FXML
    private ColorPicker clrMessageFontColor;

    @FXML
    private StatusBar stbStatus;

    /**
     * Creates a new home scene controller instance.
     *
     * @param stage    The stage the controller is responsible for.
     * @param emulator The emulator instance.
     * @throws Exception If the layout or style file couldn't be loaded.
     */
    public HomeSceneController(Stage stage, Emulator emulator) throws Exception {
        super(stage, emulator, ROOT_PATH + "/HomeScene.fxml", DEFAULT_STYLE);

        emulator.bindCartridgeLoadedEvent(this::onCartridgeLoaded);
        emulator.bindCartridgeUnloadedEvent(this::onCartridgeUnloaded);
        emulator.getConfig().quickSlotProperty().addListener((obs, oldValue, newValue) -> onQuickSlotChanged());

        chkPauseResume.selectedProperty().bindBidirectional(emulator.isPausedProperty());

        stage.setOnCloseRequest(this::onCloseRequested);

        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ROMs", "*.gb", "*.gbc"));
        fileChooser.setTitle("Select ROM");

        updateRecentROMsMenu();
        onQuickSlotChanged();

        stage.setTitle("GameBoy Emulator");
        stage.show();
    }

    /**
     * Callback to run when the loaded cartridge changes.
     */
    private void onLoadedCartridgeChanged() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::onLoadedCartridgeChanged);
            return;
        }

        updateCartridgeHeaderMenu();
        if (emulator.isCartridgeLoaded()) {
            stbStatus.setText("Running Cartridge: " + emulator.getCartridgePath().getFileName().toString());
        } else {
            stbStatus.setText("No cartridge loaded");
        }
    }

    /**
     * Callback to run when the quick slot changes.
     */
    private void onQuickSlotChanged() {
        final var selected = emulator.getConfig().getQuickSlot() - 1;

        final var items = mnuQuickSlots.getItems();
        items.forEach(item -> ((RadioMenuItem) item).setSelected(items.indexOf(item) == selected));
    }

    /**
     * Updates the "recent ROMs" menu.
     */
    private void updateRecentROMsMenu() {
        final var items = mnuRecentROMs.getItems();
        items.clear();

        final var config = emulator.getConfig();

        final var recent = new ArrayList<>(config.getRecentROMs());
        recent.sort(Comparator.naturalOrder());

        // Populate the "Recent ROMs" menu.
        for (var name : recent) {
            final var path = Paths.get(name);

            name = path.getFileName().toString();

            final var extensionIndex = name.lastIndexOf('.');
            if (extensionIndex != -1) {
                name = name.substring(0, extensionIndex);
            }

            final var item = new MenuItem(name);
            item.setOnAction(e -> requestLoadROM(path));
            items.add(item);
        }

        // The menu should be disabled if no recent roms were selected.
        mnuRecentROMs.setDisable(recent.isEmpty());

        // If at least one recently used ROM is available, show the 'clear' button.
        if (!recent.isEmpty()) {
            items.add(new SeparatorMenuItem());

            final var clearItem = new MenuItem("Clear Recent ROMs");
            clearItem.setOnAction(e -> {
                config.getRecentROMs().clear();
                try {
                    config.save();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                updateRecentROMsMenu();
            });
            items.add(clearItem);
        }
    }

    /**
     * Updates the cartridge header menu.
     */
    private void updateCartridgeHeaderMenu() {
        final var isLoaded = emulator.isCartridgeLoaded();

        mnuCartridgeHeader.setDisable(!isLoaded);

        if (cartridge != null) {
            if (isLoaded) {
                cartridge.updateDetails();
            } else {
                cartridge.requestClose();
            }
        }
    }

    /**
     * Requests the application to shut down.
     *
     * @return Whether the application should shut down.
     */
    private boolean requestShutdown() {
        var shouldShutdown = true;
        if (emulator.isCartridgeLoaded()) {

            // Create an alert to show the user.
            final var alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Are you sure you want to quit?");
            alert.setHeaderText(null);
            alert.setContentText("A ROM is currently running, are you sure you want to quit?");

            // Pause the emulator until the user makes a decision, and then restore it to its
            // original state afterwards.
            final var wasPaused = emulator.isPaused();
            emulator.setPaused(true);

            final var choice = alert.showAndWait();
            if (choice.isEmpty() || choice.get() != ButtonType.OK) {
                shouldShutdown = false;
            }

            // Restore the previous pause state.
            emulator.setPaused(wasPaused);
        }

        if (shouldShutdown) {
            emulator.stop();
            Platform.exit();
        }

        return shouldShutdown;
    }

    /**
     * Callback to run when the user attempts to close the window.
     *
     * @param e The event.
     */
    private void onCloseRequested(WindowEvent e) {
        if (!requestShutdown()) {
            e.consume();
        }
    }

    /**
     * Callback to run when a new cartridge is loaded.
     *
     * @param e The load event.
     */
    private void onCartridgeLoaded(CartridgeLoadedEvent e) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> onCartridgeLoaded(e));
            return;
        }

        final var ex = e.error();
        if (ex == null) {
            updateRecentROMsMenu();
            onLoadedCartridgeChanged();
        } else {
            final var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Failed to load ROM");

            if (ex instanceof FileNotFoundException || ex instanceof NoSuchFileException) {
                alert.setHeaderText("File does not exist");
            } else if (e.error() instanceof CartridgeException) {
                alert.setHeaderText(e.error().getMessage());
            }
            alert.setContentText("Could not load ROM: " + e.path());
            alert.show();
        }
    }

    /**
     * Callback to run when the emulator unloads a cartridge.
     *
     * @param e The unload event.
     */
    private void onCartridgeUnloaded(CartridgeUnloadedEvent e) {
        onLoadedCartridgeChanged();
    }

    /**
     * Attempts to load a ROM from the specified path into the emulator.
     *
     * @param path The ROM path.
     */
    public void requestLoadROM(Path path) {
        emulator.setCartridge(path);
        stbStatus.setText("Running Cartridge: " + path.getFileName().toString());
    }

    @FXML
    void onLoadROMClicked(ActionEvent event) {
        // Pause the emulator while the user selects a ROM.
        boolean wasPaused = emulator.isPaused();
        emulator.setPaused(true);

        final var file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            fileChooser.setInitialDirectory(file.getParentFile());
            requestLoadROM(file.toPath());
        }

        // Restore the previous pause state.
        emulator.setPaused(wasPaused);
    }

    @FXML
    void onQuickSaveClicked(ActionEvent event) {
        emulator.requestQuickSave();
    }

    @FXML
    void onQuickLoadClicked(ActionEvent event) {
        emulator.requestQuickLoad();
    }

    @FXML
    void onResetClicked(ActionEvent event) {
        emulator.reset();
    }

    @FXML
    void onQuickSlotClicked(ActionEvent event) {
        final var slot = mnuQuickSlots.getItems().indexOf(event.getSource());
        if (slot == -1) {
            throw new IllegalStateException();
        } else {
            emulator.getConfig().setQuickSlot(slot + 1);
        }
    }

    @FXML
    void onShutdownClicked(ActionEvent event) {
        requestShutdown();
    }

    @FXML
    void onSettingsClicked(ActionEvent event) {
        try {
            if (settings == null) {
                this.settings = new SettingsController(emulator);
                settings.getStage().setOnCloseRequest(e -> this.settings = null);
            }

            final var settingsStage = settings.getStage();
            settingsStage.show();
            settingsStage.setIconified(false);
            settingsStage.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onShowMemoryViewer(ActionEvent event) {
        try {
            if (memoryViewer == null) {
                this.memoryViewer = new MemoryViewerController(emulator);
                memoryViewer.getStage().setOnCloseRequest(e -> {
                    memoryViewer.cleanupForClose();
                    this.memoryViewer = null;
                });
            }

            final var memoryViewerStage = memoryViewer.getStage();
            memoryViewerStage.show();
            memoryViewerStage.setIconified(false);
            memoryViewerStage.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onAboutClicked(ActionEvent event) {
        try {
            if (about == null) {
                this.about = new AboutController(emulator);
                about.getStage().setOnCloseRequest(e -> {
                    this.about = null;
                });
            }

            final var aboutStage = about.getStage();
            aboutStage.show();
            aboutStage.setIconified(false);
            aboutStage.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onShowCartridgeHeader(ActionEvent event) {
        try {
            if (cartridge == null) {
                this.cartridge = new CartridgeController(emulator);
                cartridge.getStage().setOnCloseRequest(e -> this.cartridge = null);
            }

            final var cartridgeStage = cartridge.getStage();
            cartridgeStage.show();
            cartridgeStage.setIconified(false);
            cartridgeStage.requestFocus();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
