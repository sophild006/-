package com.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.demo.analytics.R;
import com.solid.analytics.Analytics;
import com.solid.analytics.AnalyticsUtil;
import com.solid.analytics.util.ModelUtil;

import java.net.URLDecoder;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AnalyticsUtil.sendEvent("cat", "act", "label");

        AnalyticsUtil.sendEvent("act", "main_on_create", "label", 1L);

        AnalyticsUtil.sendEventSimple("test");

        AnalyticsUtil.setProperty("name", "value");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!ModelUtil.checkPermissionPackageUsageStats(this))
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        }

        try {
            Intent referIntent = new Intent("com.android.vending.INSTALL_REFERRER");
            String referrer = "utm_source%3DZoomy%26utm_campaign%3Dgroup5%26utm_content%3D.02nOVwBAAB5EAAACQAAAKm8AQAAAAAAZyoJAc3MTD4AAAAAAcuv0AI*%26af_siteid%3D4217%26google_aid%3D";
            String decoded = URLDecoder.decode(referrer, "utf-8");
            // referIntent.putExtra("referrer", referrer);
            referIntent.putExtra("referrer", decoded);
            referIntent.setPackage(getPackageName());
            sendBroadcast(referIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Analytics.shared(this).onActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        Analytics.shared(this).onActivityStop(this);

        Analytics.shared(this).setProperty("name", null);
    }
}
