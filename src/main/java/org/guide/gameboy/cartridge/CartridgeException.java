package org.guide.gameboy.cartridge;

import java.io.Serial;

/**
 * A cartridge exception is thrown when the emulator attempts to load a cartridge file that contains invalid
 * cartridge data.
 */
public class CartridgeException extends Exception {

    @Serial
    private static final long serialVersionUID = 2136293824319625541L;

    /**
     * Creates a new cartridge exception instance.
     *
     * @param message The detail message.
     */
    public CartridgeException(String message) {
        super(message);
    }

    /**
     * Creates a new Cartridge Exception using a formatted string as its detail message.
     *
     * @param format     The message format.
     * @param parameters The parameters to the message format.
     * @return
     */
    public static CartridgeException format(String format, Object... parameters) {
        return new CartridgeException(String.format(format, parameters));
    }

}
