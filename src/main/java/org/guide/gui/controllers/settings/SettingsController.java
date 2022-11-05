package org.guide.gui.controllers.settings;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import org.controlsfx.control.PrefixSelectionComboBox;
import org.guide.emulator.Emulator;
import org.guide.emulator.input.InputAction;
import org.guide.gameboy.input.GameboyButton;
import org.guide.gui.control.ShortestMatchLookup;
import org.guide.gui.controllers.AbstractController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * The controller implementation for the "Settings" window.
 */
public class SettingsController extends AbstractController {

    /**
     * Positions of each button on the game boy control overlay. Used for highlighting buttons that are selected.
     */
    private static final Map<GameboyButton, Point2D> HIGHLIGHT_POSITIONS = new HashMap<>();

    /**
     * The search lookup functionality to use for the input binding combo boxes.
     */
    private static final ShortestMatchLookup SEARCH_LOOKUP = new ShortestMatchLookup();

    static {
        HIGHLIGHT_POSITIONS.put(GameboyButton.UP, new Point2D(62.0, 308.0));
        HIGHLIGHT_POSITIONS.put(GameboyButton.RIGHT, new Point2D(87.0, 337.0));
        HIGHLIGHT_POSITIONS.put(GameboyButton.DOWN, new Point2D(62.0, 365.0));
        HIGHLIGHT_POSITIONS.put(GameboyButton.LEFT, new Point2D(35.0, 337.0));
        HIGHLIGHT_POSITIONS.put(GameboyButton.SELECT, new Point2D(110.0, 415.0));
        HIGHLIGHT_POSITIONS.put(GameboyButton.START, new Point2D(162.0, 415.0));
        HIGHLIGHT_POSITIONS.put(GameboyButton.B, new Point2D(211.0, 345.0));
        HIGHLIGHT_POSITIONS.put(GameboyButton.A, new Point2D(260.0, 320.0));
    }

    @FXML
    private CheckBox chkPreserveAspectRatio;

    @FXML
    private CheckBox chkPauseOnFocusLost;

    @FXML
    private Rectangle overlayBackground;

    @FXML
    private Circle overlayHighlight;

    @FXML
    private Slider sldDeadzone;

    @FXML
    private CheckBox chkMessageEnable;

    @FXML
    private ColorPicker clrMessageFontColor;

    @FXML
    private PrefixSelectionComboBox<Integer> cmbMessageFontSize;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyUp;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyRight;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyDown;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyLeft;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeySelect;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyStart;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyB;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyA;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyPauseResume;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyStepForward;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyStepFrame;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyQuickSave;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyQuickLoad;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyPrevQuickslot;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputKeyNextQuickslot;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadUp;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadRight;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadDown;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadLeft;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadSelect;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadStart;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadB;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadA;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadPauseResume;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadStepForward;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadStepFrame;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadQuickSave;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadQuickLoad;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadPrevQuickslot;

    @FXML
    private PrefixSelectionComboBox<InputMappingEntry> cmbInputPadNextQuickslot;

    @FXML
    private Button btnOK;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnApply;

    private final ObservableList<InputMappingEntry> gamepadButtons;

    private final ObservableList<InputMappingEntry> keyboardButtons;

