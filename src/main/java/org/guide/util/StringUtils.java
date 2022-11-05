package org.guide.util;

/**
 * A collection of utilities for working with Strings.
 *
 * @author Brendan Jones
 */
public class StringUtils {

    /**
     * A lookup table for all 8-bit hex strings.
     */
    private static final String[] HEX_STRINGS = new String[0x100];

    static {
        for (int i = 0; i < HEX_STRINGS.length; ++i) {
            HEX_STRINGS[i] = String.format("%02X", i);
        }
    }

    /**
     * Gets the hexadecimal representation of an 8-bit integer. If necessary, the
     * String will be padded with leading zeroes.
     *
     * @param value The value.
     * @return The hex String.
     */
    public static String getHex8(int value) {
        return HEX_STRINGS[value & 0xFF];
    }

    /**
     * Gets the hexadecimal representation of a 16-bit integer. If necessary, the
     * String will be padded with leading zeroes.
     *
     * @param value The value.
     * @return The hex String.
     */
    public static String getHex16(int value) {
        return HEX_STRINGS[(value >> 8) & 0xFF] + HEX_STRINGS[value & 0xFF];
    }

//	public static String getKeyName(int key) {
//		if(key >= GLFW_KEY_0 && key <= GLFW_KEY_9) {
//			return String.valueOf(key - GLFW_KEY_0);
//		} else if(key >= GLFW_KEY_A && key <= GLFW_KEY_Z) {
//			return String.valueOf((char) ('A' + (key - GLFW_KEY_A)));
//		} else if(key >= GLFW_KEY_F1 && key <= GLFW_KEY_F25) {
//			return "F" + (key - GLFW_KEY_F1);
//		} else if(key >= GLFW_KEY_KP_0 && key <= GLFW_KEY_KP_9) {
//			return String.valueOf(key - GLFW_KEY_KP_0);
//		} else {
//			switch(key) {
//				case GLFW_KEY_ESCAPE:
//					return "ESC";
//				case GLFW_KEY_GRAVE_ACCENT:
//					return "`";
//				case GLFW_KEY_MINUS:
//					return "-";
//				case GLFW_KEY_EQUAL:
//					return "=";
//				case GLFW_KEY_BACKSPACE:
//					return "BACKSPACE";
//				case GLFW_KEY_TAB:
//					return "TAB";
//				case GLFW_KEY_LEFT_BRACKET:
//					return "[";
//				case GLFW_KEY_RIGHT_BRACKET:
//					return "]";
//				case GLFW_KEY_BACKSLASH:
//					return "\\";
//				case GLFW_KEY_CAPS_LOCK:
//					return "CAPS LOCK";
//				case GLFW_KEY_SEMICOLON:
//					return ";";
//				case GLFW_KEY_APOSTROPHE:
//					return "'";
//				case GLFW_KEY_ENTER:
//					return "ENTER";
//				case GLFW_KEY_LEFT_SHIFT:
//					return "LEFT SHIFT";
//				case GLFW_KEY_COMMA:
//					return ",";
//				case GLFW_KEY_PERIOD:
//					return ".";
//				case GLFW_KEY_SLASH:
//					return "/";
//				case GLFW_KEY_RIGHT_SHIFT:
//					return "RIGHT SHIFT";
//				case GLFW_KEY_LEFT_CONTROL:
//					return "LEFT CTRL";
//				case GLFW_KEY_LEFT_ALT:
//					return "LEFT ALT";
//				case GLFW_KEY_SPACE:
//					return "SPACE";
//				case GLFW_KEY_RIGHT_ALT:
//					return "RIGHT ALT";
//				case GLFW_KEY_RIGHT_CONTROL:
//					return "RIGHT CTRL";
//				case GLFW_KEY_UP:
//					return "UP";
//				case GLFW_KEY_RIGHT:
//					return "RIGHT";
//				case GLFW_KEY_DOWN:
//					return "DOWN";
//				case GLFW_KEY_LEFT:
//					return "LEFT";
//				case GLFW_KEY_KP_DECIMAL:
//					return "KEYPAD .";
//				case GLFW_KEY_KP_ENTER:
//					return "KEYPAD ENTER";
//			}
//		}
//	}

}
