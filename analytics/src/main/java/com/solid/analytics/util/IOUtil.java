package com.solid.analytics.util;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public final class IOUtil {

    private static final int COPY_BUF_SIZE = 8024;
    private static final int SKIP_BUF_SIZE = 4096;
    private static final byte[] SKIP_BUF = new byte['?'];

    private IOUtil() {
    }

    public static long copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, COPY_BUF_SIZE);
    }

    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        final byte[] buffer = new byte[bufferSize];
        long count = 0L;
        int read = 0;
        while ((read = input.read(buffer)) > 0) {
            output.write(buffer, 0, read);
            count += read;
        }
        return count;
    }

    public static long skip(InputStream is, long skip) throws IOException {
        final long dest = skip;
        while (skip > 0L) {
            long skipped = is.skip(skip);
            if (skipped == 0L)
                break;

            skip -= skipped;
        }

        while (skip > 0L) {
            int read = readFully(is, SKIP_BUF, 0, (int) Math.min(skip, SKIP_BUF_SIZE));
            if (read < 1)
                break;

            skip -= read;
        }

        return dest - skip;
    }

    public static int readFully(InputStream is, byte[] buffer) throws IOException {
        return readFully(is, buffer, 0, buffer.length);
    }

    public static int readFully(InputStream is, byte[] buffer, int offset, int len) throws IOException {
        if ((len < 0) || (offset < 0) || (len + offset > buffer.length))
            throw new IndexOutOfBoundsException();

        int count = 0;
        int x = 0;
        while (count != len) {
            x = is.read(buffer, offset + count, len - count);
            if (x == -1)
                break;

            count += x;
        }

        return count;
    }

    public static byte[] toBytes(InputStream is) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(is, output);
        return output.toByteArray();
    }

    public static byte[] toBytes(String string, String encoding) throws IOException {
        return string.getBytes(encoding);
    }

    public static byte[] toBytes(String string) throws IOException {
        return toBytes(string, "utf-8");
    }

    public static byte[] toBytes(File file) throws IOException {
        if (file == null || !file.exists() || file.isDirectory()) {
            throw new IOException("param is error!");
        }
        FileInputStream fis = new FileInputStream(file);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            copy(fis, bos);
            bos.close();
            return bos.toByteArray();
        } finally {
            closeQuietly(fis);
        }
    }

    public static void closeQuietly(Object c) {
        if (c == null)
            return;

        try {
            if (c instanceof Closeable) {
                ((Closeable) c).close();
                return;
            }

            if (c instanceof Cursor) {
                ((Cursor) c).close();
                return;
            }

            if (c instanceof SQLiteDatabase) {
                ((SQLiteDatabase) c).close();
            }

            Method m = c.getClass().getDeclaredMethod("close");
            m.invoke(c);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
