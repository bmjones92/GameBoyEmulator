package org.guide.gameboy.video;

import org.guide.gameboy.SerializableComponent;
import org.guide.gameboy.processor.CPUStatusMode;
import org.guide.gameboy.processor.interrupts.Interrupt;
import org.guide.gameboy.processor.interrupts.InterruptController;
import org.guide.gameboy.processor.interrupts.memory.Memory;
import org.guide.gameboy.processor.interrupts.memory.MemoryBank;
import org.guide.gameboy.processor.interrupts.memory.space.AddressSpace;
import org.guide.gameboy.video.event.VideoModeEvent;
import org.guide.gameboy.video.queue.PixelQueue;
import org.guide.gameboy.video.queue.PixelSource;
import org.guide.util.AddressUtils;
import org.guide.util.BitUtils;
import org.guide.util.delegate.EventDispatcher;
import org.guide.util.delegate.EventDispatcherHandle;

import java.nio.ByteBuffer;
import java.util.PriorityQueue;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

/**
 * Implements the Game Boy's video hardware.
 *
 * @author Brendan Jones
 */
public class Video extends AddressSpace implements SerializableComponent {

    /**
     * The width (in pixels) of the LCD.
     */
    public static final int DISPLAY_WIDTH = 160;

    /**
     * The height (in pixels) of the LCD.
     */
    public static final int DISPLAY_HEIGHT = 144;

    /**
     * The scanline that marks the start of the v-blank period.
     */
    private static final int VBLANK_START_SCANLINE = DISPLAY_HEIGHT;

    /**
     * The scanline that marks the end of the v-blank period.
     */
    private static final int VBLANK_END_SCANLINE = VBLANK_START_SCANLINE + 10;

    /**
     * The number of clock cycles that elapse per scanline.
     */
    private static final int SCANLINE_CYCLES = 456;

    /**
     * The number of clock cycles that elapse per frame.
     */
    private static final int NUM_FRAME_CYCLES = SCANLINE_CYCLES * VBLANK_END_SCANLINE;

    /**
     * The number of cycles required to complete the OAM search period.
     */
    private static final int OAM_SEARCH_CYCLES = 80;

    /**
     * The number of sprites that can be loaded into OAM memory.
     */
    private static final int MAX_SPRITES = 40;

    /**
     * The number of sprites that can be rendered per scanline.
     */
    private static final int MAX_VISIBLE_SPRITES = 10;

    /**
     * The number of color palettes on the CGB system.
     */
    private static final int NUM_CGB_PALETTES = 8;

    /**
     * The number of colors CGB palette.
     */
    private static final int NUM_PALETTE_COLORS = 4;

    /**
     * The background color palettes.
     */
    private final PaletteColor[][] cgbBackgroundPalettes = new PaletteColor[8][4];

    /**
     * The sprite color palettes.
     */
    private final PaletteColor[][] cgbSpritePalettes = new PaletteColor[8][4];

    /**
     * Whether the loaded cartridge is in CGB mode.
     */
    private boolean isCGB;

    /**
     * The internal signal for the STAT interrupt.
     */
    private boolean irqSignal;

    /**
     * The {@code InterruptController} to generate interrupts for.
     */
    private final InterruptController interrupts;

    /**
     * The {@code Framebuffer} to write display output to.
     */
    private final Framebuffer framebuffer = new Framebuffer(DISPLAY_WIDTH, DISPLAY_HEIGHT);

    /**
     * The {@code PixelQueue} for transferring data.
     */
    private final PixelQueue pixelQueue = new PixelQueue();

    /**
     * The list of sprites to be rendered on the current scanline.
     */
    private final PriorityQueue<Integer> visibleSprites;

    /**
     * The number of clock cycles that have elapsed on the current scanline.
     */
    private int currentScanlineCycles;

    /**
     * The number of cycles that overflowed from the last status.
     */
    private int overflowCycles;

    /**
     * The value of the windowX register at the start of the current scanline.
     */
    private int scx;

    /**
     * The value of the scrollY register at the start of the current scanline.
     */
    private int scy;

    /**
     * The value of the windowX register at the start of the current scanline.
     */
    private int wx;

    /**
     * The value of the windowY register at the start of the current scanline.
     */
    private int wy;

    /**
     * The x coordinate of the next pixel to transfer to the framebuffer.
     */
    private int lx;

    /**
     * Indicates whether the window background map is being fetched.
     */
    private boolean isFetchingWindow;

    /**
     * Indicates whether a sprite is being fetched.
     */
    private boolean isFetchingSprite;

    /**
     * The number of pixels to discard from the pixel queue before
     * transferring them to the LCD.
     */
    private int numPixelsToDiscard;

    /**
     * The current fetcher stage.
     */
    private int fetcherState;

    /**
     * Whether the power state is toggling.
     */
    private boolean isPowerToggling;

    /**
     * Whether the video is currently enabled.
     */
    private boolean isLCDEnabled;

