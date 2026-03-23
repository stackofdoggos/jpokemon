package com.pokemon.gui.text.emerald;

import java.io.ByteArrayOutputStream;

/**
 * Maps Java {@link String} content to Emerald script bytes for {@code FONT_NORMAL}.
 */
public final class EmeraldCharCodec {

    private EmeraldCharCodec() {
    }

    /**
     * Encode text for the field printer. Appends {@link EmeraldCharacters#EOS}.
     */
    public static byte[] encode(String text) {
        if (text == null || text.isEmpty()) {
            return new byte[] { EmeraldCharacters.EOS };
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < text.length();) {
            int cp = text.codePointAt(i);
            i += Character.charCount(cp);
            if (cp == '\n') {
                out.write(EmeraldCharacters.CHAR_NEWLINE & 0xff);
            } else if (cp <= 127) {
                out.write(mapAscii((char) cp) & 0xff);
            } else {
                out.write(EmeraldCharacters.CHAR_SPACE & 0xff);
            }
        }
        out.write(EmeraldCharacters.EOS & 0xff);
        return out.toByteArray();
    }

    private static byte mapAscii(char c) {
        if (c == ' ') {
            return EmeraldCharacters.CHAR_SPACE;
        }
        if (c >= 'A' && c <= 'Z') {
            return (byte) (EmeraldCharacters.CHAR_A + (c - 'A'));
        }
        if (c >= 'a' && c <= 'z') {
            return (byte) (EmeraldCharacters.CHAR_a + (c - 'a'));
        }
        if (c >= '0' && c <= '9') {
            return (byte) (EmeraldCharacters.CHAR_0 + (c - '0'));
        }
        return switch (c) {
            case '!' -> EmeraldCharacters.CHAR_EXCL_MARK;
            case '?' -> EmeraldCharacters.CHAR_QUESTION_MARK;
            case '.' -> EmeraldCharacters.CHAR_PERIOD;
            case '-' -> EmeraldCharacters.CHAR_HYPHEN;
            case ',' -> EmeraldCharacters.CHAR_COMMA;
            case ':' -> EmeraldCharacters.CHAR_COLON;
            case '/' -> EmeraldCharacters.CHAR_SLASH;
            case '(' -> EmeraldCharacters.CHAR_LEFT_PAREN;
            case ')' -> EmeraldCharacters.CHAR_RIGHT_PAREN;
            case '"' -> EmeraldCharacters.CHAR_DBL_QUOTE_LEFT;
            case '\'' -> EmeraldCharacters.CHAR_SGL_QUOTE_LEFT;
            default -> EmeraldCharacters.CHAR_SPACE;
        };
    }

    /**
     * @param opcodeIndex index of the byte immediately after {@link EmeraldCharacters#EXT_CTRL_CODE_BEGIN}
     * @return index of the first byte after this extended control sequence
     */
    public static int indexAfterExtCtrl(byte[] raw, int opcodeIndex) {
        return skipExtCtrl(raw, opcodeIndex);
    }

    /** Strip extension codes from a copy used only for layout (skip {@code FC …} runs). */
    public static byte[] stripForLayout(byte[] raw) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length);
        int i = 0;
        while (i < raw.length) {
            byte b = raw[i];
            if (b == EmeraldCharacters.EXT_CTRL_CODE_BEGIN) {
                i = skipExtCtrl(raw, i + 1);
                continue;
            }
            out.write(b);
            i++;
        }
        return out.toByteArray();
    }

    private static int skipExtCtrl(byte[] raw, int i) {
        if (i >= raw.length) {
            return i;
        }
        int op = raw[i] & 0xff;
        i++;
        int skipArgs = switch (op) {
            case 0x01, 0x02, 0x03 -> 1;
            case 0x04 -> 3;
            case 0x05 -> 1;
            case 0x06, 0x07 -> 0;
            case 0x08, 0x09, 0x0A -> 1;
            case 0x0B -> 2;
            case 0x0C -> 0;
            case 0x0D, 0x0E -> 1;
            case 0x0F -> 0;
            case 0x10 -> 2;
            case 0x11, 0x12, 0x13 -> 1;
            case 0x14 -> 1;
            case 0x15, 0x16, 0x17, 0x18 -> 0;
            default -> 0;
        };
        for (int k = 0; k < skipArgs && i < raw.length; k++) {
            i++;
        }
        return i;
    }
}
