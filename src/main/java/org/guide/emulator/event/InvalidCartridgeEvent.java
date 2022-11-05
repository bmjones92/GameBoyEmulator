package org.guide.emulator.event;

import java.nio.file.Path;

public record InvalidCartridgeEvent(Path path, Exception e) {
}