    public SettingsController(Emulator emulator) throws Exception {
        super(new Stage(), emulator, ROOT_PATH + "/Settings.fxml", DEFAULT_STYLE);
        stage.setTitle("Emulator Configuration");
        stage.setResizable(false);

        setHighlightedButton(null);

        // Initialize the list of mapped inputs.
        this.gamepadButtons = loadInputMappings("/config/GamepadBindingNames.txt");
        gamepadButtons.add(0, InputMappingEntry.NULL);

        this.keyboardButtons = loadInputMappings("/config/KeyBindingNames.txt");
        keyboardButtons.add(0, InputMappingEntry.NULL);

        Arrays.asList(
                cmbInputPadUp, cmbInputPadRight, cmbInputPadDown, cmbInputPadLeft,
                cmbInputPadStart, cmbInputPadSelect, cmbInputPadA, cmbInputPadB,
                cmbInputPadStepForward, cmbInputPadPauseResume, cmbInputPadStepFrame,
                cmbInputPadQuickSave, cmbInputPadQuickLoad,
                cmbInputPadPrevQuickslot, cmbInputPadNextQuickslot
        ).forEach(cmb -> {
            cmb.setLookup(SEARCH_LOOKUP);
            cmb.setItems(gamepadButtons);
        });

        Arrays.asList(
                cmbInputKeyUp, cmbInputKeyRight, cmbInputKeyDown, cmbInputKeyLeft,
                cmbInputKeyStart, cmbInputKeySelect, cmbInputKeyA, cmbInputKeyB,
                cmbInputKeyStepForward, cmbInputKeyPauseResume, cmbInputKeyStepFrame,
                cmbInputKeyQuickSave, cmbInputKeyQuickLoad,
                cmbInputKeyPrevQuickslot, cmbInputKeyNextQuickslot
        ).forEach(cmb -> {
            cmb.setLookup(SEARCH_LOOKUP);
            cmb.setItems(keyboardButtons);
        });

        sldDeadzone.valueChangingProperty().addListener((observable, oldValue, newValue) -> enableApplyButton(null));

        cmbMessageFontSize.getItems().addAll(8, 9, 10, 11, 12, 14, 16, 18, 20, 22, 24);

        initializeSettingsFromConfig();
    }

    /**
     * Loads input mappings from the specified file.
     *
     * @param file The file name.
     * @return The list of input mappings, or an empty list if the file could not be loaded.
     */
    private ObservableList<InputMappingEntry> loadInputMappings(String file) {
        try (final var in = getClass().getResourceAsStream(file)) {
            requireNonNull(in);

            final var reader = new BufferedReader(new InputStreamReader(in));

            return reader.lines()
                    .map(line -> line.split(":"))
                    .filter(line -> line.length == 2)
                    .map(tokens -> new InputMappingEntry(Integer.parseInt(tokens[0]), tokens[1]))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
        } catch (Exception e) {
            return FXCollections.emptyObservableList();
        }
    }

    /**
     * Find an input mapping entry for a given ID.
     *
     * @param entries The list of entries to search.
     * @param id      The id to search for.
     * @return
     */
    private InputMappingEntry findEntryForID(ObservableList<InputMappingEntry> entries, int id) {
        for (var entry : entries) {
            if (entry.getID() == id) {
                return entry;
            }
        }
        return InputMappingEntry.NULL;
    }

    /**
     * Highlights the specified button on the Game Boy overlay.
     *
     * @param button The button to highlight, or null to display no highlight.
     */
    private void setHighlightedButton(GameboyButton button) {
        overlayHighlight.setVisible(button != null);
        if (overlayHighlight.isVisible()) {
            setHighlightPosition(HIGHLIGHT_POSITIONS.get(button));
        } else {
            setHighlightPosition(null);
        }
    }

    /**
     * Sets the position of the highlight.
     *
     * @param position The highlight position.
     */
    private void setHighlightPosition(Point2D position) {
        Shape clip = new Rectangle(overlayBackground.getWidth(), overlayBackground.getHeight());
        if (position != null) {
            overlayHighlight.setCenterX(position.getX());
            overlayHighlight.setCenterY(position.getY());

            clip = Shape.subtract(clip, new Circle(position.getX(), position.getY(), overlayHighlight.getRadius()));
        }
        overlayBackground.setClip(clip);
    }

