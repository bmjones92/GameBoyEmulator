package org.guide.emulator;

import org.guide.emulator.event.*;
import org.guide.gameboy.video.Video;
import org.guide.util.delegate.EventDispatcher;
import org.guide.util.delegate.EventDispatcherHandle;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.Consumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * A high-level representation of an application window.
 *
 * @author Brendan Jones
 */
public class Window {

    /**
     * The initial scale of the window relative to the size of the Game Boy display.
     */
    private static final int INITIAL_SCALE = 3;

    /**
     * The dispatcher for key state events.
     */
    private final EventDispatcher<KeyStateEvent> keyStateEventDispatcher = new EventDispatcher<>();

    /**
     * The dispatcher for file drop events.
     */
    private final EventDispatcher<FileDropEvent> fileDropEventDispatcher = new EventDispatcher<>();

    /**
     * The dispatcher for window focus events.
     */
    private final EventDispatcher<WindowFocusEvent> windowFocusEventDispatcher = new EventDispatcher<>();

    /**
     * The dispatcher for window close events.
     */
    private final EventDispatcher<WindowCloseEvent> windowCloseEventDispatcher = new EventDispatcher<>();

    /**
     * The dispatcher for window resize events.
     */
    private final EventDispatcher<WindowResizeEvent> windowResizeEventDispatcher = new EventDispatcher<>();

    /**
     * The dispatcher for window refresh events.
     */
    private final EventDispatcher<WindowRefreshEvent> windowRefreshEventDispatcher = new EventDispatcher<>();

    /**
     * The native handle of the underlying window.
     */
    private long handle;

    /**
     * The width of the window.
     */
    private int width = Video.DISPLAY_WIDTH * INITIAL_SCALE;

    /**
     * The height of the window.
     */
    private int height = Video.DISPLAY_HEIGHT * INITIAL_SCALE;

    /**
     * Whether the window has focus.
     */
    private boolean isFocused;

    /**
     * The OpenGL capabilities of the underlying window.
     */
    private final GLCapabilities caps;

    /**
     * Creates a new Window.
     */
    public Window() {
        // Configure the default window creation hints.
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        // Attempt to create the window.
        this.handle = glfwCreateWindow(width, height, "UNDEFINED", NULL, NULL);
        if (handle == NULL) {
            throw new IllegalStateException("Could not create Emulator window.");
        }

        glfwSetWindowSizeLimits(handle, Video.DISPLAY_WIDTH, Video.DISPLAY_HEIGHT, GLFW_DONT_CARE, GLFW_DONT_CARE);

        // Initialize the OpenGL capabilities.
        glfwMakeContextCurrent(handle);
        this.caps = GL.createCapabilities();

        // Register event callbacks.
        glfwSetKeyCallback(handle, this::onKeyEvent);
        glfwSetDropCallback(handle, this::onDropEvent);
        glfwSetWindowFocusCallback(handle, this::onWindowFocusEvent);
        glfwSetWindowCloseCallback(handle, this::onWindowCloseEvent);
        glfwSetFramebufferSizeCallback(handle, this::onFramebufferSizeEvent);
        glfwSetWindowRefreshCallback(handle, this::onWindowRefreshEvent);
    }

    /**
     * Set the visibility of the window.
     *
     * @param visible Whether the window is visible.
     */
    public void setVisible(boolean visible) {
        if (visible) {
            glfwShowWindow(handle);
        } else {
            glfwHideWindow(handle);
        }
    }

    /**
     * Set the title of the window.
     *
     * @param title The title of the window.
     */
    public void setTitle(String title) {
        glfwSetWindowTitle(handle, Objects.requireNonNull(title));
    }

    /**
     * Swaps the frame buffers for this window.
     */
    public void swapBuffers() {
        glfwSwapBuffers(handle);
    }

    /**
     * Destroys the window.
     */
    public void destroy() {
        glfwDestroyWindow(handle);
        this.handle = NULL;
    }

