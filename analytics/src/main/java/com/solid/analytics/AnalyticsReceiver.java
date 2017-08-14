package com.solid.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.solid.analytics.util.log.Logger;
import com.solid.analytics.util.log.LoggerFactory;

public class AnalyticsReceiver extends BroadcastReceiver {

    static final Logger log = LoggerFactory.getLogger(AnalyticsReceiver.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        log.debug("onReceive" + " intent:" + intent);

        long start = System.currentTimeMillis();
        if (intent == null)
            return;

        final String action = intent.getAction();

        try {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
                    AnalyticsService.startConnectivitySync(context);
                }
                return;
            }
        } catch (Exception e) {
            log.warn("onReceive", e);
        } finally {
            log.debug("onReceive" + " used:" + (System.currentTimeMillis() - start) + "ms");
        }
    }
}
