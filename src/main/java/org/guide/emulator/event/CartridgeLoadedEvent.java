package org.guide.emulator.event;

import org.guide.gameboy.cartridge.Cartridge;

import java.nio.file.Path;

/**
 * An event that fires when a cartridge is loaded.
 *
 * @param path      The path of the cartridge.
 * @param cartridge The cartridge.
 * @param error     The error if one occurred.
 * @author Brendan Jones
 */
public record CartridgeLoadedEvent(Path path, Cartridge cartridge, Exception error) {
}
