package org.guide.util.fps;

/**
 * A frame rate event that is emitted periodically by frame rate tickers.
 *
 * @param ticker The ticker that generated the event.
 * @param fps    The frame rate.
 * @author Brendan Jones
 */
public record FrameRateEvent(FrameRateTicker ticker, int fps) {
}