    /**
     * Processes a key event and dispatches it to all registered observers.
     *
     * @param window   The window that generated the event.
     * @param key      The key that generated the event.
     * @param scancode The scancode.
     * @param action   The new key state (PRESS, RELEASE, REPEAT).
     * @param mods     Any modifiers that were down at the time of the event.
     */
    private void onKeyEvent(long window, int key, int scancode, int action, int mods) {
        keyStateEventDispatcher.broadcast(new KeyStateEvent(this, key, action));
    }

    /**
     * Processes a file drop event and dispatches it to all registered observers.
     *
     * @param window The window that generated the event.
     * @param count  The number of files that were dropped.
     * @param names  The names of the files that were dropped.
     */
    private void onDropEvent(long window, int count, long names) {
        final var fileNames = new ArrayList<String>(count);
        for (var id = 0; id < count; id++) {
            final var fileName = GLFWDropCallback.getName(names, id);
            fileNames.add(fileName);
        }

        fileDropEventDispatcher.broadcast(new FileDropEvent(this, fileNames));
    }

    /**
     * Processes a window focus event and dispatches it to all registered observers.
     *
     * @param window  The window that generated the event.
     * @param focused Whether the window is focused.
     */
    private void onWindowFocusEvent(long window, boolean focused) {
        this.isFocused = focused;
        windowFocusEventDispatcher.broadcast(new WindowFocusEvent(this, focused));
    }

    /**
     * Processes a window close event and dispatches it to all registered observers.
     *
     * @param window The window that generated the event.
     */
    private void onWindowCloseEvent(long window) {
        windowCloseEventDispatcher.broadcast(new WindowCloseEvent(this));
    }

    /**
     * Processes a framebuffer resize event and dispatches it to all registered observers.
     *
     * @param window The window that generated the event.
     */
    private void onFramebufferSizeEvent(long window, int width, int height) {
        this.width = width;
        this.height = height;

        windowResizeEventDispatcher.broadcast(new WindowResizeEvent(this, width, height));
    }

    /**
     * Processes a window refresh event and dispatches it to all registered observers.
     *
     * @param window The window that generated the event.
     */
    private void onWindowRefreshEvent(long window) {
        windowRefreshEventDispatcher.broadcast(new WindowRefreshEvent(this));
    }

    /**
     * Registers a callback to be executed whenever a key state event occurs.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<KeyStateEvent> bindKeyStateEvent(Consumer<KeyStateEvent> callback) {
        return keyStateEventDispatcher.bind(callback);
    }

    /**
     * Registers a callback to be executed whenever a file drop event occurs.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<FileDropEvent> bindFileDropEvent(Consumer<FileDropEvent> callback) {
        return fileDropEventDispatcher.bind(callback);
    }

    /**
     * Registers a callback to be executed whenever a window focus event occurs.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<WindowFocusEvent> bindWindowFocusEvent(Consumer<WindowFocusEvent> callback) {
        return windowFocusEventDispatcher.bind(callback);
    }

    /**
     * Registers a callback to be executed whenever a window close event occurs.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<WindowCloseEvent> bindWindowCloseEvent(Consumer<WindowCloseEvent> callback) {
        return windowCloseEventDispatcher.bind(callback);
    }

    /**
     * Registers a callback to be executed whenever a window resize event occurs.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<WindowResizeEvent> bindWindowResizeEvent(Consumer<WindowResizeEvent> callback) {
        return windowResizeEventDispatcher.bind(callback);
    }

    /**
     * Registers a callback to be executed whenever a window refresh event occurs.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<WindowRefreshEvent> bindWindowRefreshEvent(Consumer<WindowRefreshEvent> callback) {
        return windowRefreshEventDispatcher.bind(callback);
    }

    /**
     * The width (in pixels) of the window's framebuffer.
     *
     * @return The width.
     */
    public int getWidth() {
        return width;
    }

    /**
     * The height (in pixels) of the window's framebuffer.
     *
     * @return The height.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Whether the window is currently in focus.
     *
     * @return Whether the window is focused.
     */
    public boolean isFocused() {
        return isFocused;
    }

}
