package org.guide.gui.controllers;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.guide.emulator.Emulator;
import org.guide.gameboy.processor.interrupts.memory.MemoryChangedEvent;
import org.guide.gui.control.memory.MemorySection;
import org.guide.gui.control.memory.MemoryTableCell;
import org.guide.gui.control.memory.MemoryTableEntry;
import org.guide.util.AddressUtils;
import org.guide.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * The controller implementation for the memory viewer.
 */
public class MemoryViewerController extends AbstractController {

    /**
     * The label for the "AF" register.
     */
    @FXML
    private Label lblRegAF;

    /**
     * The label for the "BC" register.
     */
    @FXML
    private Label lblRegBC;

    /**
     * The label for the "DE" register.
     */
    @FXML
    private Label lblRegDE;

    /**
     * The label for the "HL" register.
     */
    @FXML
    private Label lblRegHL;

    /**
     * The label for the "SP" register.
     */
    @FXML
    private Label lblRegSP;

    /**
     * The label for the "PC" register.
     */
    @FXML
    private Label lblRegPC;

    /**
     * The label for the "IME" register.
     */
    @FXML
    private Label lblRegIME;

    /**
     * The label for the "IE" register.
     */
    @FXML
    private Label lblRegIE;

    /**
     * The label for the "IF" register.
     */
    @FXML
    private Label lblRegIF;

    /**
     * The label for the "KEY1" register.
     */
    @FXML
    private Label lblRegKEY1;

    /**
     * The label for the "LCDC" register.
     */
    @FXML
    private Label lblRegLCDC;

    /**
     * The label for the "STAT" register.
     */
    @FXML
    private Label lblRegSTAT;

    /**
     * The label for the "LY" register.
     */
    @FXML
    private Label lblRegLY;

    /**
     * The label for the "SCX" register.
     */
    @FXML
    private Label lblRegSCX;

    /**
     * The label for the "SCY" register.
     */
    @FXML
    private Label lblRegSCY;

    /**
     * The label for the "WX" register.
     */
    @FXML
    private Label lblRegWX;

    /**
     * The label for the "WY" register.
     */
    @FXML
    private Label lblRegWY;

    /**
     * The label for the "LYC" register.
     */
    @FXML
    private Label lblRegLYC;

    /**
     * The label for the "TAC" register.
     */
    @FXML
    private Label lblRegTAC;

    /**
     * The label for the "DIV" register.
     */
    @FXML
    private Label lblRegDIV;

    /**
     * The label for the "TIMA" register.
     */
    @FXML
    private Label lblRegTIMA;

    /**
     * The label for the "TMA" register.
     */
    @FXML
    private Label lblRegTMA;

    /**
     * The table view for the memory map.
     */
    @FXML
    private TableView<MemoryTableEntry> tblMemory;

    /**
     * The container the memory map overview.
     */
    @FXML
    private VBox jumpContainer;

    /**
     * The refresh timer.
     */
    private final AnimationTimer refreshTimer;

