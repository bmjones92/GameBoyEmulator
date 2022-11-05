package org.guide;

import javafx.application.Application;
import javafx.stage.Stage;
import org.guide.emulator.Emulator;
import org.guide.emulator.config.EmulatorConfig;
import org.guide.gui.controllers.HomeSceneController;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;

import java.nio.file.Paths;

/**
 * This class is responsible for bootstrapping the application.
 *
 * @author Brendan Jones
 */
public class App extends Application {

    /**
     * The application instance.
     */
    private static App instance;

    /**
     * The emulator instance.
     */
    private static Emulator emulator;

    /**
     * The command line arguments the program was launched with.
     */
    private static String[] args;

    /**
     * The home scene controller.
     */
    private static HomeSceneController homeController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        App.instance = this;
        App.homeController = new HomeSceneController(primaryStage, emulator);

        // Load the ROM right away if the user specified one.
        if (args.length > 0) {
            homeController.requestLoadROM(Paths.get(args[0]));
        }
    }

    /**
     * Gets the application instance.
     * @return The application instance.
     */
    public static App get() {
        return instance;
    }

    public static void main(String[] args) {
        App.args = args;

        // Load the configuration file.
        EmulatorConfig config;
        try {
            config = EmulatorConfig.load();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        // Initialize the GLFW library.
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW.");
        }

        // The FX controllers reference the emulator so it is necessary to create it before
        // launching the thread.
        try {
            App.emulator = new Emulator(config);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // The emulator must start on the main thread due to OS limitations. JavaFX does not return control to the
        // launcher thread until the JavaFX application closes, so it is necessary to create a proxy launcher thread in
        // the meantime.
        Thread fxLauncher = new Thread(() -> launch(App.class, args));
        fxLauncher.start();

        // Now that the FX thread has been created, it is safe to launch the emulator.
        emulator.start();
    }

}
