package org.guide.gameboy.processor.interrupts.memory;

import org.guide.gameboy.SerializableComponent;

import java.nio.ByteBuffer;
import java.util.Arrays;

import static java.util.Objects.requireNonNull;

/**
 * A memory bank is an area of memory consisting of several banks that can be swapped into system memory.
 *
 * @author Brendan Jones
 */
public class MemoryBank implements SerializableComponent {

    /**
     * The memory component this bank belongs to.
     */
    private final Memory memory;

    /**
     * Whether this is a unified memory bank. In a unified memory bank, all banks occupy the same area of memory
     * and can be dynamically swapped at runtime. In a non-unified memory bank, bank 0 is always active and occupies
     * a separate memory space from the switchable banks.
     */
    private final boolean isUnified;

    /**
     * Whether the memory bank is mutable.
     */
    private final boolean isMutable;

    /**
     * The start address of the memory bank in system memory.
     */
    private final int startAddress;

    /**
     * The end address of the memory bank in system memory.
     */
    private final int endAddress;

    /**
     * The number of banks managed by this memory bank.
     */
    private final int numBanks;

    /**
     * The size of each individual bank.
     */
    private final int bankSize;

    /**
     * The underlying data for each bank.
     */
    private final byte[][] banks;

    /**
     * The currently selected bank.
     */
    private int activeBank;

    /**
     * Creates a new MemoryBank instance.
     *
     * @param memory      The memory component to bind to.
     * @param isUnified   Whether this is a unified bank. In a unified memory bank, all banks occupy the same memory space
     *                    and only one will be active at a time. Otherwise, bank 0 will always be active, and will occupy
     *                    a memory space separate from the other banks.
     * @param isMutable   Whether this memory bank is mutable.
     * @param baseAddress The base address of the memory bank.
     * @param numBanks    The number of banks to create.
     * @param bankSize    The size of each bank.
     */
    public MemoryBank(Memory memory, boolean isUnified, boolean isMutable, int baseAddress, int numBanks, int bankSize) {
        this.memory = requireNonNull(memory);
        this.isUnified = isUnified;
        this.isMutable = isMutable;
        this.startAddress = baseAddress;
        this.endAddress = startAddress + bankSize * (isUnified ? 1 : 2);
        this.numBanks = numBanks;
        this.bankSize = bankSize;
        this.banks = new byte[numBanks][bankSize];
    }

    /**
     * Resets the memory bank to its default state.
     *
     * @param data The data to initialize the bank with.
     */
    public void reset(byte[] data) {
        this.activeBank = isUnified ? 0 : 1;
        setData(data);

        updateMemoryMap(true);
    }

    /**
     * Synchronizes the memory banks with main memory.
     *
     * @param fullSync Whether to also synchronize bank 0 in a non-unified memory bank. This does nothing in a unified
     *                 memory bank.
     */
    private void synchronizeMemoryBanks(boolean fullSync) {
        if (isMutable) {
            memory.getBytes(getBankBaseAddress(activeBank), banks[activeBank]);
            if (!isUnified && fullSync) {
                memory.getBytes(getBankBaseAddress(0), banks[0]);
            }
        }
    }

    @Override
    public void serialize(ByteBuffer out) {
        // Ensure that the memory banks are up-to-date.
        synchronizeMemoryBanks(true);

        SerializableComponent.writeIntegrityCheck(out);

        out.putShort((short) activeBank);
        if (isMutable) {
            for (byte[] bank : banks) {
                out.put(bank);
            }
        }
    }

    @Override
    public void deserialize(ByteBuffer in) {
        SerializableComponent.verifyIntegrityCheck(in, "MemoryBank::Start");

        int active = in.getShort() & 0xFFFF;
        if (isMutable) {
            this.activeBank = active;

            for (byte[] bank : banks) {
                in.get(bank);
            }

            updateMemoryMap(true);
        } else {
            setActiveBank(active);
        }
    }

    /**
     * Sets the data for all managed banks. The provided data will be split into bank-length sections and copied into
     * each of the underlying banks in order. If no data is provided, then the banks will have their data zeroed out.
     *
     * @param data The data to copy into the banks.
     */
    public void setData(byte[] data) {
        if (data != null) {
            final var expectedLength = numBanks * bankSize;
            if (expectedLength != data.length) {
                throw new IllegalArgumentException(
                        String.format("Length mismatch: actual=%s, expected=%s", data.length, expectedLength)
                );
            }

            // Copy the data into each of the memory banks.
            for (var i = 0; i < numBanks; ++i) {
                System.arraycopy(data, bankSize * i, banks[i], 0, bankSize);
            }
        } else {
            // Zero out the memory banks.
            for (var bank : banks) {
                Arrays.fill(bank, (byte) 0);
            }
        }
    }

    /**
     * Sets the currently active memory bank.
     *
     * @param bank The bank number.
     */
    public void setActiveBank(int bank) {
        if (bank < 0 || bank >= numBanks) {
            throw new IllegalArgumentException("Invalid bank: " + bank);
        }

        // The bank is already active, so there is nothing to switch.
        if (activeBank == bank) {
            return;
        }

        synchronizeMemoryBanks(false);

        // Switch the active bank.
        this.activeBank = bank;

        // Swap banks on the memory map.
        updateMemoryMap(false);
    }

    /**
     * Write the contents of the active memory bank to system memory.
     *
     * @param isResetting Whether the update is a result of a system reset.
     */
    private void updateMemoryMap(boolean isResetting) {
        if (isUnified) {
            // All banks are mapped the same area in memory.
            memory.setBytes(startAddress, banks[activeBank]);
        } else {
            // Bank 0 is mapped in a separate area of memory and cannot be switched, so we only need to write its
            // contents to memory when it is.
            if (isResetting) {
                memory.setBytes(startAddress, banks[0]);
            }

            // Write the newly activated bank to memory.
            memory.setBytes(getBankBaseAddress(activeBank), banks[activeBank]);
        }
    }

    /**
     * Get the raw byte data of the specified bank.
     *
     * @param bank The bank number.
     * @return The underlying buffer for the bank data.
     */
    public byte[] getBankData(int bank) {
        return banks[bank];
    }

    /**
     * Gets the signed 8-bit value for the specified address within the specified bank.
     *
     * @param bank    The bank number.
     * @param address The memory address.
     * @return The signed value.
     */
    public int get(int bank, int address) {
        if (!isUnified && bank == 0 || bank == activeBank) {
            // The desired bank is loaded into main memory and the banked copy might not be up-to-date.
            return memory.get(address);
        } else {
            // Bank is not loaded into main memory so the banked version is guaranteed to be up-to-date.
            final var bankAddress = address - getBankBaseAddress(bank);
            return banks[bank][bankAddress];
        }
    }

    /**
     * Gets the signed 8-bit value for the specified address within the specified bank.
     *
     * @param bank    The bank number.
     * @param address The memory address.
     * @return The unsigned value.
     */
    public int getUnsigned(int bank, int address) {
        return get(bank, address) & 0xFF;
    }

    /**
     * Gets the base address for the specified bank.
     *
     * @param bank The bank number.
     * @return The base address.
     */
    private int getBankBaseAddress(int bank) {
        return (isUnified || bank == 0) ? startAddress : (startAddress + banks[0].length);
    }

    /**
     * Gets the currently active bank.
     *
     * @return The bank number.
     */
    public int getActiveBank() {
        return activeBank;
    }

    /**
     * Gets the number of banks managed by this memory bank.
     *
     * @return The number of banks.
     */
    public int getNumBanks() {
        return numBanks;
    }

}
