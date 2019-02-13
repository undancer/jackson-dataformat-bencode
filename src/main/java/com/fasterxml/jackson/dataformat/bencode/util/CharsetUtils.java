package com.fasterxml.jackson.dataformat.bencode.util;

public class CharsetUtils {

    private static final int INT_009_0x09 = 0b00001001; //   9  0x09
    private static final int INT_010_0x0A = 0b00001010; //  10  0x0A
    private static final int INT_013_0x0D = 0b00001101; //  13  0x0D
    private static final int INT_032_0x20 = 0b00100000; //  32  0x20
    private static final int INT_126_0x7E = 0b01111110; // 126  0x7E
    private static final int INT_128_0x80 = 0b10000000; // 128  0x80
    private static final int INT_143_0x8F = 0b10001111; // 143  0x8F
    private static final int INT_144_0x90 = 0b10010000; // 144  0x90
    private static final int INT_159_0x9F = 0b10011111; // 159  0x9F
    private static final int INT_160_0xA0 = 0b10100000; // 160  0xA0
    private static final int INT_191_0xBF = 0b10111111; // 191  0xBF
    private static final int INT_194_0xC2 = 0b11000010; // 194  0xC2
    private static final int INT_223_0xDF = 0b11011111; // 223  0xDF
    private static final int INT_224_0xE0 = 0b11100000; // 224  0xE0
    private static final int INT_225_0xE1 = 0b11100001; // 225  0xE1
    private static final int INT_236_0xEC = 0b11101100; // 236  0xEC
    private static final int INT_237_0xED = 0b11101101; // 237  0xED
    private static final int INT_238_0xEE = 0b11101110; // 238  0xEE
    private static final int INT_239_0xEF = 0b11101111; // 239  0xEF
    private static final int INT_240_0xF0 = 0b11110000; // 240  0xF0
    private static final int INT_241_0xF1 = 0b11110001; // 241  0xF1
    private static final int INT_243_0xF3 = 0b11110011; // 243  0xF3
    private static final int INT_244_0xF4 = 0b11110100; // 244  0xF4

    // https://github.com/wayfind/is-utf8
    public static boolean isUTF8(byte[] bytes) {
        int i = 0;
        while (i < bytes.length) {
            if ((// ASCII
                    bytes[i] == INT_009_0x09 ||
                            bytes[i] == INT_010_0x0A ||
                            bytes[i] == INT_013_0x0D ||
                            (INT_032_0x20 <= bytes[i] && bytes[i] <= INT_126_0x7E)
            )
            ) {
                i += 1;
                continue;
            }

            if ((// non-overlong 2-byte
                    (INT_194_0xC2 <= bytes[i] && bytes[i] <= INT_223_0xDF) &&
                            (INT_128_0x80 <= bytes[i + 1] && bytes[i + 1] <= INT_191_0xBF)
            )
            ) {
                i += 2;
                continue;
            }

            if ((// excluding overlongs
                    bytes[i] == INT_224_0xE0 &&
                            (INT_160_0xA0 <= bytes[i + 1] && bytes[i + 1] <= INT_191_0xBF) &&
                            (INT_128_0x80 <= bytes[i + 2] && bytes[i + 2] <= INT_191_0xBF)
            ) ||
                    (// straight 3-byte
                            ((INT_225_0xE1 <= bytes[i] && bytes[i] <= INT_236_0xEC) ||
                                    bytes[i] == INT_238_0xEE ||
                                    bytes[i] == INT_239_0xEF) &&
                                    (INT_128_0x80 <= bytes[i + 1] && bytes[i + 1] <= INT_191_0xBF) &&
                                    (INT_128_0x80 <= bytes[i + 2] && bytes[i + 2] <= INT_191_0xBF)
                    ) ||
                    (// excluding surrogates
                            bytes[i] == INT_237_0xED &&
                                    (INT_128_0x80 <= bytes[i + 1] && bytes[i + 1] <= INT_159_0x9F) &&
                                    (INT_128_0x80 <= bytes[i + 2] && bytes[i + 2] <= INT_191_0xBF)
                    )
            ) {
                i += 3;
                continue;
            }

            if ((// planes 1-3
                    bytes[i] == INT_240_0xF0 &&
                            (INT_144_0x90 <= bytes[i + 1] && bytes[i + 1] <= INT_191_0xBF) &&
                            (INT_128_0x80 <= bytes[i + 2] && bytes[i + 2] <= INT_191_0xBF) &&
                            (INT_128_0x80 <= bytes[i + 3] && bytes[i + 3] <= INT_191_0xBF)
            ) ||
                    (// planes 4-15
                            (INT_241_0xF1 <= bytes[i] && bytes[i] <= INT_243_0xF3) &&
                                    (INT_128_0x80 <= bytes[i + 1] && bytes[i + 1] <= INT_191_0xBF) &&
                                    (INT_128_0x80 <= bytes[i + 2] && bytes[i + 2] <= INT_191_0xBF) &&
                                    (INT_128_0x80 <= bytes[i + 3] && bytes[i + 3] <= INT_191_0xBF)
                    ) ||
                    (// plane 16
                            bytes[i] == INT_244_0xF4 &&
                                    (INT_128_0x80 <= bytes[i + 1] && bytes[i + 1] <= INT_143_0x8F) &&
                                    (INT_128_0x80 <= bytes[i + 2] && bytes[i + 2] <= INT_191_0xBF) &&
                                    (INT_128_0x80 <= bytes[i + 3] && bytes[i + 3] <= INT_191_0xBF)
                    )
            ) {
                i += 4;
                continue;
            }

            return false;
        }

        return true;
    }
}
