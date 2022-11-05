package org.guide.emulator.input;

import org.guide.gameboy.GameBoy;
import org.guide.gameboy.input.GameboyButton;
import org.guide.gameboy.input.Input;

import java.util.Objects;
import java.util.function.Consumer;

public class InputCommand implements Consumer<Boolean> {

    private final GameboyButton button;

    private final Input input;

    public InputCommand(GameboyButton button, GameBoy gameboy) {
        this.button = Objects.requireNonNull(button);
        this.input = Objects.requireNonNull(gameboy.getInput());
    }

    @Override
    public void accept(Boolean pressed) {
        input.setButton(button, pressed);
    }

}
