package com.solid.analytics.transport;

import android.content.Context;
import android.util.Base64;

import com.solid.analytics.BuildConfig;
import com.solid.analytics.model.Config;
import com.solid.analytics.model.Response;
import com.solid.analytics.util.AndroidUtil;
import com.solid.analytics.util.IOUtil;
import com.solid.analytics.util.StringUtil;
import com.solid.analytics.util.log.Logger;
import com.solid.analytics.util.log.LoggerFactory;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class HttpTransport implements Transport {

    static final Logger log = LoggerFactory.getLogger(HttpTransport.class);

    final Context mContext;
    final Config mConfig;
    final String mUrl;
    final String mDeviceId;
    final String mKey;

    public HttpTransport(Context context, Config config, String url, String deviceId, String key) {
        mContext = context;
        mConfig = config;
        mUrl = url;
        mDeviceId = deviceId;
        mKey = key;
    }

    @Override
    public Response transfer(byte[] data) {
        if (!AndroidUtil.isNetworkAvailable(mContext)) {
            log.warn("transfer fail:" + " network not available!");
            return null;
        }

        try {
            final Config cf = mConfig;

            if (log.isDebugEnabled())
                log.debug("transfer begin:" + " url:" + mUrl + " data:" + StringUtil.toDebugString(data, "utf-8"));// + " encrypt:" + StringUtil.toDebugString(encrypt(data, mKey), "utf-8"));

            if (BuildConfig.DEBUG)
                log.debug("transfer encrypt:" + " url:" + mUrl + " encrypt:" + StringUtil.toDebugString(encrypt(data, mKey), "utf-8"));

            byte[] encryptedData = encrypt(data, mKey);

            // prepare headers
            Map<String, String> headerValues = new HashMap<String, String>();

            // sort headers
            List<String> headers = new ArrayList<String>(headerValues.size());
            headers.addAll(headerValues.keySet());
            Collections.sort(headers);

            // start connect
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            // add headers
            for (String header : headerValues.keySet()) {
                conn.setRequestProperty(header, headerValues.get(header));
            }
            conn.setRequestProperty("User-Agent", "Android");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setRequestProperty("Content-Type", "application/json");

            // write data
            OutputStream os = conn.getOutputStream();
            os.write(encryptedData);
            os.flush();

            // response code
            int responseCode = conn.getResponseCode();
            if (responseCode >= 400) {
                log.warn("transfer fail:" + " responseCode:" + responseCode);
                return null;
            }

            // response data
            byte[] responseData = IOUtil.toBytes(conn.getInputStream());
            byte[] decryptedData = decrypt(responseData, mKey);
            String decryptedText = new String(decryptedData, "utf-8");
            if (log.isDebugEnabled())
                log.debug("transfer result:" + " url:" + mUrl + " result:" + decryptedText + " data:" + StringUtil.toDebugString(data, "utf-8"));
            JSONObject res = new JSONObject(decryptedText);
            Response response = new Response();
            response.read(res);
            return response;
        } catch (Exception e) {
            log.warn("transfer fail:", e);
            return null;
        }
    }

    private static byte[] encrypt(final byte[] data, String key) {
        try {
            Key secretKey = getKey(key);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            final byte[] encryptData = cipher.doFinal(data);
            return Base64.encode(encryptData, Base64.NO_WRAP);
        } catch (final Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    private static byte[] decrypt(final byte[] data, String key) {
        try {
            Key secretKey = getKey(key);
            Cipher cipher = Cipher.getInstance("DES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            final byte[] base64De = Base64.decode(data, Base64.NO_WRAP);
            final byte[] decryptData = cipher.doFinal(base64De);
            return decryptData;
        } catch (final Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static Key getKey(String key) {
        try {
            byte[] keyByte = key.getBytes();
            byte[] byteTemp = new byte[8];
            for (int i = 0; i < byteTemp.length && i < keyByte.length; i++) {
                byteTemp[i] = keyByte[i];
            }
            return new SecretKeySpec(byteTemp, "DES");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