    /**
     * Whether the video hardware is enabling.
     */
    private boolean isLCDEnabling;

    /**
     * The address space for video RAM.
     */
    private final MemoryBank vram;

    /**
     * The dispatcher for video mode events.
     */
    private final EventDispatcher<VideoModeEvent> videoModeEventDispatcher = new EventDispatcher<>();

    /**
     * Creates a new {@code Video} instance that uses the specified memory and interrupt controller.
     *
     * @param memory     The memory.
     * @param interrupts The interrupt controller.
     */
    public Video(Memory memory, InterruptController interrupts) {
        super(memory);
        this.interrupts = requireNonNull(interrupts);
        this.vram = new MemoryBank(memory, true, true, AddressUtils.VRAM_ADDRESS_START, 2, 0x2000);

        this.visibleSprites = new PriorityQueue<>(MAX_VISIBLE_SPRITES, this::compareVisibleSprites);

        // Initialize the color palettes.
        for (var palette = 0; palette < NUM_CGB_PALETTES; ++palette) {
            for (var color = 0; color < NUM_PALETTE_COLORS; ++color) {
                cgbBackgroundPalettes[palette][color] = new PaletteColor(PaletteColor.DEFAULT_PALETTE[color]);
                cgbSpritePalettes[palette][color] = new PaletteColor(PaletteColor.DEFAULT_PALETTE[color]);
            }
        }

        // Register the address space callbacks.
        memory.setAddressSpace(this, AddressUtils.LCDC);
        memory.setAddressSpace(this, AddressUtils.STAT);
        memory.setAddressSpace(this, AddressUtils.SCY);
        memory.setAddressSpace(this, AddressUtils.SCX);
        memory.setAddressSpace(this, AddressUtils.LY);
        memory.setAddressSpace(this, AddressUtils.LYC);
        memory.setAddressSpace(this, AddressUtils.BGP);
        memory.setAddressSpace(this, AddressUtils.OBP0);
        memory.setAddressSpace(this, AddressUtils.OBP1);
        memory.setAddressSpace(this, AddressUtils.WY);
        memory.setAddressSpace(this, AddressUtils.WX);
        memory.setAddressSpace(this, AddressUtils.BCPS);
        memory.setAddressSpace(this, AddressUtils.BCPD);
        memory.setAddressSpace(this, AddressUtils.OCPS);
        memory.setAddressSpace(this, AddressUtils.OCPD);

        memory.setAddressSpace(this, AddressUtils.VRAM_ADDRESS_START, AddressUtils.VRAM_ADDRESS_END);
        memory.setAddressSpace(this, AddressUtils.VBK);
    }

    /**
     * Resets the Video hardware to its default state.
     *
     * @param isCGB Whether to reset to the default state for DMG or CGB hardware.
     */
    public void reset(boolean isCGB) {
        this.isCGB = isCGB;
        this.irqSignal = false;

        vram.reset(null);

        // Reset the color palettes to their default values.
        for (var palette = 0; palette < NUM_CGB_PALETTES; ++palette) {
            for (var color = 0; color < NUM_PALETTE_COLORS; ++color) {
                cgbBackgroundPalettes[palette][color].set(PaletteColor.DEFAULT_PALETTE[color]);
                cgbSpritePalettes[palette][color].set(PaletteColor.DEFAULT_PALETTE[color]);
            }
        }

        this.currentScanlineCycles = 0;
        this.overflowCycles = 0;
        this.isFetchingSprite = false;
        this.isFetchingWindow = false;
        this.isLCDEnabled = true;
        this.isLCDEnabling = false;

        pixelQueue.clear();
        visibleSprites.clear();

        memory.set(AddressUtils.LCDC, 0x91);
        memory.set(AddressUtils.SCY, 0x00);
        memory.set(AddressUtils.SCX, 0x00);
        memory.set(AddressUtils.LYC, 0x00);
        memory.set(AddressUtils.BGP, 0xFC);
        memory.set(AddressUtils.OBP0, 0xFF);
        memory.set(AddressUtils.OBP1, 0xFF);
        memory.set(AddressUtils.WY, 0x00);
        memory.set(AddressUtils.WX, 0x07);
        memory.set(AddressUtils.LY, 0x00);

        setStatusMode(LCDMode.SEARCH, 0);
    }

    @Override
    public void write(int address, int value) {
        if (address == AddressUtils.LY || (address == AddressUtils.VBK && !isCGB) || isMemoryInaccessible(address)) {
            return;
        }

        super.write(address, value);

        switch (address) {
            case AddressUtils.STAT -> updateStatInterruptSignal();
            case AddressUtils.LYC -> updateLYCompareBit();
            case AddressUtils.BCPD -> writeColorPalette(AddressUtils.BCPS, value, cgbBackgroundPalettes);
            case AddressUtils.OCPD -> writeColorPalette(AddressUtils.OCPS, value, cgbSpritePalettes);
            case AddressUtils.VBK -> vram.setActiveBank(value & 0x1);
            case AddressUtils.LCDC -> updateLCDCRegister();
        }
    }

