package com.solid.analytics.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;

public class JsonUtil {

    public static JSONObject addPrefix(JSONObject obj, String prefix) throws Exception {
        if (obj == null)
            return null;

        JSONObject result = new JSONObject();
        Iterator<String> keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = obj.get(key);

            if (value instanceof JSONArray) {
                result.put(prefix + key, addPrefix((JSONArray) value, prefix));
                continue;
            }

            if (value instanceof JSONObject) {
                result.put(prefix + key, addPrefix((JSONObject) value, prefix));
                continue;
            }

            result.put(prefix + key, value);
        }

        return result;
    }

    public static JSONArray addPrefix(JSONArray arr, String prefix) throws Exception {
        if (arr == null)
            return null;

        JSONArray result = new JSONArray();
        for (int i = 0; i < arr.length(); i++) {
            Object value = arr.get(i);
            if (value instanceof JSONArray) {
                result.put(addPrefix((JSONArray) value, prefix));
                continue;
            }

            if (value instanceof JSONObject) {
                result.put(addPrefix((JSONObject) value, prefix));
                continue;
            }

            result.put(value);
        }

        return result;
    }

}
