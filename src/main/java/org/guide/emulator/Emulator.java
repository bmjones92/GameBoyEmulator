package org.guide.emulator;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.guide.emulator.config.EmulatorConfig;
import org.guide.emulator.event.*;
import org.guide.emulator.input.GamepadManager;
import org.guide.emulator.input.InputAction;
import org.guide.gameboy.GameBoy;
import org.guide.gameboy.input.GameboyButton;
import org.guide.util.delegate.EventDispatcher;
import org.guide.util.delegate.EventDispatcherHandle;
import org.guide.util.fps.FrameRateEvent;
import org.guide.util.fps.FrameRateTicker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * A high-level representation of an emulator.
 *
 * @author Brendan Jones
 */
public class Emulator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Emulator.class);

    /**
     * The target frequency to emulate frames at.
     */
    private static final double TARGET_FREQUENCY = 59.7;

    /**
     * The number of nanoseconds per frame.
     */
    private static final long FULL_FRAME_TIME = Math.round(1000000000L / TARGET_FREQUENCY);

    /**
     * The directory to store quick save files in.
     */
    public static final Path QUICK_SAVE_ROOT = Paths.get("config", "saves");

    /**
     * The size of a quick save file.
     */
    private static final int QUICK_SAVE_SIZE = 1024 * 512;

    /**
     * The dispatcher for cartridge load events.
     */
    private final EventDispatcher<CartridgeLoadedEvent> cartridgeLoadedEvent = new EventDispatcher<>();

    /**
     * The dispatcher for cartridge unload events.
     */
    private final EventDispatcher<CartridgeUnloadedEvent> cartridgeUnloadedEvent = new EventDispatcher<>();

    /**
     * The ticker for tracking the frame rate.
     */
    private final FrameRateTicker framerateTicker = new FrameRateTicker((int) Math.ceil(TARGET_FREQUENCY), 1000);

    /**
     * The Game Boy being emulated.
     */
    private final GameBoy gameboy = new GameBoy();

    /**
     * The configuration of the emulator.
     */
    private final EmulatorConfig config;

    /**
     * The window to render the game onto.
     */
    private final Window window = new Window();

    /**
     * Whether the emulator is running.
     */
    private boolean isRunning;

    /**
     * Whether the emulator is paused. This needs to be a boolean property so JavaFX can bind to it, this is kind of
     * messy, but works and doesn't hurt performance.
     */
    private final BooleanProperty isPaused;

    /**
     * Whether the emulator has a game cartridge loaded.
     */
    private boolean isCartridgeLoaded;

    /**
     * Whether the viewport is dirty and needs to be redrawn on the next tick.
     */
    private boolean isViewportDirty;

    /**
     * The amount of time that has passed during this frame.
     */
    private long partialFrameTime;

    /**
     * The time that the last frame was completed.
     */
    private long lastFrameTime;

    /**
     * The path of the currently loaded cartridge.
     */
    private Path cartridgePath;

    /**
     * Whether the cartridge is waiting to be loaded.
     */
    private boolean isLoadingCartridge;

    /**
     * Whether a quick-save request was made.
     */
    private boolean isQuickSaving;

    /**
     * Whether a quick-load request was made.
     */
    private boolean isQuickLoading;

    /**
     * Forces the game to execute a single instruction. Used when stepping through instructions during debugging.
     */
    private boolean forceExecuteTick;

    /**
     * Forces the game to execute a single frame. Used when stepping through frames during debugging.
     */
    private boolean forceExecuteFrame;

    /**
     * The texture handle to display the emulator output on.
     */
    private int emulatorTexture;

    /**
     * Creates a new {@code EmulatorOLD} instance.
     */
    public Emulator(EmulatorConfig config) throws Exception {
        this.config = requireNonNull(config);

        framerateTicker.bindFrameRateEvent(this::onFramerateEvent);

        GamepadManager.initialize();
        GamepadManager.bindGamepadButtonEvent(this::onGamepadButtonStateChanged);

        window.bindKeyStateEvent(this::onKeyStateChanged);
        window.bindWindowCloseEvent(this::onWindowClosed);
        window.bindWindowResizeEvent(this::onWindowResized);
        window.bindWindowRefreshEvent(this::onWindowRefreshed);

        this.isRunning = false;
        this.isPaused = new SimpleBooleanProperty(false);

        this.cartridgePath = null;
        this.isLoadingCartridge = false;
        this.isCartridgeLoaded = false;
    }

    /**
     * Initializes the emulator and starts the emulator loop. This method will not return control to the
     * caller until {@link Emulator#stop} is called.
     */
    public void start() {
        // Ensure that the emulator is not already running.
        if (isRunning) {
            throw new IllegalStateException("Emulator is already running");
        }
        this.isRunning = true;

        LOGGER.info("Initializing emulator...");
        initializeTexture();
        initializeOpenGL();
        initializeConfigurationBindings();
        LOGGER.info("Initialization complete.");

        LOGGER.info("Starting emulator loop.");
        while (isRunning) {
            final var frameStartTime = System.currentTimeMillis();
            try {
                tick();
            } catch (Exception e) {
                LOGGER.error("Error occurred during emulator tick", e);
                this.isCartridgeLoaded = false;
            }

            // Check OpenGL for errors.
            final var error = glGetError();
            if (error != GL_NO_ERROR) {
                LOGGER.error("An OpenGL error occurred: " + error);
            }

            // Sleep until it is time to start the next frame.
            sleepFor(isPaused.get() ? FULL_FRAME_TIME : FULL_FRAME_TIME - partialFrameTime);

            final var frameTime = System.currentTimeMillis() - frameStartTime;
            framerateTicker.pushFrameTime(frameTime);
        }

        shutdown();
    }

    /**
     * Stop running and prepare for shutdown.
     */
    public void stop() {
        this.isRunning = false;
    }

    /**
     * Shutdown the emulator.
     */
    private void shutdown() {
        LOGGER.info("Shutting down emulator");
        window.destroy();
    }

    /**
     * Ticks the emulator.
     */
    private void tick() {
        updateInputState();

        if (isLoadingCartridge) {
            loadCartridge();
            this.isLoadingCartridge = false;
        }

        if (isQuickSaving) {
            quickSave();
            this.isQuickSaving = false;
        }

        if (isQuickLoading) {
            quickLoad();
            this.isQuickLoading = false;
        }

        if (isViewportDirty) {
            recalculateViewport();
            this.isViewportDirty = false;
        }

        if (isCartridgeLoaded) {
            final var currentTime = System.nanoTime();
            if (!isPaused.get() && (window.isFocused() || !config.getPauseOnFocusLost())) {
                partialFrameTime += (currentTime - lastFrameTime);
            }
            lastFrameTime = currentTime;

            if (forceExecuteFrame) {
                this.forceExecuteFrame = false;
                this.forceExecuteTick = false;
                gameboy.runFrame();
                renderFrame();
            } else if (forceExecuteTick) {
                this.forceExecuteTick = false;
                if (gameboy.tick()) {
                    renderFrame();
                }
            } else if (partialFrameTime >= FULL_FRAME_TIME) {
                // Run as many frames as necessary to catch up.
                while (partialFrameTime >= FULL_FRAME_TIME) {
                    partialFrameTime -= FULL_FRAME_TIME;
                    gameboy.runFrame();
                }

                // Render only the most recently processed frame to the display.
                renderFrame();
            }
        }
    }

    /**
     * Queries input devices and dispatches input events.
     */
    private void updateInputState() {
        glfwPollEvents();
        GamepadManager.pollEvents(config.getDeadzone());
    }

    /**
     * Reset the emulator.
     */
    public void reset() {
        if (isCartridgeLoaded) {
            gameboy.reset();
        }
    }

    /**
     * Called when a key state changes.
     *
     * @param e The event.
     */
    private void onKeyStateChanged(KeyStateEvent e) {
        final var type = config.getInputActionForKey(e.key());
        if (type != null && (type.allowRepeatEvents() || e.state() != GLFW_REPEAT)) {
            onInputStateChanged(type, e.state() != GLFW_RELEASE);
        }
    }

    /**
     * Called when a gamepad button state changes.
     *
     * @param e The event.
     */
    private void onGamepadButtonStateChanged(GamepadButtonEvent e) {
        final var action = config.getInputActionForButton(e.button());
        if (action != null) {
            onInputStateChanged(action, e.pressed());
        }
    }

    /**
     * Called when the window closes.
     *
     * @param e The window close event.
     */
    private void onWindowClosed(WindowCloseEvent e) {
        unloadCartridge();
    }

    /**
     * Called when the window resizes.
     *
     * @param e The resize event.
     */
    private void onWindowResized(WindowResizeEvent e) {
        markViewportDirty();
    }

    /**
     * Called when the window refreshes.
     *
     * @param e The refresh event.
     */
    private void onWindowRefreshed(WindowRefreshEvent e) {
        if (isViewportDirty) {
            recalculateViewport();
            renderFrame();
        }
    }

    /**
     * Called when an input state changes.
     *
     * @param action  The input action.
     * @param pressed Whether the action button is pressed or released.
     */
    private void onInputStateChanged(InputAction action, boolean pressed) {
        switch (action) {
            case GAMEBOY_A -> gameboy.getInput().setButton(GameboyButton.A, pressed);
            case GAMEBOY_B -> gameboy.getInput().setButton(GameboyButton.B, pressed);
            case GAMEBOY_START -> gameboy.getInput().setButton(GameboyButton.START, pressed);
            case GAMEBOY_SELECT -> gameboy.getInput().setButton(GameboyButton.SELECT, pressed);
            case GAMEBOY_UP -> gameboy.getInput().setButton(GameboyButton.UP, pressed);
            case GAMEBOY_RIGHT -> gameboy.getInput().setButton(GameboyButton.RIGHT, pressed);
            case GAMEBOY_DOWN -> gameboy.getInput().setButton(GameboyButton.DOWN, pressed);
            case GAMEBOY_LEFT -> gameboy.getInput().setButton(GameboyButton.LEFT, pressed);
            case SYSTEM_PAUSE_RESUME -> {
                if (pressed) {
                    setPaused(!isPaused());
                }
            }
            case SYSTEM_STEP_FORWARD -> {
                if (pressed) {
                    this.forceExecuteTick = true;
                }
            }
            case SYSTEM_STEP_FRAME -> {
                if (pressed) {
                    this.forceExecuteFrame = true;
                }
            }
            case SYSTEM_QUICK_SAVE -> {
                if (pressed) {
                    requestQuickSave();
                }
            }
            case SYSTEM_QUICK_LOAD -> {
                if (pressed) {
                    requestQuickLoad();
                }
            }
            case SYSTEM_QUICK_PREV -> {
                if (pressed) {
                    config.selectPreviousQuickSlot();
                }
            }
            case SYSTEM_QUICK_NEXT -> {
                if (pressed) {
                    config.selectNextQuickSlot();
                }
            }
        }
    }

    /**
     * Called when a frame rate event occurs.
     *
     * @param e The event
     */
    private void onFramerateEvent(FrameRateEvent e) {
        final var cart = gameboy.getCartridge();
        if (cart.isLoaded()) {
            final var cartTitle = cart.getHeader().getTitle();
            window.setTitle(String.format("%s (%d fps)", cartTitle, e.fps()));
        }
    }

    /**
     * Save the current emulator state to the currently selected quick save slot.
     */
    private void quickSave() {
        final var path = getQuickSavePath();
        if (path == null) {
            return;
        }

        final var parent = path.getParent();
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // Serialize the game boy state and write it to disk.
        try {
            final var buf = ByteBuffer.allocate(QUICK_SAVE_SIZE);
            gameboy.serialize(buf);
            buf.flip();

            final var data = new byte[buf.limit()];
            buf.get(data);

            Files.write(path, data);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Load the emulator state from the currently selected quick save slot.
     */
    private void quickLoad() {
        final var path = getQuickSavePath();
        if (path == null) {
            return;
        }

        // Deserialize the game boy state from the loaded file.
        if (Files.exists(path)) {
            try {
                final var data = Files.readAllBytes(path);
                final var buf = ByteBuffer.wrap(data);
                gameboy.deserialize(buf);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the path to the currently selected quick save slot.
     *
     * @return The path, or null if no cartridge is loaded.
     */
    private Path getQuickSavePath() {
        if (!isCartridgeLoaded) {
            return null;
        }

        final var header = gameboy.getCartridge().getHeader();

        final var title = header.getTitle().replace(' ', '_') + header.getGlobalChecksum();
        return QUICK_SAVE_ROOT.resolve(title).resolve("quicksave-" + config.getQuickSlot() + ".dat");
    }

    /**
     * Load the current cartridge from disk and boot up the game boy.
     */
    private void loadCartridge() {
        if (!isLoadingCartridge) {
            return;
        }
        this.isLoadingCartridge = false;

        Exception err = null;
        try {
            // Load the cartridge data into the Game Boy.
            final var rom = Files.readAllBytes(cartridgePath);
            gameboy.loadCartridge(rom);

            // Show the window now that we have a ROM to display.
            final var title = gameboy.getCartridge().getHeader().getTitle();
            window.setTitle(title);
            window.setVisible(true);

            this.lastFrameTime = System.nanoTime();
            this.isCartridgeLoaded = true;

            config.addRecentROM(cartridgePath.toString());
            try {
                config.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            this.isCartridgeLoaded = false;
            err = e;
        }

        cartridgeLoadedEvent.broadcast(new CartridgeLoadedEvent(cartridgePath, gameboy.getCartridge(), err));
    }

    /**
     * Creates and initializes the texture used to render the framebuffer onto the window.
     */
    private void initializeTexture() {
        LOGGER.info("Initializing emulator render texture.");

        this.emulatorTexture = glGenTextures();
        if (emulatorTexture == NULL) {
            throw new RuntimeException("Failed to allocate render texture.");
        }

        // Texture should use nearest neighbor filtering so the texture can scale without becoming blurry.
        glBindTexture(GL_TEXTURE_2D, emulatorTexture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // Initialize the texture data.
        final var fb = gameboy.getVideo().getFramebuffer();
        glTexImage2D(GL_TEXTURE_2D, 0, 3, fb.getWidth(), fb.getHeight(), 0, GL_RGB, GL_UNSIGNED_BYTE, NULL);
    }

    /**
     * Initializes the OpenGL state machine.
     */
    private void initializeOpenGL() {
        LOGGER.info("Initializing OpenGL state machine.");
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        glEnable(GL_TEXTURE_2D);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Registers callbacks to configuration properties.
     */
    private void initializeConfigurationBindings() {
        LOGGER.info("Initializing configuration bindings.");
        config.preserveAspectRatioProperty().addListener(e -> markViewportDirty());
    }

    /**
     * Renders the game boy's framebuffer onto the window.
     */
    private void renderFrame() {
        final var fb = gameboy.getVideo().getFramebuffer();

        glBindTexture(GL_TEXTURE_2D, emulatorTexture);
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, fb.getWidth(), fb.getHeight(), GL_RGB, GL_UNSIGNED_BYTE, fb.getPixels());

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        // TODO Ditch immediate-mode rendering and use vertex buffers instead.
        glBegin(GL_TRIANGLE_STRIP);
        {
            glTexCoord2f(0.0f, 0.0f);
            glVertex2f(-1.0f, 1.0f);

            glTexCoord2f(1.0f, 0.0f);
            glVertex2f(1.0f, 1.0f);

            glTexCoord2f(0.0f, 1.0f);
            glVertex2f(-1.0f, -1.0f);

            glTexCoord2f(1.0f, 1.0f);
            glVertex2f(1.0f, -1.0f);
        }
        glEnd();

        window.swapBuffers();
    }

    /**
     * Marks the viewport as dirty, forcing the display to be redrawn.
     */
    public void markViewportDirty() {
        this.isViewportDirty = true;
    }

    /**
     * Recalculates the size of the OpenGL viewport. By default, this will stretch to take up the entire window. If
     * aspect ratio preservation is enabled, it will calculate the size of the black bars.
     */
    private void recalculateViewport() {
        var padX = 0;
        var padY = 0;

        if (config.getPreserveAspectRatio()) {
            final var fb = gameboy.getVideo().getFramebuffer();

            final var targetAspect = (float) fb.getWidth() / fb.getHeight();
            final var windowAspect = (float) window.getWidth() / window.getHeight();

            if (targetAspect > windowAspect) {
                // Calculate the height of the black bars on the top and bottom.
                final var targetHeight = Math.round(window.getWidth() / targetAspect);
                padY = Math.abs(targetHeight - window.getHeight());
            } else {
                // Calculate the width of the black bars on the sides.
                final var targetWidth = Math.round(window.getHeight() * targetAspect);
                padX = Math.abs(targetWidth - window.getWidth());
            }
        }

        glViewport(padX / 2, padY / 2, window.getWidth() - padX, window.getHeight() - padY);
        renderFrame();
    }

    /**
     * Unloads the current cartridge allowing the system to terminate.
     */
    public void unloadCartridge() {
        this.isCartridgeLoaded = false;
        window.setVisible(false);

        cartridgeUnloadedEvent.broadcast(new CartridgeUnloadedEvent());
    }

    /**
     * Set the cartridge to load.
     *
     * @param path The path to the cartridge.
     */
    public void setCartridge(Path path) {
        this.cartridgePath = requireNonNull(path);
        this.isLoadingCartridge = true;
    }

    /**
     * Requests that the emulator state be saved to the current quick slot.
     */
    public void requestQuickSave() {
        this.isQuickSaving = true;
    }

    /**
     * Requests that the emulator state be loaded from the current quick slot.
     */
    public void requestQuickLoad() {
        this.isQuickLoading = true;
    }

    /**
     * Gets the observable property bound to the pause property. This is primarily used with JavaFX
     * to bind UI elements to data values.
     *
     * @return The pause property.
     */
    public BooleanProperty isPausedProperty() {
        return isPaused;
    }

    /**
     * Gets whether the emulator is currently paused.
     *
     * @return Whether the emulator is paused.
     */
    public boolean isPaused() {
        return isPaused.get();
    }

    /**
     * Pauses or unpauses the emulator.
     *
     * @param paused Whether the emulator should be paused.
     */
    public void setPaused(boolean paused) {
        isPaused.set(paused);
    }

    /**
     * Gets whether a cartridge is currently loaded.
     *
     * @return Whether a cartridge is loaded.
     */
    public boolean isCartridgeLoaded() {
        return isCartridgeLoaded;
    }

    /**
     * Gets the path of the currently loaded cartridge.
     *
     * @return The cartridge path, or null if no cartridge is loaded.
     */
    public Path getCartridgePath() {
        return cartridgePath;
    }

    /**
     * Gets the current configuration settings.
     *
     * @return The configuration.
     */
    public EmulatorConfig getConfig() {
        return config;
    }

    /**
     * Gets the Game Boy being emulated.
     *
     * @return The Game Boy
     */
    public GameBoy getGameBoy() {
        return gameboy;
    }

    /**
     * Registers a callback to be executed whenever a cartridge is loaded.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<CartridgeLoadedEvent> bindCartridgeLoadedEvent(Consumer<CartridgeLoadedEvent> callback) {
        return cartridgeLoadedEvent.bind(callback);
    }

    /**
     * Registers a callback to be executed whenever a cartridge is unloaded.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<CartridgeUnloadedEvent> bindCartridgeUnloadedEvent(Consumer<CartridgeUnloadedEvent> callback) {
        return cartridgeUnloadedEvent.bind(callback);
    }

    /**
     * Attempts to sleep for the specified amount of time.
     *
     * @param nanos The number of nanoseconds.
     */
    private static void sleepFor(long nanos) {
        if (nanos > 0L) {
            try {
                Thread.sleep(nanos / 1000000L, (int) (nanos % 1000000L));
            } catch (InterruptedException e) {
                // Nothing to do here.
            }
        }
    }

}
