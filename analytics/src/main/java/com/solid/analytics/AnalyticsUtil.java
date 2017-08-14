package com.solid.analytics;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.View;

import com.solid.analytics.util.log.Logger;
import com.solid.analytics.util.log.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsUtil {

    static final Logger log = LoggerFactory.getLogger("AnalyticsUtil");

    protected static Context sContext;

    public static void init(Context context) {
        sContext = context;
    }

    public static void sendEvent(String category, String action, String label, Long value, Map<String, Object> params) {
        params = params != null ? params : new HashMap<String, Object>();
        if (label != null) params.put(Analytics.LABEL, label);
        if (value != null) params.put(Analytics.VALUE, value);

        Analytics.shared(sContext).sendEvent(category, action, params);
    }

    public static void sendEvent(String category, String action, String label, long value) {
        sendEvent(category, action, label, value, null);
    }

    public static void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, null, null);
    }

    public static void sendEvent(String category, String action) {
        sendEvent(category, action, null, null, null);
    }

    public static void sendEventSimple(String action, String label, Long value, Map<String, Object> params) {
        sendEvent(null, action, label, value, params);
    }

    public static void sendEventSimple(String action, String label, long value) {
        sendEvent(null, action, label, value, null);
    }

    public static void sendEventSimple(String action, String label) {
        sendEvent(null, action, label, null, null);
    }

    public static void sendEventSimple(String action) {
        sendEvent(null, action, null, null, null);
    }

    public static void setProperty(String name, String value) {
        Analytics.shared(sContext).setProperty(name, value);
    }

    public static void onPageBegin(String pageName) {
        try {
            if (pageName == null)
                return;

            Analytics.shared(sContext).onPageBegin(pageName);
        } catch (Exception e) {
            log.warn("onPageBegin", e);
        }
    }

    public static void onPageEnd(String pageName) {
        try {
            if (pageName == null)
                return;

            Analytics.shared(sContext).onPageEnd(pageName);
        } catch (Exception e) {
            log.warn("onPageEnd", e);
        }
    }

    public static void onPageBegin(View view) {
        try {
            if (view == null)
                return;

            Analytics.shared(sContext).onPageBegin(view.getClass().getName());
        } catch (Exception e) {
            log.warn("onPageBegin", e);
        }
    }

    public static void onPageEnd(View view) {
        try {
            if (view == null)
                return;

            Analytics.shared(sContext).onPageEnd(view.getClass().getName());
        } catch (Exception e) {
            log.warn("onPageEnd", e);
        }
    }

    public static void onPageBegin(Fragment frag) {
        try {
            if (frag == null)
                return;

            Analytics.shared(sContext).onPageBegin(frag.getClass().getName());
        } catch (Exception e) {
            log.warn("onPageBegin", e);
        }
    }

    public static void onPageEnd(Fragment frag) {
        try {
            if (frag == null)
                return;

            Analytics.shared(sContext).onPageEnd(frag.getClass().getName());
        } catch (Exception e) {
            log.warn("onPageEnd", e);
        }
    }

    public static void onPageBegin(android.app.Fragment frag) {
        try {
            if (frag == null)
                return;

            Analytics.shared(sContext).onPageBegin(frag.getClass().getName());
        } catch (Exception e) {
            log.warn("onPageBegin", e);
        }
    }

    public static void onPageEnd(android.app.Fragment frag) {
        try {
            if (frag == null)
                return;

            Analytics.shared(sContext).onPageEnd(frag.getClass().getName());
        } catch (Exception e) {
            log.warn("onPageEnd", e);
        }
    }
}
