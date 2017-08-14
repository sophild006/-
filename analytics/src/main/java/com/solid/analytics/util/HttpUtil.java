package com.solid.analytics.util;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.zip.GZIPInputStream;

public class HttpUtil {

    public static final String TAG = "HttpUtil";

    public static InputStream doGetInputStream(String url) {
        try {
            final URL u = new URL(url);
            final HttpURLConnection conn = (HttpURLConnection) u.openConnection();

            conn.setConnectTimeout(60000);
            conn.setReadTimeout(60000);

            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setUseCaches(false);

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/x-stream");
            conn.setRequestProperty("Accept", "application/x-stream");
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setRequestProperty("User-Agent", "Java/Android");

            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Keep-Alive", "5000");
            conn.setRequestProperty("Http-version", "HTTP/1.1");

            conn.connect();

            final int responseCode = conn.getResponseCode();
            if (responseCode != 200)
                return null;

            // Read the responses
            final InputStream is;
            final String contentEncoding = conn.getContentEncoding();
            final boolean gzipped = contentEncoding != null && contentEncoding.toLowerCase(Locale.US).contains("gzip");
            final InputStream origin = conn.getInputStream();
            if (gzipped) {
                is = new GZIPInputStream(origin);
            } else {
                is = origin;
            }

            return is;
        } catch (Exception e) {
            return null;
        }
    }

    public static String doGet(String url) throws Exception {
        InputStream is = doGetInputStream(url);
        if (is == null)
            return null;

        try {
            return StringUtil.toString(is, "utf-8");
        } finally {
            IOUtil.closeQuietly(is);
        }
    }

    public static byte[] doGetBytes(String url) throws Exception {
        InputStream is = doGetInputStream(url);
        if (is == null)
            return null;

        try {
            return IOUtil.toBytes(is);
        } finally {
            IOUtil.closeQuietly(is);
        }
    }
}
