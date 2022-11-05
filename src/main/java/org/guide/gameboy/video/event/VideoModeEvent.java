package org.guide.gameboy.video.event;

import org.guide.gameboy.video.LCDMode;
import org.guide.gameboy.video.Video;

/**
 * The Video Mode event is fired whenever the video hardware's LCD mode changes.
 *
 * @param video The video hardware that fired the event.
 * @param mode  The mode.
 * @author Brendan Jones
 */
public record VideoModeEvent(Video video, LCDMode mode) {
}
