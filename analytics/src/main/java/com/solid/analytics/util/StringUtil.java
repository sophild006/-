package com.solid.analytics.util;

import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class StringUtil {

    public static boolean isEmpty(String str) {
        if (str == null || str.length() == 0)
            return true;

        return false;
    }

    public static String toString(Object o) {
        if (o == null)
            return "";

        if (o instanceof String)
            return (String) o;

        return o.toString();
    }

    public static String toString(Bundle o) {
        if (o == null)
            return "";

        Set<String> keys = o.keySet();
        if (keys != null && keys.size() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append("Bundle {");
            for (String key : o.keySet()) {
                sb.append(key).append("=").append(toString(o.get(key))).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("}");
            return sb.toString();
        }

        return o.toString();
    }

    public static String toString(InputStream is, String encoding) throws Exception {
        if (is == null)
            return null;

        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        byte[] buffer = new byte[1024];
        int read = 0;
        while ((read = is.read(buffer)) > 0) {
            bos.write(buffer, 0, read);
        }

        return new String(bos.toByteArray(), encoding);
    }

    public static String toString(File file, String encoding) throws Exception {
        if (file == null)
            return null;

        FileInputStream fis = new FileInputStream(file);
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = fis.read(buffer)) > 0) {
                bos.write(buffer, 0, read);
            }

            return new String(bos.toByteArray(), encoding);
        } finally {
            IOUtil.closeQuietly(fis);
        }
    }

    public static String toDebugString(byte[] data, String encoding) {
        try {
            if (data == null)
                return null;

            return new String(data, encoding);
        } catch (Exception e) {
            return data.toString();
        }
    }

    public static String trimEndSplash(String s) {
        if (s == null || !s.endsWith("/"))
            return s;

        return s.substring(0, s.length() - 1);
    }

    public static Map<String, String> decodeUrlParameters(String s) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        try {
            if (s != null && s.trim().length() > 0) {
                if (s.indexOf('&') > 0) {
                    String array[] = s.split("&");
                    for (String parameter : array) {
                        String v[] = parameter.split("=");
                        params.put(URLDecoder.decode(v[0], "UTF-8"), URLDecoder.decode(v[1], "UTF-8"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return params;
    }
}
