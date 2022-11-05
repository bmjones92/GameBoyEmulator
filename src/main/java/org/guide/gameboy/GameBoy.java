package org.guide.gameboy;

import org.guide.gameboy.audio.Audio;
import org.guide.gameboy.cartridge.Cartridge;
import org.guide.gameboy.input.Input;
import org.guide.gameboy.processor.CPUStatusMode;
import org.guide.gameboy.processor.Processor;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.dma.DMAController;
import org.guide.gameboy.processor.interrupts.memory.dma.HDMAController;
import org.guide.gameboy.serial.Serial;
import org.guide.gameboy.timer.Timer;
import org.guide.gameboy.video.Video;

import java.nio.ByteBuffer;

/**
 * Implements the Game Boy system in its entirety.
 *
 * @author Brendan Jones
 */
public class GameBoy implements SerializableComponent {

    /**
     * The memory component.
     */
    private final Memory memory;

    /**
     * The processor component.
     */
    private final Processor processor;

    /**
     * The input component.
     */
    private final Input input;

    /**
     * The video component.
     */
    private final Video video;

    /**
     * The cartridge component.
     */
    private final Cartridge cartridge;

    /**
     * The timer component.
     */
    private final Timer timer;

    /**
     * The serial component.
     */
    private final Serial serial;

    /**
     * The audio component.
     */
    private final Audio audio;

    /**
     * The dma controller component.
     */
    private final DMAController dma;

    /**
     * The hdma controller component.
     */
    private final HDMAController hdma;

    /**
     * <p>Creates a new {@code GameBoy} instance.</p>
     */
    public GameBoy() {
        this.memory = new Memory();
        this.processor = new Processor(memory);
        this.cartridge = new Cartridge(memory);
        this.audio = new Audio(memory);

        final var interrupts = processor.getInterrupts();
        this.input = new Input(memory, interrupts);
        this.video = new Video(memory, interrupts);
        this.timer = new Timer(memory, interrupts);
        this.serial = new Serial(memory, interrupts);

        this.dma = new DMAController(memory);
        this.hdma = new HDMAController(memory, video);
    }

    /**
     * Emulates the system until the end of the current frame.
     */
    public void runFrame() {
        if (!cartridge.isLoaded()) {
            return;
        }

        while (!tick()) ;
    }

    /**
     * Ticks the hardware for a single instruction worth of cycles.
     *
     * @return Whether the processed instruction marks the end of a frame.
     */
    public boolean tick() {
        final var cycles = processor.tick();
        final var status = processor.getStatus();

        serial.tick(cycles);
        input.tick();

        if (status == CPUStatusMode.STOPPED) {
            return true;
        }

        if (status == CPUStatusMode.RUNNING) {
            dma.tick(cycles);
        }

        timer.tick(cycles);

        // Video and audio hardware is not affected by double processor speed.
        final var normalizedCycles = processor.isDoubleSpeed() ? cycles >> 1 : cycles;

        // TODO audio.tick(normalizedCycles)
        return video.tick(status, normalizedCycles);
    }

    /**
     * Loads new cartridge data.
     *
     * @param data The cartridge.
     * @throws Exception
     */
    public void loadCartridge(byte[] data) throws Exception {
        // Load the new cartridge data.
        cartridge.load(data);

        // Reset the emulator to its initial state.
        reset();
    }

    @Override
    public void serialize(ByteBuffer out) {
        if (!cartridge.isLoaded()) {
            throw new IllegalStateException("Game Boy is not running.");
        }

        cartridge.getMBC().serialize(out);
        memory.serialize(out);
        processor.serialize(out);
        serial.serialize(out);
        timer.serialize(out);
        dma.serialize(out);
        hdma.serialize(out);
        video.serialize(out);
        audio.serialize(out);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        if (!cartridge.isLoaded()) {
            return;
        }

        reset();

        cartridge.getMBC().deserialize(in);
        memory.deserialize(in);
        processor.deserialize(in);
        serial.deserialize(in);
        timer.deserialize(in);
        dma.deserialize(in);
        hdma.deserialize(in);
        video.deserialize(in);
        audio.deserialize(in);

        memory.broadcastGlobalMemoryChangedEvent();
    }

    /**
     * Resets the system to its initial boot state.
     */
    public void reset() {
        // No ROM is loaded, so there is nothing to reset.
        if (cartridge == null) {
            return;
        }

        // Get the mode to start the cartridge on.
        boolean isCGB = cartridge.getHeader().isCGB();

        // Reset the system components.
        memory.reset(isCGB);
        cartridge.reset();
        processor.reset(isCGB);
        input.reset();
        video.reset(isCGB);
        timer.reset(isCGB);
        serial.reset();
        dma.reset();
        hdma.reset(isCGB);
    }

    /**
     * Gets the memory component of the system.
     *
     * @return The memory component.
     */
    public Memory getMemory() {
        return memory;
    }

    /**
     * Gets the processor component of the system.
     *
     * @return The processor component.
     */
    public Processor getProcessor() {
        return processor;
    }

    /**
     * Gets the input component of the system.
     *
     * @return The input component.
     */
    public Input getInput() {
        return input;
    }

    /**
     * Gets the video component of the system.
     *
     * @return The video component.
     */
    public Video getVideo() {
        return video;
    }

    /**
     * Gets the timer component of the system.
     *
     * @return The timer component.
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Gets the serial component of the system.
     *
     * @return The serial component.
     */
    public Serial getSerial() {
        return serial;
    }

    /**
     * Gets the audio component of the system.
     *
     * @return The audio component.
     */
    public Audio getAudio() {
        return audio;
    }

    /**
     * Gets the cartridge component of the system.
     *
     * @return The cartridge component.
     */
    public Cartridge getCartridge() {
        return cartridge;
    }

}
