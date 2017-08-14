package com.demo;

import android.os.Build;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.solid.analytics.Analytics;
import com.solid.analytics.util.AndroidUtil;

public class MainApp extends MultiDexApplication {

    static final String TAG = "MainApp";

    @Override
    public void onCreate() {
        super.onCreate();

        try {
            Analytics.shared(this)
                    .init(new Analytics.Configuration.Builder()
                            .setAnalyticsUrl("http://test.tracking.uhen2.com") // 统计url
                            .setChannel("gp") // 当前安装的包渠道
                            .setFirebaseEnable(true)
                            .setMtaAppKey(null) // mta app key
                            .setMtaAutoExceptionCaught(true) // mta 捕获异常
                            .setMtaConcurrentProcessEnable(true) // mta 支持多进程上报
                            .setBuglyAppId(null) // buglyAppId
                            .setBuglyDebugMode(true) // bugly debug mode
                            .setAppsFlyerKey(null) // appsflyer key
                            .setGoogleAnalyticsTrackingId("UA-100039574-1")
                            .setUploadAppsInfo(false) // 上传应用列表
                            .setCategoryCanBeEmpty(true) // category 是否允许为空
                            .setActionCanBeEmpty(true) // action 是否允许为空
                            .build());

            Analytics.shared(this).setDebugMode(true);

            Log.d(TAG, "androidId:" + AndroidUtil.getAndroidId(this) + " serial:" + Build.SERIAL + " bucketId:" + AndroidUtil.getBucketId(this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}