    @Override
    public int read(int address) {
        if (isMemoryInaccessible(address)) {
            return 0xFF;
        }

        switch (address) {
            case AddressUtils.BCPD -> readColorPalette(AddressUtils.BCPS, address, cgbBackgroundPalettes);
            case AddressUtils.OCPD -> readColorPalette(AddressUtils.OCPS, address, cgbSpritePalettes);
        }

        return super.read(address);
    }

    @Override
    public void serialize(ByteBuffer out) {
        SerializableComponent.writeIntegrityCheck(out);

        out.putShort((short) currentScanlineCycles);
        out.put((byte) (irqSignal ? 1 : 0));
        out.put((byte) overflowCycles);
        out.put((byte) scx);
        out.put((byte) scy);
        out.put((byte) wx);
        out.put((byte) wy);
        out.put((byte) lx);
        out.put((byte) (isFetchingWindow ? 1 : 0));
        out.put((byte) (isFetchingSprite ? 1 : 0));
        out.put((byte) numPixelsToDiscard);
        out.put((byte) fetcherState);

        SerializableComponent.writeIntegrityCheck(out);

        // Write the color palettes out to memory.
        for (var palette = 0; palette < NUM_CGB_PALETTES; ++palette) {
            for (var color = 0; color < NUM_PALETTE_COLORS; ++color) {
                out.putShort((short) cgbBackgroundPalettes[palette][color].getColor15());
                out.putShort((short) cgbSpritePalettes[palette][color].getColor15());
            }
        }

        SerializableComponent.writeIntegrityCheck(out);

        // Write the current framebuffer state.
        out.put(framebuffer.getPixels().asReadOnlyBuffer());

        SerializableComponent.writeIntegrityCheck(out);

        // Write the current sprite queue elements. The order of the elements is not sorted.
        out.put((byte) visibleSprites.size());
        visibleSprites.forEach(sprite -> out.put((byte) sprite.intValue()));

        SerializableComponent.writeIntegrityCheck(out);

        // Serialize the pixel queue.
        pixelQueue.serialize(out);

        SerializableComponent.writeIntegrityCheck(out);

        // Write the video ram state.
        vram.serialize(out);

        SerializableComponent.writeIntegrityCheck(out);
    }

    @Override
    public void deserialize(ByteBuffer in) {
        SerializableComponent.verifyIntegrityCheck(in, "Video::Start");

        this.currentScanlineCycles = in.getShort() & 0xFFFF;
        this.irqSignal = (in.get() & 0xFF) == 1;
        this.overflowCycles = in.get() & 0xFF;
        this.scx = in.get() & 0xFF;
        this.scy = in.get() & 0xFF;
        this.wx = in.get() & 0xFF;
        this.wy = in.get() & 0xFF;
        this.lx = in.get() & 0xFF;
        this.isFetchingWindow = (in.get() & 0xFF) == 1;
        this.isFetchingSprite = (in.get() & 0xFF) == 1;
        this.numPixelsToDiscard = in.get() & 0xFF;
        this.fetcherState = in.get() & 0xFF;

        SerializableComponent.verifyIntegrityCheck(in, "Video::Registers");

        for (var palette = 0; palette < NUM_CGB_PALETTES; ++palette) {
            for (var color = 0; color < NUM_PALETTE_COLORS; ++color) {
                cgbBackgroundPalettes[palette][color].set(in.getShort() & 0xFFFF);
                cgbSpritePalettes[palette][color].set(in.getShort() & 0xFFFF);
            }
        }

        SerializableComponent.verifyIntegrityCheck(in, "Video::Palette");

        // TODO Maybe move this into the framebuffer class.
        {
            final var data = new byte[framebuffer.getWidth() * framebuffer.getHeight() * 3];
            in.get(data);

            final var fb = framebuffer.getPixels();
            fb.clear().put(data).clear();
        }

        SerializableComponent.verifyIntegrityCheck(in, "Video::Framebuffer");

        // Read the list of visible sprites.
        final var numSprites = in.get() & 0xFF;
        for (var i = 0; i < numSprites; ++i) {
            visibleSprites.add(in.get() & 0xFF);
        }

        SerializableComponent.verifyIntegrityCheck(in, "Video::VisibleSprites");

        // Deserialize the pixel queue.
        pixelQueue.deserialize(in);

        SerializableComponent.verifyIntegrityCheck(in, "Video::PixelQueue");

        // Deserialize the video ram.
        vram.deserialize(in);

        SerializableComponent.verifyIntegrityCheck(in, "Video::VRAM");
    }

    /**
     * Compares two visible sprites to determine their sorting order.
     *
     * @param s1 The index of the fist sprite.
     * @param s2 The index of the second sprite.
     * @return The sprite that is ordered first.
     */
    private int compareVisibleSprites(int s1, int s2) {
        final var x1 = memory.getUnsigned(AddressUtils.getSpriteAddress(s1) + 1);
        final var x2 = memory.getUnsigned(AddressUtils.getSpriteAddress(s2) + 1);

        return Integer.compare(x1, x2);
    }