    /**
     * Initializes the settings from the emulator configuration.
     */
    private void initializeSettingsFromConfig() {
        final var config = emulator.getConfig();

        chkPreserveAspectRatio.setSelected(config.getPreserveAspectRatio());
        chkPauseOnFocusLost.setSelected(config.getPauseOnFocusLost());
        sldDeadzone.setValue(config.getDeadzone());

        chkMessageEnable.setSelected(config.getEnableMessageOverlay());
        clrMessageFontColor.setValue(config.getMessageFontColor());
        cmbMessageFontSize.getSelectionModel().select((Integer) config.getMessageFontSize());

        for (var action : InputAction.values()) {
            final var input = config.getInputBinding(action);

            final var keyboardSelection = findEntryForID(keyboardButtons, input.getKey());
            final var gamepadSelection = findEntryForID(gamepadButtons, input.getButton());

            switch (action) {
                case GAMEBOY_UP -> {
                    cmbInputKeyUp.getSelectionModel().select(keyboardSelection);
                    cmbInputPadUp.getSelectionModel().select(gamepadSelection);
                }
                case GAMEBOY_RIGHT -> {
                    cmbInputKeyRight.getSelectionModel().select(keyboardSelection);
                    cmbInputPadRight.getSelectionModel().select(gamepadSelection);
                }
                case GAMEBOY_DOWN -> {
                    cmbInputKeyDown.getSelectionModel().select(keyboardSelection);
                    cmbInputPadDown.getSelectionModel().select(gamepadSelection);
                }
                case GAMEBOY_LEFT -> {
                    cmbInputKeyLeft.getSelectionModel().select(keyboardSelection);
                    cmbInputPadLeft.getSelectionModel().select(gamepadSelection);
                }
                case GAMEBOY_A -> {
                    cmbInputKeyA.getSelectionModel().select(keyboardSelection);
                    cmbInputPadA.getSelectionModel().select(gamepadSelection);
                }
                case GAMEBOY_B -> {
                    cmbInputKeyB.getSelectionModel().select(keyboardSelection);
                    cmbInputPadB.getSelectionModel().select(gamepadSelection);
                }
                case GAMEBOY_START -> {
                    cmbInputKeyStart.getSelectionModel().select(keyboardSelection);
                    cmbInputPadStart.getSelectionModel().select(gamepadSelection);
                }
                case GAMEBOY_SELECT -> {
                    cmbInputKeySelect.getSelectionModel().select(keyboardSelection);
                    cmbInputPadSelect.getSelectionModel().select(gamepadSelection);
                }
                case SYSTEM_PAUSE_RESUME -> {
                    cmbInputKeyPauseResume.getSelectionModel().select(keyboardSelection);
                    cmbInputPadPauseResume.getSelectionModel().select(gamepadSelection);
                }
                case SYSTEM_STEP_FORWARD -> {
                    cmbInputKeyStepForward.getSelectionModel().select(keyboardSelection);
                    cmbInputPadStepForward.getSelectionModel().select(gamepadSelection);
                }
                case SYSTEM_STEP_FRAME -> {
                    cmbInputKeyStepFrame.getSelectionModel().select(keyboardSelection);
                    cmbInputPadStepFrame.getSelectionModel().select(gamepadSelection);
                }
                case SYSTEM_QUICK_SAVE -> {
                    cmbInputKeyQuickSave.getSelectionModel().select(keyboardSelection);
                    cmbInputPadQuickSave.getSelectionModel().select(gamepadSelection);
                }
                case SYSTEM_QUICK_LOAD -> {
                    cmbInputKeyQuickLoad.getSelectionModel().select(keyboardSelection);
                    cmbInputPadQuickLoad.getSelectionModel().select(gamepadSelection);
                }
                case SYSTEM_QUICK_PREV -> {
                    cmbInputKeyPrevQuickslot.getSelectionModel().select(keyboardSelection);
                    cmbInputPadPrevQuickslot.getSelectionModel().select(gamepadSelection);
                }
                case SYSTEM_QUICK_NEXT -> {
                    cmbInputKeyNextQuickslot.getSelectionModel().select(keyboardSelection);
                    cmbInputPadNextQuickslot.getSelectionModel().select(gamepadSelection);
                }
            }
        }
    }

