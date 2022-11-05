package org.guide.emulator.event;

import org.guide.emulator.Window;

import java.util.List;

/**
 * An event that fires when a file is dropped from the file explorer onto a window.
 *
 * @param window    The window.
 * @param fileNames The names of the files that were dropped.
 * @author Brendan Jones
 */
public record FileDropEvent(Window window, List<String> fileNames) {
}