    public MemoryViewerController(Emulator emulator) throws Exception {
        super(new Stage(), emulator, ROOT_PATH + "/MemoryViewer.fxml", DEFAULT_STYLE);
        stage.setTitle("Memory Viewer");

        initializeMemoryTableAndSegments();

        this.refreshTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                refresh();
            }
        };
        refreshTimer.start();
    }

    /**
     * Performs cleanup operations in preparation to close.
     */
    public void cleanupForClose() {
        refreshTimer.stop();
    }

    /**
     * Initializes the memory table and segments
     */
    private void initializeMemoryTableAndSegments() {
        // Populate the table with data rows.
        for (var address = 0x0000; address < 0xFFFF; address += MemoryTableEntry.NUM_VALUES) {
            tblMemory.getItems().add(new MemoryTableEntry(address));
        }

        // Initialize the table columns.
        for (var i = 0; i < tblMemory.getColumns().size(); ++i) {
            final var valueIndex = i - 1;

            @SuppressWarnings("unchecked") final var col = (TableColumn<MemoryTableEntry, String>) tblMemory.getColumns().get(i);

            if (i == 0) {
                col.setCellValueFactory(data -> data.getValue().baseAddressProperty());
            } else {
                col.setCellValueFactory(data -> data.getValue().valueProperty(valueIndex));
                col.setCellFactory(data -> new MemoryTableCell(valueIndex));
            }
        }

        // Apply styling to the memory map overview.
        for (var section : MemorySection.values()) {
            final var label = new Label(section.toString());
            label.getStyleClass().add(section.styleClass());
            label.setOnMouseClicked(e -> jumpToMemory(section.startAddress()));

            jumpContainer.getChildren().add(label);
        }
    }

    /**
     * Causes the memory viewer to jump to the specified address.
     *
     * @param address The address to jump to.
     */
    private void jumpToMemory(int address) {
        final var row = address / MemoryTableEntry.NUM_VALUES;

        tblMemory.getSelectionModel().select(row);
        tblMemory.scrollTo(row);
    }

    /**
     * Refreshes the UI components with the most recent values from the emulator.
     */
    private void refresh() {
        final var gameboy = emulator.getGameBoy();

        final var processor = gameboy.getProcessor();
        lblRegAF.setText(StringUtils.getHex16(processor.getAF().read()));
        lblRegBC.setText(StringUtils.getHex16(processor.getBC().read()));
        lblRegDE.setText(StringUtils.getHex16(processor.getDE().read()));
        lblRegHL.setText(StringUtils.getHex16(processor.getHL().read()));
        lblRegSP.setText(StringUtils.getHex16(processor.getSP().read()));
        lblRegPC.setText(StringUtils.getHex16(processor.getPC().read()));

        final var interrupts = processor.getInterrupts();
        lblRegIME.setText(interrupts.getMasterEnabled() ? "ON" : "OFF");

        final var memory = gameboy.getMemory();
        lblRegIE.setText(StringUtils.getHex8(memory.get(AddressUtils.IE)));
        lblRegIF.setText(StringUtils.getHex8(memory.get(AddressUtils.IF)));
        lblRegKEY1.setText(StringUtils.getHex8(memory.get(AddressUtils.KEY1)));

        lblRegLCDC.setText(StringUtils.getHex8(memory.get(AddressUtils.LCDC)));
        lblRegSTAT.setText(StringUtils.getHex8(memory.get(AddressUtils.STAT)));
        lblRegLY.setText(StringUtils.getHex8(memory.get(AddressUtils.LY)));
        lblRegLYC.setText(StringUtils.getHex8(memory.get(AddressUtils.LYC)));
        lblRegSCX.setText(StringUtils.getHex8(memory.get(AddressUtils.SCX)));
        lblRegSCY.setText(StringUtils.getHex8(memory.get(AddressUtils.SCY)));
        lblRegWX.setText(StringUtils.getHex8(memory.get(AddressUtils.WX)));
        lblRegWY.setText(StringUtils.getHex8(memory.get(AddressUtils.WY)));

        lblRegTAC.setText(StringUtils.getHex8(memory.get(AddressUtils.TAC)));
        lblRegDIV.setText(StringUtils.getHex8(memory.get(AddressUtils.DIV)));
        lblRegTIMA.setText(StringUtils.getHex8(memory.get(AddressUtils.TIMA)));
        lblRegTMA.setText(StringUtils.getHex8(memory.get(AddressUtils.TMA)));

        final var memoryMap = memory.getMemoryMap();

        var address = 0x0000;
        for (var entry : tblMemory.getItems()) {
            for (var i = 0; i < MemoryTableEntry.NUM_VALUES; ++i) {
                entry.valueProperty(i).set(StringUtils.getHex8(memoryMap[address++]));
            }
        }
    }

    /**
     * Callback to run whenever memory is updated.
     *
     * @param e The memory changed event.
     */
    private void onMemoryUpdated(MemoryChangedEvent e) {
        final var values = e.memory().getMemoryMap();

        var row = e.address() >> 4;
        var col = e.address() % 0x10;

        final var it = tblMemory.getItems().listIterator(row);

        var entry = it.next();
        for (var i = e.address(); i < e.address() + e.length(); ++i) {
            if (col > 0xF) {
                col = 0;
                entry = it.next();
            }
            entry.valueProperty(col).set(StringUtils.getHex8(values[i]));
            col++;
        }
    }

    /**
     * Callback to run when the breakpoints menu is clicked.
     *
     * @param e The event.
     */
    @FXML
    public void onBreakpointsMenuClicked(ActionEvent e) {

    }

    @FXML
    public void onDumpMemory() {
        // Pause the emulator while the user chooses a file.
        final var wasPaused = emulator.isPaused();
        emulator.setPaused(true);

        // Create the file chooser.
        final var fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Dump", "*.dmp"));
        fileChooser.setTitle("Save memory dump");

        // Write the selected file to memory.
        final var file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            final var data = emulator.getGameBoy().getMemory().getMemoryMap();
            try {
                Files.write(file.toPath(), data);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Restore the emulator's pause state.
        emulator.setPaused(wasPaused);
    }
}
