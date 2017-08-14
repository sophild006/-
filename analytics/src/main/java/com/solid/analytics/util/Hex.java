package com.solid.analytics.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Hex {

    public static String hex(byte[] bytes, boolean upperCase) {
        if (bytes == null)
            return null;

        final char[] digits = upperCase ? DIGITS_UPPERCASE : DIGITS_LOWERCASE;

        final StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(digits[0xf & (b >> 4)]);
            sb.append(digits[0xf & (b)]);
        }

        return sb.toString();
    }

    public static String hex(byte[] bytes) {
        return hex(bytes, false);
    }

    public static String hex(byte[] bytes, int start, int length, boolean upperCase) {
        if (bytes == null)
            return null;

        if (start < 0 || length < 0 || (start + length) > bytes.length)
            throw new IllegalArgumentException();

        final char[] digits = upperCase ? DIGITS_UPPERCASE : DIGITS_LOWERCASE;

        final StringBuilder sb = new StringBuilder(length * 2);
        final int end = start + length;
        for (int i = start; i < end; i++) {
            final byte b = bytes[i];
            sb.append(digits[0xf & (b >> 4)]);
            sb.append(digits[0xf & (b)]);
        }

        return sb.toString();
    }

    public static String hex(byte[] bytes, int start, int length) {
        return hex(bytes, start, length, false);
    }

    public static String hex(InputStream is, boolean upperCase) throws IOException {
        final char[] digits = upperCase ? DIGITS_UPPERCASE : DIGITS_LOWERCASE;
        final StringBuilder sb = new StringBuilder(1024);

        byte[] bytes = new byte[1024];
        int read = 0;
        while ((read = is.read(bytes)) > 0) {
            for (int i = 0; i < read; i++) {
                final byte b = bytes[i];
                sb.append(digits[0xf & (b >> 4)]);
                sb.append(digits[0xf & (b)]);
            }
        }

        return sb.toString();
    }

    public static String hex(InputStream is) throws IOException {
        return hex(is, false);
    }

    public static String hex(File file, boolean upperCase) throws IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return hex(is, upperCase);
        } finally {
            IOUtil.closeQuietly(is);
        }
    }

    public static String hex(File file) throws IOException {
        return hex(file, false);
    }

    public static int fromHex(char c) {
        if (c >= 'a' && c <= 'f')
            return (c - 'a') + 10;
        if (c >= 'A' && c <= 'F')
            return (c - 'A') + 10;
        if (c >= '0' && c <= '9')
            return (c - '0');

        throw new IllegalArgumentException("must be hex!");
    }

    public static byte[] fromHex(String hex) {
        if (hex == null)
            return null;

        if (hex.length() % 2 != 0)
            throw new IllegalArgumentException("length must be even!");

        byte[] bin = new byte[hex.length() / 2];
        for (int i = 0; i < bin.length; i++) {
            bin[i] = (byte) ((fromHex(hex.charAt(i * 2)) << 4) | (fromHex(hex.charAt(i * 2 + 1))));
        }

        return bin;
    }

    private static final char[] DIGITS_LOWERCASE = {
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'a',
            'b',
            'c',
            'd',
            'e',
            'f',};

    private static final char[] DIGITS_UPPERCASE = {
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F',};
}