    /**
     * Apply changes made through the UI to the emulator configuration.
     */
    private void applyChanges() {
        final var config = emulator.getConfig();
        config.setPreserveAspectRatio(chkPreserveAspectRatio.isSelected());
        config.setPauseOnFocusLost(chkPauseOnFocusLost.isSelected());
        config.setDeadzone((float) sldDeadzone.getValue());

        config.setEnableMessageOverlay(chkMessageEnable.isSelected());
        config.setMessageFontColor(clrMessageFontColor.getValue());
        config.setMessageFontSize(cmbMessageFontSize.getSelectionModel().getSelectedItem());

        for (var type : InputAction.values()) {
            final var input = config.getInputBinding(type);
            switch (type) {
                case GAMEBOY_UP -> {
                    input.setKey(cmbInputKeyUp.getValue().getID());
                    input.setButton(cmbInputPadUp.getValue().getID());
                }
                case GAMEBOY_RIGHT -> {
                    input.setKey(cmbInputKeyRight.getValue().getID());
                    input.setButton(cmbInputPadRight.getValue().getID());
                }
                case GAMEBOY_DOWN -> {
                    input.setKey(cmbInputKeyDown.getValue().getID());
                    input.setButton(cmbInputPadDown.getValue().getID());
                }
                case GAMEBOY_LEFT -> {
                    input.setKey(cmbInputKeyLeft.getValue().getID());
                    input.setButton(cmbInputPadLeft.getValue().getID());
                }
                case GAMEBOY_A -> {
                    input.setKey(cmbInputKeyA.getValue().getID());
                    input.setButton(cmbInputPadA.getValue().getID());
                }
                case GAMEBOY_B -> {
                    input.setKey(cmbInputKeyB.getValue().getID());
                    input.setButton(cmbInputPadB.getValue().getID());
                }
                case GAMEBOY_START -> {
                    input.setKey(cmbInputKeyStart.getValue().getID());
                    input.setButton(cmbInputPadStart.getValue().getID());
                }
                case GAMEBOY_SELECT -> {
                    input.setKey(cmbInputKeySelect.getValue().getID());
                    input.setButton(cmbInputPadSelect.getValue().getID());
                }
                case SYSTEM_PAUSE_RESUME -> {
                    input.setKey(cmbInputKeyPauseResume.getValue().getID());
                    input.setButton(cmbInputPadPauseResume.getValue().getID());
                }
                case SYSTEM_STEP_FORWARD -> {
                    input.setKey(cmbInputKeyStepForward.getValue().getID());
                    input.setButton(cmbInputPadStepForward.getValue().getID());
                }
                case SYSTEM_STEP_FRAME -> {
                    input.setKey(cmbInputKeyStepFrame.getValue().getID());
                    input.setButton(cmbInputPadStepFrame.getValue().getID());
                }
                case SYSTEM_QUICK_SAVE -> {
                    input.setKey(cmbInputKeyQuickSave.getValue().getID());
                    input.setButton(cmbInputPadQuickSave.getValue().getID());
                }
                case SYSTEM_QUICK_LOAD -> {
                    input.setKey(cmbInputKeyQuickLoad.getValue().getID());
                    input.setButton(cmbInputPadQuickLoad.getValue().getID());
                }
                case SYSTEM_QUICK_PREV -> {
                    input.setKey(cmbInputKeyPrevQuickslot.getValue().getID());
                    input.setButton(cmbInputPadPrevQuickslot.getValue().getID());
                }
                case SYSTEM_QUICK_NEXT -> {
                    input.setKey(cmbInputKeyNextQuickslot.getValue().getID());
                    input.setButton(cmbInputPadNextQuickslot.getValue().getID());
                }
            }
        }

        try {
            config.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void enableApplyButton(ActionEvent e) {
        btnApply.setDisable(false);
    }

    @FXML
    void onInputBindingMouseExited(MouseEvent event) {
        setHighlightedButton(null);
    }

    @FXML
    void onInputAMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.A);
    }

    @FXML
    void onInputBMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.B);
    }

    @FXML
    void onInputDownMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.DOWN);
    }

    @FXML
    void onInputLeftMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.LEFT);
    }

    @FXML
    void onInputRightMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.RIGHT);
    }

    @FXML
    void onInputSelectMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.SELECT);
    }

    @FXML
    void onInputStartMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.START);
    }

    @FXML
    void onInputUpMouseEntered(MouseEvent event) {
        setHighlightedButton(GameboyButton.UP);
    }

    @FXML
    public void onOKPressed(ActionEvent e) {
        applyChanges();
        stage.close();
    }

    @FXML
    public void onCancelPressed(ActionEvent e) {
        stage.close();
    }

    @FXML
    public void onApplyPressed(ActionEvent e) {
        applyChanges();
        btnApply.setDisable(true);
    }

}
