package org.guide.gui.controllers;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.guide.emulator.Emulator;

/**
 * The controller implementation for the "Cartridge" screen. It shows the header information for the currently loaded
 * cartridge.
 *
 * @author Brendan Jones
 */
public class CartridgeController extends AbstractController {

    /**
     * The number of bytes in a kilobyte.
     */
    private static final int BYTES_PER_KB = 1024;

    /**
     * The number of bytes in a megabyte.
     */
    private static final int BYTES_PER_MB = BYTES_PER_KB * 1024;

    @FXML
    private Label lblTitle;

    @FXML
    private Label lblPlatform;

    @FXML
    private Label lblDestination;

    @FXML
    private Label lblController;

    @FXML
    private Label lblHasRTC;

    @FXML
    private Label lblHasRumble;

    @FXML
    private Label lblHasBattery;

    @FXML
    private Label lblAvailableROM;

    @FXML
    private Label lblAvailableRAM;

    /**
     * Creates a new cartridge controller instance.
     *
     * @param emulator The emulator instance.
     * @throws Exception If the layout or style file could not be loaded.
     */
    public CartridgeController(Emulator emulator) throws Exception {
        super(new Stage(), emulator, ROOT_PATH + "/Cartridge.fxml", DEFAULT_STYLE);
        stage.setTitle("Cartridge Details");

        updateDetails();
    }

    @FXML
    void onClosePressed(ActionEvent event) {
        requestClose();
    }

    /**
     * Updates the details of the cartridge header.
     */
    public void updateDetails() {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(this::updateDetails);
            return;
        }

        final var header = emulator.getGameBoy().getCartridge().getHeader();
        if (header != null) {
            lblTitle.setText(header.getTitle());
            lblPlatform.setText(header.isCGB() ? "Game Boy Color" : "Game Boy");
            lblDestination.setText(header.getDestination().toString());
            lblController.setText(header.getMBCType().name());
            lblHasRTC.setText(header.hasRTC() ? "Yes" : "No");
            lblHasRumble.setText(header.hasRumble() ? "Yes" : "No");
            lblHasBattery.setText(header.hasBattery() ? "Yes" : "No");
            lblAvailableROM.setText(calculateBanksString(header.getROMSize(), 0x4000));
            lblAvailableRAM.setText(calculateBanksString(header.getRAMSize(), 0x2000));
        }
    }

    /**
     * Generates the memory bank strings.
     *
     * @param size     The total memory size.
     * @param bankSize The size of each memory bank.
     * @return The memory bank string.
     */
    private static String calculateBanksString(int size, int bankSize) {
        final var b = new StringBuilder();
        if (size >= BYTES_PER_MB) {
            b.append(String.format("%.1f", (float) size / BYTES_PER_MB)).append("MB");
        } else if (size > BYTES_PER_KB) {
            b.append(String.format("%.1f", (float) size / BYTES_PER_KB)).append("KB");
        } else {
            b.append(size).append(" bytes");
        }

        final var numBanks = size / bankSize;
        b.append(" (").append(numBanks).append(numBanks != 1 ? " banks)" : " bank)");

        return b.toString();
    }

}