    /**
     * Reads a color palette's data.
     *
     * @param specAddress The address of the specification register.
     * @param dataAddress The address of the data register.
     * @param palettes    The color palettes to read from.
     */
    private void readColorPalette(int specAddress, int dataAddress, PaletteColor[][] palettes) {
        final var spec = memory.getUnsigned(specAddress);

        final var paletteNum = (spec >> 3) & 0x07;
        final var dataNum = (spec >> 1) & 0x03;

        var data = palettes[paletteNum][dataNum].getColor15();
        if (BitUtils.isSet(data, 0)) {
            data = (data >> 8) & 0xFF; // High byte
        } else {
            data = data & 0xFF; // Low byte
        }
        memory.set(dataAddress, data);
    }

    /**
     * Updates a palette color.
     *
     * @param specAddress The address of the specification register.
     * @param data        The data to write.
     * @param palettes    The color palette group to update.
     */
    private void writeColorPalette(int specAddress, int data, PaletteColor[][] palettes) {
        final var spec = memory.getUnsigned(specAddress);

        final var paletteNum = (spec >> 3) & 0x07;
        final var dataNum = (spec >> 1) & 0x03;

        // Update the color palette data.
        final var color = palettes[paletteNum][dataNum];
        if (BitUtils.isSet(spec, 0)) {
            color.setHigh(data);
        } else {
            color.setLow(data);
        }

        // Auto-increment is enabled.
        if (BitUtils.isSet(spec, 7)) {
            final var newSpec = (spec & 0xC0) | ((spec + 1) & 0x3F);
            memory.set(specAddress, newSpec);
        }
    }

    /**
     * Checks whether the processor is able to access the specified memory address based on the current status mode.
     *
     * @param address The address to check.
     * @return Whether the specified address is inaccessible.
     */
    private boolean isMemoryInaccessible(int address) {
        // FIXME There's a bug somewhere causes emulation to break when this is enabled. Possibly some functionality
        //       that hasn't been implemented yet?
//        if (isLCDEnabled) {
//            final var mode = LCDMode.get(memory.getUnsigned(AddressUtils.STAT));
//            if (!mode.isOAMAccessible() && AddressUtils.isAddressOAM(address)) {
//                return false;
//            }
//
//            if (!mode.isVRAMAccessible() && AddressUtils.isAddressVRAM(address)) {
//                return false;
//            }
//
//            // Palette data registers are inaccessible on DMG and cannot be accessed during certain status modes.
//            if ((!isCGB || !mode.isPaletteAccessible()) && (address == AddressUtils.BCPD || address == AddressUtils.OCPD)) {
//                return false;
//            }
//        }
        return false;
    }

    /**
     * Runs the LCD hardware for the specified number of ticks.
     *
     * @param status        The status mode of the processor.
     * @param elapsedCycles The number of cycles that have elapsed since the last tick.
     * @return Whether the LCD entered v-blank mode during this tick.
     */
    public boolean tick(CPUStatusMode status, int elapsedCycles) {
        elapsedCycles += overflowCycles;
        overflowCycles = 0;

        // Update the number of cycles that have elapsed for the current scanline.
        this.currentScanlineCycles += elapsedCycles;

        if (isLCDEnabled) {
            final var mode = LCDMode.get(memory.get(AddressUtils.STAT));
            switch (mode) {
                case HBLANK:
                    return tickHBlank();
                case VBLANK:
                    tickVBlank();
                    return false;
                case SEARCH:
                    tickSearchOAM();
                    return false;
                case TRANSFER:
                    tickTransferPixels(elapsedCycles);
                    return false;
                default:
                    throw new IllegalStateException("Unexpected LCDMode: " + mode);
            }
        } else {
            if (currentScanlineCycles >= NUM_FRAME_CYCLES) {
                currentScanlineCycles -= NUM_FRAME_CYCLES;
                return true;
            }
            return false;
        }
    }

    /**
     * This mode occurs at the start of each visible (0-143) scanline. It is responsible for searching through OAM
     * memory and selects up to 10 sprites to be drawn during the current scanline.
     */
    private void tickSearchOAM() {
        if (currentScanlineCycles >= OAM_SEARCH_CYCLES) {
            visibleSprites.clear();

            final var lcdc = memory.getUnsigned(AddressUtils.LCDC);
            if (BitUtils.isSet(lcdc, 1)) {
                final var ly = memory.getUnsigned(AddressUtils.LY) + 16;
                final var spriteHeight = BitUtils.isSet(lcdc, 2) ? 16 : 8;

                // Search through OAM and select which sprites should be
                // drawn on the current scanline.
                for (var sprite = 0; sprite < MAX_SPRITES; ++sprite) {
                    final var address = AddressUtils.getSpriteAddress(sprite);

                    final var spriteY = memory.getUnsigned(address);
                    final var spriteX = memory.getUnsigned(address + 1);

                    if (spriteX != 0 && ly >= spriteY && ly < spriteY + spriteHeight) {
                        visibleSprites.add(sprite);
                        if (visibleSprites.size() >= MAX_VISIBLE_SPRITES) {
                            break;
                        }
                    }
                }
            }

            setStatusMode(LCDMode.TRANSFER, currentScanlineCycles - OAM_SEARCH_CYCLES);
        }
    }

