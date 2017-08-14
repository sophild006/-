package com.solid.analytics.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 {

    public static String sha1(byte[] bin, boolean upperCase) throws NoSuchAlgorithmException {
        return Hex.hex(sha1bin(bin), upperCase);
    }

    public static String sha1(byte[] bin) throws NoSuchAlgorithmException {
        return sha1(bin, false);
    }

    public static String sha1(String string, boolean upperCase) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return Hex.hex(sha1bin(string), upperCase);
    }

    public static String sha1(String string) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return sha1(string, false);
    }

    public static String sha1(InputStream is, boolean upperCase) throws NoSuchAlgorithmException, IOException {
        return Hex.hex(sha1bin(is), upperCase);
    }

    public static String sha1(InputStream is) throws NoSuchAlgorithmException, IOException {
        return sha1(is, false);
    }

    public static String sha1(File file, boolean upperCase) throws NoSuchAlgorithmException, IOException {
        return Hex.hex(sha1bin(file), upperCase);
    }

    public static String sha1(File file) throws NoSuchAlgorithmException, IOException {
        return sha1(file, false);
    }

    public static byte[] sha1bin(byte[] bin) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        return digest.digest(bin);
    }

    public static byte[] sha1bin(String string) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        return sha1bin(string.getBytes("utf-8"));
    }

    public static byte[] sha1bin(InputStream is) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA1");
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
        }
        return digest.digest();
    }

    public static byte[] sha1bin(File file) throws NoSuchAlgorithmException, IOException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            return sha1bin(is);
        } finally {
            IOUtil.closeQuietly(is);
        }
    }
}
