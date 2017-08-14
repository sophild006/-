package com.solid.analytics.thrift;

import com.solid.analytics.thrift.protocol.TField;
import com.solid.analytics.thrift.protocol.TJSONProtocol;
import com.solid.analytics.thrift.transport.TIOStreamTransport;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;

public class ThriftUtil {

    public static void init(byte[] key, byte[] iv) throws Exception {
        TField.initDecrypter(key, iv);
    }

    static byte[] dump(byte[] bs) {

        //        if (bs != null) {
        //            try {
        //                String s = new String(bs, "utf-8");
        //                log.info("dump:" + s);
        //            } catch (Exception e) {
        //                log.warn("dump" + e);
        //            }
        //        } else {
        //            log.info("dump:");
        //        }

        return bs;
    }

    public static byte[] serialize(TBase obj) {
        try {
            if (obj == null)
                return null;

            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            TIOStreamTransport tios = new TIOStreamTransport(bao);
            TJSONProtocol prot = new TJSONProtocol(tios);
            obj.write(prot);
            tios.close();
            return dump(bao.toByteArray());
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends TBase> T deserialize(byte[] bs, Class<T> clazz) {
        T t = null;
        try {
            if (bs == null)
                return t;

            t = clazz.newInstance();
            ByteArrayInputStream bai = new ByteArrayInputStream(dump(bs));
            TIOStreamTransport tios = new TIOStreamTransport(bai);
            TJSONProtocol prot = new TJSONProtocol(tios);
            t.read(prot);
            tios.close();
            return t;
        } catch (Throwable e) {
            e.printStackTrace();
            return t;
        }
    }

    public static JSONObject toJson(TBase t) {
        try {
            if (t == null)
                return null;

            JSONObject o = new JSONObject();
            t.write(o);
            return o;
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toString(TBase t) {
        try {
            if (t == null)
                return "null";

            JSONObject o = new JSONObject();
            t.write(o);
            return o.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return t.toString();
        }
    }

    public static String toString(Collection<? extends TBase> ts) {
        try {
            if (ts == null)
                return "null";

            JSONArray a = new JSONArray();
            for (TBase t : ts)
                a.put(t != null ? toJson(t) : null);
            return a.toString();
        } catch (Throwable e) {
            e.printStackTrace();
            return ts.toString();
        }
    }
}