    /**
     * This mode occurs after the OAM search mode for each visible scanline. It is responsible for transferring graphics
     * data from memory to the LCD's framebuffer.
     *
     * @param elapsedCycles The number of CPU cycles that have elapsed since the last tick.
     */
    private void tickTransferPixels(int elapsedCycles) {
        final var lcdc = memory.getUnsigned(AddressUtils.LCDC);
        final var ly = memory.getUnsigned(AddressUtils.LY);

        // The color palettes for DMG games.
        final var bgp = memory.getUnsigned(AddressUtils.BGP);
        final var obp0 = memory.getUnsigned(AddressUtils.OBP0);
        final var obp1 = memory.getUnsigned(AddressUtils.OBP1);

        for (var currentCycle = 0; currentCycle < elapsedCycles; ++currentCycle) {
            if (!tickPixelFIFO(lcdc, ly, bgp, obp0, obp1)) {
                setStatusMode(LCDMode.HBLANK, elapsedCycles - currentCycle - 1);
                break;
            }

            if ((currentCycle % 2) == 1) {
                tickFetcher(lcdc, ly);
            }
        }
    }

    /**
     * Simulates a single tick of the pixel FIFO which is responsible for constructing a single pixel and writing it to
     * the Framebuffer.
     *
     * @param lcdc The value of the LCDC register.
     * @param ly   The value of the LY register.
     * @param bgp  The value of the BGP register.
     * @param obp0 The value of the OBP0 register.
     * @param obp1 The value of the OBP1 register.
     * @return Whether this is still more data to transfer for the current scanline.
     */
    private boolean tickPixelFIFO(int lcdc, int ly, int bgp, int obp0, int obp1) {
        // The pixel FIFO requires more than 8 pixels to be enqueued. Additionally, it is suspended while a sprite is
        // being fetched because sprites overlay the first 8 pixels in the queue.
        if (isFetchingSprite || pixelQueue.size() <= 8) {
            return true;
        }

        // The fetcher is not currently suspended, so it is safe to process the next element.
        final var entry = pixelQueue.pop();
        if (entry.getSource() == PixelSource.BACKGROUND && numPixelsToDiscard > 0) {
            numPixelsToDiscard--;
        } else {
            PaletteColor color;
            if (isCGB) {
                if (entry.getSource() == PixelSource.SPRITE) {
                    color = cgbSpritePalettes[entry.getPalette()][entry.getColor()];
                } else {
                    color = cgbBackgroundPalettes[entry.getPalette()][entry.getColor()];
                }
            } else {
                var colorIndex = entry.getColor();
                switch (entry.getSource()) {
                    case BACKGROUND:
                    case WINDOW:
                        colorIndex = (bgp >> (colorIndex << 1)) & 0x3;
                        break;
                    case SPRITE:
                        if (entry.getPalette() == 0) {
                            colorIndex = (obp0 >> (colorIndex << 1)) & 0x3;
                        } else {
                            colorIndex = (obp1 >> (colorIndex << 1)) & 0x3;
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Unexpected pixel source: " + entry.getSource());
                }

                color = PaletteColor.DEFAULT_PALETTE[colorIndex];
            }

            framebuffer.setPixel(lx, ly, color);
            setLX(lx + 1);
        }

        return lx < DISPLAY_WIDTH;
    }

    /**
     * Simulates a single tick of the tile fetcher which is responsible for loading data from memory and inserting it
     * into the pixel queue.
     *
     * @param lcdc The value of the LCDC register.
     * @param ly   The value of the LY register.
     */
    private void tickFetcher(int lcdc, int ly) {
        // The fetcher takes 3 ticks to fetch and construct the tile data.
        if (fetcherState < 3) {
            fetcherState++;
        }

        // Tile data can only be inserted into the queue if there is enough space in the queue to hold the new data. If
        // not, the fetcher will suspend until there is space.
        if (fetcherState == 3 && pixelQueue.size() <= 8) {
            this.fetcherState = 0;
            fetchBackground(lcdc, ly);
        }

        // Sprite data does not get inserted into the queue in the same way that background and window tiles are.
        // Instead, sprite data overlays existing tile data, so there must be at least 8 elements worth of data in the
        // pixel FIFO. If not, the fetcher will suspend until enough data is available.
        if (fetcherState == 3 && isFetchingSprite && pixelQueue.size() >= 8) {
            this.fetcherState = 0;
            fetchSprite(lcdc, ly);
            checkFetchSprite(lcdc);
        }
    }

    /**
     * Fetches a background tile from memory and inserts it into the pixel queue.
     *
     * @param lcdc The LCDC register value.
     * @param ly   The LY register value.
     */
    private void fetchBackground(int lcdc, int ly) {
        // Determine the pixel coordinate of the tile to fetch from the 256x256 tile map.
        int mapX;
        int mapY;
        if (isFetchingWindow) {
            mapX = (lx - wx + pixelQueue.size()) & 0xFF;
            mapY = (ly - wy) & 0xFF;
        } else {
            mapX = (scx + lx + pixelQueue.size()) & 0xFF;
            mapY = (scy + ly) & 0xFF;
        }

        // Calculate the coordinate of the tile to fetch from the 32x32 tile map.
        final var tileX = mapX / 8;
        final var tileY = mapY / 8;

        // Calculate the row within the tile to fetch.
        var tileRow = mapY % 8;

        // Calculate the address of the tile map data.
        final var mapIndex = (tileY * 32) + tileX;
        final var mapAddress = (BitUtils.isSet(lcdc, isFetchingWindow ? 6 : 3) ? 0x9C00 : 0x9800) + mapIndex;

        var palette = 0;
        var bank = 0;
        var flipH = false;
        var hasPriority = false;
        if (isCGB) {
            final var flags = vram.get(1, mapAddress);

            palette = flags & 0x7;
            bank = BitUtils.getBit(flags, 3);
            flipH = BitUtils.isSet(flags, 5);
            hasPriority = BitUtils.isSet(flags, 7);

            if (BitUtils.isSet(flags, 6)) {
                tileRow = 7 - tileRow;
            }
        }

        // Calculate the address of the tile data.
        int dataAddress;
        if (BitUtils.isSet(lcdc, 4)) {
            dataAddress = 0x8000 + (vram.getUnsigned(0, mapAddress) * 16) + (tileRow * 2);
        } else {
            dataAddress = 0x9000 + (vram.get(0, mapAddress) * 16) + (tileRow * 2);
        }

        final var source = isFetchingWindow ? PixelSource.WINDOW : PixelSource.BACKGROUND;
        fetchTile(source, dataAddress, bank, palette, 0, 0, 0, flipH, hasPriority);
    }

    /**
     * Fetches a sprite tile from memory and inserts it into the pixel queue.
     *
     * @param lcdc The LCDC register value.
     * @param ly   The LY register value.
     */
    private void fetchSprite(int lcdc, int ly) {
        final int spriteID = requireNonNull(visibleSprites.poll());
        final var address = AddressUtils.getSpriteAddress(spriteID);

        final var spriteY = memory.getUnsigned(address);
        final var spriteX = memory.getUnsigned(address + 1);
        var tileID = memory.getUnsigned(address + 2);
        final var flags = memory.getUnsigned(address + 3);

        final var isSprite16 = BitUtils.isSet(lcdc, 2);

        final var spriteHeight = isSprite16 ? 16 : 8;

        var tileRow = ly - (spriteY - 16);
        if (BitUtils.isSet(flags, 6)) {
            tileRow = spriteHeight - tileRow - 1;
        }

        if (isSprite16) {
            if (tileRow >= 8) {
                tileID |= 0x1; // Lower tile
                tileRow -= 8;
            } else {
                tileID &= 0xFE; // Upper Tile
            }
        }

        int palette;
        int bank;
        if (isCGB) {
            palette = flags & 0x7;
            bank = BitUtils.getBit(flags, 3);
        } else {
            palette = BitUtils.getBit(flags, 4);
            bank = 0;
        }

        final var flipH = BitUtils.isSet(flags, 5);
        final var hasPriority = !BitUtils.isSet(flags, 7);

        final var dataAddress = 0x8000 + (tileID * 16) + (tileRow * 2);

        final var clipOffset = 8 - (spriteX - lx);
        fetchTile(PixelSource.SPRITE, dataAddress, bank, palette, spriteX, spriteID, clipOffset, flipH, hasPriority);
    }

    /**
     * Fetches the specified tile from memory and inserts it into the pixel queue.
     *
     * @param src         The type of graphic that is being fetched. This determines how the pixels are inserted into the queue.
     * @param address     The memory address of the tile data to fetch.
     * @param bank        The VRAM bank that contains the tile data.
     * @param palette     The color palette to colorize the pixels with.
     * @param spriteX     The coordinate of the sprite.
     * @param spriteID    The coordinate of the sprite.
     * @param clipX       How many pixels to discard due to clipping on the left edge of the screen.
     * @param flipH       Whether the tile should be flipped horizontally.
     * @param hasPriority Whether this tile has priority. Determines the z-order of the pixels in the queue.
     */
    private void fetchTile(PixelSource src, int address, int bank, int palette, int spriteX, int spriteID, int clipX, boolean flipH, boolean hasPriority) {
        // Each row of pixels in an 8x8 tile consist of two bytes. The first byte contains the low bit for each pixel,
        // and the second byte contains the high bit for each pixel.
        final var dataLow = vram.getUnsigned(bank, address);
        final var dataHigh = vram.getUnsigned(bank, address + 1);

        final var step = flipH ? 1 : -1;
        var bit = (flipH ? 0 : 7) + clipX * step;

        // FIXME
        //  Super Mario Land: Figure out why Mario's sprite reverses when it clips off the left edge of the screen.

        final var width = 8 - clipX;
        for (var i = 0; i < width; ++i) {
            final var color = (BitUtils.getBit(dataHigh, bit) << 1) | BitUtils.getBit(dataLow, bit);
            bit += step;

            // Background pixels are inserted at the end of the pixel queue, while Sprite pixels are drawn on top of the
            // pixels that are already present in the pixel queue based on their priority.
            if (src == PixelSource.SPRITE) {
                // Sprite pixels with color 0 are always transparent, so they are not inserted into the queue.
                if (color != 0) {
                    final var entry = pixelQueue.get(i);

                    if (entry.getSource() != PixelSource.SPRITE) {
                        // Sprites pixels with priority will always draw over Background pixels unless the Background
                        // pixels also have priority (in which case, they are treated as a foreground layer). Sprites
                        // will always draw over Background pixels with a color of 0, regardless of priority.
                        if ((!entry.hasPriority() && hasPriority) || entry.getColor() == 0) {
                            entry.set(color, palette, true, spriteX, spriteID);
                        }
                    } else {
                        // When two sprites overlap in DMG mode, the one with the smaller X coordinate gets higher
                        // priority. In CGB mode (or when the overlapping sprites have the same X coordinate in DMG
                        // mode), the sprite with the smallest ID gets higher priority.
                        if (!isCGB && entry.getSpriteX() != spriteX) {
                            if (spriteX < entry.getSpriteX()) {
                                entry.set(color, palette, true, spriteX, spriteID);
                            }
                        } else {
                            if (spriteID < entry.getSpriteID()) {
                                entry.set(color, palette, true, spriteX, spriteID);
                            }
                        }
                    }
                }
            } else {
                pixelQueue.push(src, color, palette, hasPriority);
            }
        }
    }

    /**
     * This mode occurs at the end of each visible (0-143) scanline. Its sole purpose is to pad each scanline to exactly
     * 456 clock cycles. During this time, the HDMA service can also tick if active.
     *
     * @return {@code true} if this tick marked the end of the current frame.
     */
    private boolean tickHBlank() {
        if (currentScanlineCycles >= SCANLINE_CYCLES) {
            currentScanlineCycles -= SCANLINE_CYCLES;

            final var ly = memory.getUnsigned(AddressUtils.LY) + 1;
            setLY(ly);

            if (ly == VBLANK_START_SCANLINE) {
                setStatusMode(LCDMode.VBLANK, 0);
                return true;
            } else {
                setStatusMode(LCDMode.SEARCH, 0);
            }
        }
        return false;
    }

    /**
     * This mode occurs at the end of the frame and lasts from scanline (144-154). During this mode, the hardware is
     * idle and the CPU may freely access any area of memory.
     */
    private void tickVBlank() {
        if (currentScanlineCycles >= SCANLINE_CYCLES) {
            currentScanlineCycles -= SCANLINE_CYCLES;

            var ly = memory.getUnsigned(AddressUtils.LY) + 1;
            if (ly == VBLANK_END_SCANLINE) {
                ly = 0;
                setStatusMode(LCDMode.SEARCH, 0);
            }
            setLY(ly);
        }
    }

    /**
     * Sets the LCD's status mode.
     *
     * @param mode           The status mode.
     * @param overflowCycles The number of overflow cycles.
     */
    private void setStatusMode(LCDMode mode, int overflowCycles) {
        // Update the STAT register to reflect the new status mode.
        final var stat = memory.getUnsigned(AddressUtils.STAT);
        memory.set(AddressUtils.STAT, (stat & 0xFC) | mode.ordinal());

        this.overflowCycles = overflowCycles;

        // The status mode changed so check the signal.
        updateStatInterruptSignal();

        switch (mode) {
            case VBLANK:
                interrupts.setRequested(Interrupt.V_BLANK, true);
                if (isLCDEnabling) {
                    this.isLCDEnabling = false;
                    framebuffer.fill(0xFFFFFF);
                }
                break;
            case HBLANK:
            case SEARCH:
                break;
            case TRANSFER:
                // These registers are cached at the beginning of each pixel transfer. Any changes made to them are not
                // applied until the next transfer period.
                this.scx = memory.getUnsigned(AddressUtils.SCX);
                this.scy = memory.getUnsigned(AddressUtils.SCY);
                this.wx = memory.getUnsigned(AddressUtils.WX) - 7;
                this.wy = memory.getUnsigned(AddressUtils.WY);

                this.isFetchingWindow = false;
                this.isFetchingSprite = false;
                this.fetcherState = 0;

                this.numPixelsToDiscard = scx % 8;

                pixelQueue.clear();
                setLX(0);

                break;
        }

        // Notify the world that the mode changed.
        videoModeEventDispatcher.broadcast(new VideoModeEvent(this, mode));
    }

    /**
     * Sets the X coordinate of the next pixel to transfer to the LCD's framebuffer.
     *
     * @param lx The value to set.
     */
    private void setLX(int lx) {
        this.lx = lx;

        final var lcdc = memory.getUnsigned(AddressUtils.LCDC);
        final var ly = memory.getUnsigned(AddressUtils.LY);

        // The pixel FIFO must be cleared and the fetcher reset when switching between the background and window maps.
        boolean shouldFetchWindow = BitUtils.isSet(lcdc, 5) && lx >= wx && ly >= wy;
        if (isFetchingWindow != shouldFetchWindow) {
            this.isFetchingWindow = shouldFetchWindow;
            this.fetcherState = 0;

            pixelQueue.clear();
        }

        // Check if a sprite needs to be fetched.
        checkFetchSprite(lcdc);
    }

    /**
     * Checks whether a sprite should be fetched given the current LX value.
     *
     * @param lcdc The value of the LCDC register.
     */
    private void checkFetchSprite(int lcdc) {
        this.isFetchingSprite = false;
        if (BitUtils.isSet(lcdc, 1) && !visibleSprites.isEmpty()) {
            final var sprite = visibleSprites.peek();
            final var spriteX = memory.getUnsigned(AddressUtils.getSpriteAddress(sprite) + 1);

            if (lx + 8 >= spriteX) {
                this.isFetchingSprite = true;
                this.fetcherState = 0;
            }
        }
    }

    /**
     * Sets the value of the LY register.
     *
     * @param ly The value to set.
     */
    private void setLY(int ly) {
        memory.set(AddressUtils.LY, ly);
        updateLYCompareBit();
    }

    /**
     * Updates the LY comparison bit.
     */
    private void updateLYCompareBit() {
        final var ly = memory.getUnsigned(AddressUtils.LY);
        final var lyc = memory.getUnsigned(AddressUtils.LYC);

        // Update the state of the STAT register.
        var stat = memory.getUnsigned(AddressUtils.STAT);
        if (ly == lyc) {
            stat = BitUtils.setBit(stat, 2);
        } else {
            stat = BitUtils.clearBit(stat, 2) & 0xFF;
        }
        memory.set(AddressUtils.STAT, stat);

        // Update the interrupt signal.
        updateStatInterruptSignal();
    }

    /**
     * Updates the internal signal for generating LCD_STAT interrupts. This interrupt can occur when the status mode
     * changes or when the LY = LYC flag changes. The interrupt is only generated on a rising edge so not all triggers
     * will necessarily be detected.
     */
    private void updateStatInterruptSignal() {
        final var oldSignal = irqSignal;

        final var stat = memory.getUnsigned(AddressUtils.STAT);
        final var mode = LCDMode.get(stat);

        // Signal is high when current LCD mode bit is enabled or the LY == LYC
        // bit is enabled and LY == LYC.
        this.irqSignal = BitUtils.isSet(stat, mode.getInterruptBit())
                | BitUtils.isSet(stat, 6) && BitUtils.isSet(stat, 2);

        // Interrupt is only requested on the signal's rising edge.
        if (!oldSignal && irqSignal) {
            interrupts.setRequested(Interrupt.LCD_STAT, true);
        }
    }

    /**
     * Updates the LCDC register.
     */
    private void updateLCDCRegister() {
        final var lcdc = memory.getUnsigned(AddressUtils.LCDC);

        // Bit 7 determines whether the video hardware is powered on or not. The hardware state should reset to its
        // default state whenever the power toggles.
        if (isLCDEnabled != BitUtils.isSet(lcdc, 7)) {
            this.isLCDEnabled = !isLCDEnabled;

            this.currentScanlineCycles = 0;
            this.overflowCycles = 0;
            this.isFetchingSprite = false;
            this.isFetchingWindow = false;

            pixelQueue.clear();
            visibleSprites.clear();

            if (!isLCDEnabled) {
                setLY(0);
                setStatusMode(LCDMode.HBLANK, 0);
            } else {
                this.isLCDEnabling = true;
            }
        }
    }

    /**
     * Registers a callback to be executed whenever a cartridge is loaded.
     *
     * @param callback The callback to register.
     * @return The callback handle.
     */
    public EventDispatcherHandle<VideoModeEvent> bindVideoModeEvent(Consumer<VideoModeEvent> callback) {
        return videoModeEventDispatcher.bind(callback);
    }

    /**
     * Gets the Framebuffer the video hardware renders pixels to.
     *
     * @return The framebuffer.
     */
    public Framebuffer getFramebuffer() {
        return framebuffer;
    }

}