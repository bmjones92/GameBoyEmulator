package org.guide.util;

import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;

/**
 * A collection of utilities for working with JavaFX.
 *
 * @author Brendan Jones
 */
public class FXUtilities {

    /**
     * Runs a task on the JavaFX application thread and waits for it to complete.
     *
     * @param runnable The task to run.
     */
    public static void runAndWait(Runnable runnable) {
        Objects.requireNonNull(runnable, "Runnable cannot be null.");

        if (Platform.isFxApplicationThread()) {
            runnable.run();
        } else {
            final CountDownLatch sync = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    runnable.run();
                } finally {
                    sync.countDown();
                }
            });

            try {
                sync.await();
            } catch (InterruptedException e) {
                // Nothing to do.
            }
        }
    }
}
