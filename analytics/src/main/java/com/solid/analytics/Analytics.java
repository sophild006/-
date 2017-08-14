package com.solid.analytics;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.View;

import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.solid.analytics.model.Config;
import com.solid.analytics.model.Event;
import com.solid.analytics.model.PageEvent;
import com.solid.analytics.model.Property;
import com.solid.analytics.model.PropertyList;
import com.solid.analytics.thrift.ThriftUtil;
import com.solid.analytics.util.AndroidUtil;
import com.solid.analytics.util.Hex;
import com.solid.analytics.util.StringUtil;
import com.solid.analytics.util.TimeUtil;
import com.solid.analytics.util.log.Logger;
import com.solid.analytics.util.log.LoggerFactory;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.stat.StatAppMonitor;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.tencent.stat.common.StatConstants;
import com.umeng.analytics.MobclickAgent;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

public final class Analytics {

    static final Logger log = LoggerFactory.getLogger("Analytics");

    public static final String CATEGORY = "category";
    public static final String LABEL = "label";
    public static final String VALUE = "value";

    private static volatile Analytics sInstance;

    private final Context mContext;

    private final int mVersionCode;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private boolean mDebugMode = false;

    private Configuration mConfiguration;

    private Object mFirebaseAnalytics;

    private Object mGoogleAnalytics;

    static {
        try {
            ThriftUtil.init(Hex.fromHex(BuildConfig.THRIFT_KEY), Hex.fromHex(BuildConfig.THRIFT_IV));
        } catch (Exception e) {
            log.warn("Analytics", e);
        }
    }

    private Analytics(Context context) {
        mContext = context.getApplicationContext();
        mVersionCode = AndroidUtil.getVersionCode(mContext);
    }

    public static Analytics shared(Context context) {
        if (sInstance != null)
            return sInstance;

        synchronized (Analytics.class) {
            if (sInstance != null)
                return sInstance;

            sInstance = new Analytics(context);
            AnalyticsUtil.init(context);
            return sInstance;
        }
    }

    public Analytics init(Configuration cf) {
        long start = System.currentTimeMillis();
        try {
            mConfiguration = cf;

            String deviceId = Analytics.getDeviceId(mContext);

            if (isBuglyEnabled()) {
                long begin = System.currentTimeMillis();
                CrashReport.initCrashReport(mContext, cf.buglyAppId, cf.buglyDebugMode);
                CrashReport.setUserId(deviceId);
                if (log.isDebugEnabled())
                    log.debug("CrashReport init used:" + (System.currentTimeMillis() - begin) + "ms");
            }

            {
                long begin = System.currentTimeMillis();
                Config config = new Config();
                config.setAnalyticsUrl(cf.analyticsUrl);
                config.setChannel(cf.channel);
                config.setUploadAppsInfo(cf.uploadAppsInfo);

                AnalyticsService.startInit(mContext, config);
                if (log.isDebugEnabled())
                    log.debug("Analytics init used:" + (System.currentTimeMillis() - begin) + "ms");
            }

            // init mta
            if (isMtaEnabled()) {
                long begin = System.currentTimeMillis();
                try {
                    if (mConfiguration.mtaConcurrentProcessEnable)
                        StatConfig.setEnableConcurrentProcess(mConfiguration.mtaConcurrentProcessEnable);
                    StatConfig.setAppKey(mContext, mConfiguration.mtaAppKey);
                    StatConfig.setInstallChannel(mContext, mConfiguration.channel);
                    StatConfig.setAutoExceptionCaught(mConfiguration.mtaAutoExceptionCaught);
                    StatConfig.setDebugEnable(mDebugMode);
                    StatService.setContext(mContext);
                    StatService.startStatService(mContext, mConfiguration.mtaAppKey, StatConstants.VERSION);
                    StatService.registerActivityLifecycleCallbacks((Application) mContext.getApplicationContext());
                } catch (Exception e) {
                    log.warn("init mta:", e);
                } finally {
                    if (log.isDebugEnabled())
                        log.debug("mta init used:" + (System.currentTimeMillis() - begin) + "ms");
                }
            }

            if (isFirebaseEnabled()) {
                mFirebaseAnalytics = FirebaseAnalytics.getInstance(mContext);
            }

            if (isGoogleAnalyticsEnabled()) {
                GoogleAnalytics analytics = GoogleAnalytics.getInstance(mContext);
                mGoogleAnalytics = analytics.newTracker(mConfiguration.googleAnalyticsTrackingId);
            }

            if (isUmengEnabled()) {
                MobclickAgent.startWithConfigure(new MobclickAgent.UMAnalyticsConfig(mContext, mConfiguration.umengAppKey, mConfiguration.channel));
                ((Application) mContext).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                    @Override
                    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    }

                    @Override
                    public void onActivityStarted(Activity activity) {
                    }

                    @Override
                    public void onActivityResumed(Activity activity) {
                        MobclickAgent.onResume(activity);
                    }

                    @Override
                    public void onActivityPaused(Activity activity) {
                        MobclickAgent.onPause(activity);
                    }

                    @Override
                    public void onActivityStopped(Activity activity) {
                    }

                    @Override
                    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                    }

                    @Override
                    public void onActivityDestroyed(Activity activity) {
                    }
                });
            }

            if (isAppsFlyerEnabled()) {
                long begin = System.currentTimeMillis();
                AppsFlyerLib.getInstance().setAppUserId(deviceId);
                AppsFlyerLib.getInstance().setCollectAndroidID(true);
                AppsFlyerLib.getInstance().setCollectIMEI(true);
                AppsFlyerLib.getInstance().setCollectFingerPrint(true);
                AppsFlyerLib.getInstance().startTracking((Application) mContext.getApplicationContext(), cf.appsFlyerKey);
                if (log.isDebugEnabled())
                    log.debug("AppsFlyerLib init used:" + (System.currentTimeMillis() - begin) + "ms");
            }

            return this;
        } finally {
            log.debug("init" + " used:" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    public boolean isMtaEnabled() {
        return mConfiguration != null && !StringUtil.isEmpty(mConfiguration.mtaAppKey);
    }

    boolean isFirebaseEnabled() {
        return mConfiguration != null && mConfiguration.firebaseEnable;
    }

    boolean isFirebaseAvailable() {
        return isFirebaseEnabled() && mFirebaseAnalytics != null;
    }

    FirebaseAnalytics firebase() {
        return (FirebaseAnalytics) mFirebaseAnalytics;
    }

    boolean isGoogleAnalyticsEnabled() {
        return mConfiguration != null && !StringUtil.isEmpty(mConfiguration.googleAnalyticsTrackingId);
    }

    boolean isGoogleAnalyticsAvailable() {
        return isGoogleAnalyticsEnabled() && mGoogleAnalytics != null;
    }

    Tracker google() {
        return (Tracker) mGoogleAnalytics;
    }

    boolean isUmengEnabled() {
        return mConfiguration != null && !StringUtil.isEmpty(mConfiguration.umengAppKey);
    }

    public boolean isBuglyEnabled() {
        return mConfiguration != null && !StringUtil.isEmpty(mConfiguration.buglyAppId);
    }

    public boolean isAppsFlyerEnabled() {
        return mConfiguration != null && !StringUtil.isEmpty(mConfiguration.appsFlyerKey);
    }

    public Analytics setDebugMode(boolean debugMode) {
        mDebugMode = debugMode;

        LoggerFactory.setTraceEnabled(debugMode);
        LoggerFactory.setDebugEnabled(debugMode);

        if (isMtaEnabled())
            StatConfig.setDebugEnable(debugMode);

        if (isUmengEnabled())
            MobclickAgent.setDebugMode(debugMode);

        log.debug("setDebugMode:" + debugMode);
        return this;
    }

    final Runnable mDebugModeSync = new Runnable() {
        @Override
        public void run() {
            AnalyticsService.startDebugSync(mContext);
        }
    };

    private void checkDebugModeSync() {
        if (!mDebugMode)
            return;

        mHandler.removeCallbacks(mDebugModeSync);
        mHandler.postDelayed(mDebugModeSync, TimeUtil.SECOND * 2L);
    }

    boolean isCategoryValid(String act) {
        if (mConfiguration == null)
            return true;

        if (mConfiguration.categoryCanBeEmpty)
            return true;

        return !StringUtil.isEmpty(act);
    }

    boolean isActionValid(String act) {
        if (mConfiguration == null)
            return true;

        if (mConfiguration.actionCanBeEmpty)
            return true;

        return !StringUtil.isEmpty(act);
    }

    public void sendEvent(String cat, String act, Map<String, Object> params) {
        if (!isCategoryValid(cat)) {
            log.warn("sendEvent: cat can not be empty!");
            throw new IllegalArgumentException("sendEvent: cat can not be empty!");
        }
        if (!isActionValid(act)) {
            log.warn("sendEvent: act can not be empty!");
            throw new IllegalArgumentException("sendEvent: act can not be empty!");
        }

        if (log.isDebugEnabled())
            log.debug("sendEvent" + " cat:" + cat + " act:" + act + " params:" + params);

        String label = labelOf(params);
        Object originValue = params != null ? params.get(VALUE) : null;
        Long value = valueOf(params);

        // analytics
        try {
            if (checkSendEventInterceptor(mConfiguration.analyticInterceptor, cat, act, params)) {
                Map<String, Object> ps = params != null ? new HashMap<String, Object>(params) : null;
                if (ps != null) {
                    ps.remove(LABEL);
                    ps.remove(VALUE);
                }

                Event event = new Event();
                event.setCategory(cat);
                event.setEvent_code(act);
                event.setLabel(label);
                event.setValue(originValue != null ? StringUtil.toString(originValue) : null);
                event.setExtra(toJson(ps));

                event.setCreated(TimeUtil.dateTimeNow());
                event.setEvent_occurred_ver(mVersionCode);

                AnalyticsService.startSendEvent(mContext, event);
            }
            checkDebugModeSync();
        } catch (Exception e) {
            log.warn("analytics.sendEvent", e);
        }

        // mta
        try {
            if (isMtaEnabled() && checkSendEventInterceptor(mConfiguration.mtaInterceptor, cat, act, params)) {
                Properties props = toProperties(params);
                if (cat != null) props.put(CATEGORY, cat);
                StatService.trackCustomKVEvent(mContext, act, props);
            }
        } catch (Exception e) {
            log.warn("Mta.sendEvent", e);
        }

        // firebase
        // check firebase act length
        if (isFirebaseAvailable() && (act == null || act.length() <= 0 || act.length() > 40))
            throw new IllegalArgumentException("action length must be 1-40 for firebase!");
        try {
            if (isFirebaseAvailable() && checkSendEventInterceptor(mConfiguration.firebaseInterceptor, cat, act, params)) {
                Bundle bundle = toBundle(params);
                if (cat != null) bundle.putString(CATEGORY, cat);
                if (value != null) bundle.putLong(FirebaseAnalytics.Param.VALUE, value);
                firebase().logEvent(act, bundle);
            }
        } catch (Exception e) {
            log.warn("firebase.logEvent", e);
        }

        // google
        try {
            if (isGoogleAnalyticsAvailable() && checkSendEventInterceptor(mConfiguration.googleAnalyticsInterceptor, cat, act, params)) {
                Map<String, String> ps = toStringMap(params);
                HitBuilders.EventBuilder builder = new HitBuilders.EventBuilder()
                        .setCategory(cat != null ? cat : "app")
                        .setAction(act);
                if (label != null) builder.setLabel(label);
                if (value != null) builder.setValue(value);
                ps.putAll(builder.build());
                google().send(ps);
            }
        } catch (Exception e) {
            log.warn("google.send", e);
        }

        // umeng
        try {
            if (isUmengEnabled() && checkSendEventInterceptor(mConfiguration.umengInterceptor, cat, act, params)) {
                Map<String, String> ps = toStringMap(params);
                if (cat != null) params.put(CATEGORY, cat);
                if (value != null) {
                    MobclickAgent.onEventValue(mContext, act, ps, value.intValue());
                } else {
                    if (ps == null || ps.size() <= 0) {
                        MobclickAgent.onEvent(mContext, act);
                    } else {
                        MobclickAgent.onEvent(mContext, act, ps);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("umeng.onEvent", e);
        }
    }

    private final WeakHashMap<Object, PageEvent> mPageObjects = new WeakHashMap<Object, PageEvent>();

    public void onPageBegin(Object page, String ext) {
        long current = System.currentTimeMillis();
        final String pageName = page.getClass().getName();

        synchronized (mPageObjects) {
            mPageObjects.put(page, new PageEvent(pageName, ext, current, current));
        }

        if (log.isDebugEnabled())
            log.debug("onPageBegin" + " pageName:" + pageName);

        if (isMtaEnabled() && checkOnPageBeginInterceptor(mConfiguration.mtaInterceptor, page, ext))
            StatService.trackBeginPage(mContext, pageName);
    }

    public void onPageBegin(Object page) {
        onPageBegin(page, null);
    }

    public void onPageEnd(Object page) {
        long current = System.currentTimeMillis();
        final String pageName = page.getClass().getName();

        PageEvent pageEvent;
        synchronized (mPageObjects) {
            pageEvent = mPageObjects.remove(page);
        }

        if (pageEvent == null) {
            log.warn("onPageEnd not exist:" + page.toString());
            return;
        }

        pageEvent.setEt(current);

        AnalyticsService.startSendPageEvent(mContext, pageEvent);

        checkDebugModeSync();

        if (log.isDebugEnabled())
            log.debug("onPageEnd" + " pageName:" + pageName);

        if (isMtaEnabled() && checkOnPageEndInterceptor(mConfiguration.mtaInterceptor, page))
            StatService.trackEndPage(mContext, pageName);
    }

    public void onActivityStart(Activity activity, String ext) {
        onPageBegin(activity, ext);
    }

    public void onActivityStart(Activity activity) {
        onActivityStart(activity, null);
    }

    public void onActivityStop(Activity activity) {
        onPageEnd(activity);
    }

    public void onViewBegin(View view, String ext) {
        onPageBegin(view, ext);
    }

    public void onViewBegin(View view) {
        onViewBegin(view, null);
    }

    public void onViewEnd(View view) {
        onPageEnd(view);
    }

    public void onFragmentStart(Fragment fragment, String ext) {
        onPageBegin(fragment, ext);
    }

    public void onFragmentStart(Fragment fragment) {
        onFragmentStart(fragment, null);
    }

    public void onFragmentStop(Fragment fragment) {
        onPageEnd(fragment);
    }

    public void onFragmentStart(android.app.Fragment fragment, String ext) {
        onPageBegin(fragment, ext);
    }

    public void onFragmentStart(android.app.Fragment fragment) {
        onFragmentStart(fragment, null);
    }

    public void onFragmentStop(android.app.Fragment fragment) {
        onPageEnd(fragment);
    }

    private final HashMap<String, PageEvent> mPageNames = new HashMap<String, PageEvent>();

    public void onPageBegin(String pageName, String ext) {
        long current = System.currentTimeMillis();

        synchronized (mPageNames) {
            mPageNames.put(pageName, new PageEvent(pageName, ext, current, current));
        }

        if (log.isDebugEnabled())
            log.debug("onPageBegin" + " pageName:" + pageName);

        if (isMtaEnabled())
            StatService.trackBeginPage(mContext, pageName);
    }

    public void onPageBegin(String pageName) {
        onPageBegin(pageName, null);
    }

    public void onPageEnd(String pageName) {
        long current = System.currentTimeMillis();

        PageEvent pageEvent;
        synchronized (mPageNames) {
            pageEvent = mPageNames.remove(pageName);
        }

        if (pageEvent == null) {
            log.warn("onPageEnd not exist:" + pageName);
            return;
        }

        pageEvent.setEt(current);

        AnalyticsService.startSendPageEvent(mContext, pageEvent);

        checkDebugModeSync();

        if (log.isDebugEnabled())
            log.debug("onPageEnd" + " pageName:" + pageName);

        if (isMtaEnabled())
            StatService.trackEndPage(mContext, pageName);
    }

    public Object onInterfaceBegin(String name) {
        if (!isMtaEnabled() || StringUtil.isEmpty(name))
            return null;

        if (!checkOnInterfaceBeginInterceptor(mConfiguration.mtaInterceptor, name))
            return null;

        if (log.isDebugEnabled())
            log.debug("onInterfaceBegin:" + name);

        try {
            final StatAppMonitor m = new StatAppMonitor(name);
            m.setMillisecondsConsume(System.currentTimeMillis());
            m.setSampling(1);
            return m;
        } catch (Throwable e) {
            log.warn("onInterfaceBegin", e);
            return null;
        }
    }

    public void onInterfaceEnd(Object obj, long reqSize, long respSize, int returnCode) {
        if (!isMtaEnabled() || !(obj instanceof StatAppMonitor))
            return;

        final StatAppMonitor m = (StatAppMonitor) obj;

        if (log.isDebugEnabled())
            log.debug("onInterfaceEnd:" + m.getInterfaceName());

        try {
            long start = m.getMillisecondsConsume();
            m.setMillisecondsConsume(System.currentTimeMillis() - start);
            m.setReqSize(reqSize);
            m.setRespSize(respSize);
            m.setReturnCode(returnCode);
            m.setResultType(StatAppMonitor.SUCCESS_RESULT_TYPE);

            StatService.reportAppMonitorStat(mContext, m);
        } catch (Throwable e) {
            log.warn("onInterfaceEnd", e);
        }
    }

    public void onInterfaceEnd(Object obj) {
        onInterfaceEnd(obj, 0L, 0L, 0);
    }

    public void onInterfaceFail(Object obj, long reqSize, int returnCode) {
        if (!isMtaEnabled() || !(obj instanceof StatAppMonitor))
            return;

        final StatAppMonitor m = (StatAppMonitor) obj;

        if (log.isDebugEnabled())
            log.debug("onInterfaceFail:" + m.getInterfaceName());

        try {
            long start = m.getMillisecondsConsume();
            m.setMillisecondsConsume(System.currentTimeMillis() - start);
            m.setReqSize(reqSize);
            m.setReturnCode(returnCode);
            m.setResultType(StatAppMonitor.FAILURE_RESULT_TYPE);

            StatService.reportAppMonitorStat(mContext, m);
        } catch (Throwable e) {
            log.warn("onInterfaceFail", e);
        }
    }

    public void onInterfaceFail(Object obj) {
        onInterfaceFail(obj, 0L, -1);
    }

    public void setProperty(String name, String value) {
        if (StringUtil.isEmpty(name))
            throw new IllegalArgumentException("name can not be empty!");

        PropertyList propertyList = new PropertyList();
        propertyList.addToProperties(new Property(name, value));
        AnalyticsService.startSetPropertyList(mContext, propertyList);

        checkDebugModeSync();

        if (isMtaEnabled()) {
            Map<String, String> attrs = new HashMap<String, String>();
            attrs.put(name, value);
            StatService.setEnvAttributes(mContext, attrs);
        }

        if (isFirebaseAvailable()) {
            firebase().setUserProperty(name, value);
        }
    }

    public void setProperties(Map<String, String> properties) {
        if (properties == null || properties.size() <= 0)
            return;

        PropertyList propertyList = new PropertyList();
        for (String name : properties.keySet()) {
            if (StringUtil.isEmpty(name))
                throw new IllegalArgumentException("property name can not be empty!");
            propertyList.addToProperties(new Property(name, properties.get(name)));
        }
        AnalyticsService.startSetPropertyList(mContext, propertyList);

        checkDebugModeSync();

        if (isMtaEnabled()) {
            StatService.setEnvAttributes(mContext, properties);
        }

        if (isFirebaseAvailable()) {
            for (String name : properties.keySet())
                firebase().setUserProperty(name, properties.get(name));
        }
    }

    public static String getDeviceId(Context context) {
        return AndroidUtil.getDeviceIdImeiAndroidId(context);
    }

    public static int getBucketId(Context context) {
        return AndroidUtil.getBucketId(context);
    }

    public interface Interceptor {
        boolean sendEvent(String cat, String act, Map<String, Object> params);

        boolean onPageBegin(Object page, String ext);

        boolean onPageEnd(Object page);

        boolean onInterfaceBegin(String name);
    }

    public static class InterceptorBase implements Interceptor {
        @Override
        public boolean sendEvent(String cat, String act, Map<String, Object> params) {
            return true;
        }

        @Override
        public boolean onPageBegin(Object page, String ext) {
            return true;
        }

        public boolean onPageEnd(Object page) {
            return true;
        }

        public boolean onInterfaceBegin(String name) {
            return true;
        }
    }

    public static class Configuration {
        final public String analyticsUrl;
        final public String channel;
        final public Interceptor analyticInterceptor;
        final public String mtaAppKey;
        final public boolean mtaAutoExceptionCaught;
        final public boolean mtaConcurrentProcessEnable;
        final public Interceptor mtaInterceptor;
        final public boolean firebaseEnable;
        final public Interceptor firebaseInterceptor;
        final public String googleAnalyticsTrackingId;
        final public Interceptor googleAnalyticsInterceptor;
        final public String umengAppKey;
        final public boolean umengAutoTrackActivity;
        final public Interceptor umengInterceptor;
        final public String buglyAppId;
        final public boolean buglyDebugMode;
        final public String appsFlyerKey;
        final public boolean uploadAppsInfo;
        final public boolean categoryCanBeEmpty;
        final public boolean actionCanBeEmpty;

        public Configuration(String analyticsUrl,
                             String channel,
                             Interceptor analyticInterceptor,
                             String mtaAppKey,
                             boolean mtaAutoExceptionCaught,
                             boolean mtaConcurrentProcessEnable,
                             Interceptor mtaInterceptor,
                             boolean firebaseEnable,
                             Interceptor firebaseInterceptor,
                             String googleAnalyticsTrackingId,
                             Interceptor googleAnalyticsInterceptor,
                             String umengAppKey,
                             boolean umengAutoTrackActivity,
                             Interceptor umengInterceptor,
                             String buglyAppId,
                             boolean buglyDebugMode,
                             String appsFlyerKey,
                             boolean uploadAppsInfo,
                             boolean categoryCanBeEmpty,
                             boolean actionCanBeEmpty) {
            this.analyticsUrl = StringUtil.trimEndSplash(analyticsUrl);
            this.channel = channel;
            this.analyticInterceptor = analyticInterceptor;
            this.mtaAppKey = mtaAppKey;
            this.mtaAutoExceptionCaught = mtaAutoExceptionCaught;
            this.mtaConcurrentProcessEnable = mtaConcurrentProcessEnable;
            this.mtaInterceptor = mtaInterceptor;
            this.firebaseEnable = firebaseEnable;
            this.firebaseInterceptor = firebaseInterceptor;
            this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
            this.googleAnalyticsInterceptor = googleAnalyticsInterceptor;
            this.umengAppKey = umengAppKey;
            this.umengAutoTrackActivity = umengAutoTrackActivity;
            this.umengInterceptor = umengInterceptor;
            this.buglyAppId = buglyAppId;
            this.buglyDebugMode = buglyDebugMode;
            this.appsFlyerKey = appsFlyerKey;
            this.uploadAppsInfo = uploadAppsInfo;
            this.categoryCanBeEmpty = categoryCanBeEmpty;
            this.actionCanBeEmpty = actionCanBeEmpty;
        }

        public static class Builder {
            private String analyticsUrl;
            private String channel;
            private Interceptor analyticInterceptor;
            private String mtaAppKey;
            private boolean mtaAutoExceptionCaught = true;
            private boolean mtaConcurrentProcessEnable;
            private Interceptor mtaInterceptor;
            private boolean firebaseEnable = true;
            private Interceptor firebaseInterceptor;
            private String googleAnalyticsTrackingId;
            private Interceptor googleAnalyticsInterceptor;
            private String umengAppKey;
            private boolean umengAutoTrackActivity = true;
            private Interceptor umengInterceptor;
            private String buglyAppId;
            private boolean buglyDebugMode;
            private String appsFlyerKey;
            private boolean uploadAppsInfo;
            private boolean categoryCanBeEmpty = true;
            private boolean actionCanBeEmpty;

            public Configuration build() {
                return new Configuration(
                        analyticsUrl,
                        channel,
                        analyticInterceptor,
                        mtaAppKey,
                        mtaAutoExceptionCaught,
                        mtaConcurrentProcessEnable,
                        mtaInterceptor,
                        firebaseEnable,
                        firebaseInterceptor,
                        googleAnalyticsTrackingId,
                        googleAnalyticsInterceptor,
                        umengAppKey,
                        umengAutoTrackActivity,
                        umengInterceptor,
                        buglyAppId,
                        buglyDebugMode,
                        appsFlyerKey,
                        uploadAppsInfo,
                        categoryCanBeEmpty,
                        actionCanBeEmpty);
            }

            public Builder setAnalyticsUrl(String url) {
                this.analyticsUrl = url;
                return this;
            }

            public Builder setChannel(String channel) {
                this.channel = channel;
                return this;
            }

            public Builder setAnalyticInterceptor(Interceptor analyticInterceptor) {
                this.analyticInterceptor = analyticInterceptor;
                return this;
            }

            public Builder setMtaAppKey(String mtaAppKey) {
                this.mtaAppKey = mtaAppKey;
                return this;
            }

            public Builder setMtaAutoExceptionCaught(boolean mtaAutoExceptionCaught) {
                this.mtaAutoExceptionCaught = mtaAutoExceptionCaught;
                return this;
            }

            public Builder setMtaConcurrentProcessEnable(boolean mtaConcurrentProcessEnable) {
                this.mtaConcurrentProcessEnable = mtaConcurrentProcessEnable;
                return this;
            }

            public Builder setMtaInterceptor(Interceptor mtaInterceptor) {
                this.mtaInterceptor = mtaInterceptor;
                return this;
            }

            public Builder setFirebaseEnable(boolean firebaseEnable) {
                this.firebaseEnable = firebaseEnable;
                return this;
            }

            public Builder setFirebaseInterceptor(Interceptor firebaseInterceptor) {
                this.firebaseInterceptor = firebaseInterceptor;
                return this;
            }

            public Builder setGoogleAnalyticsTrackingId(String googleAnalyticsTrackingId) {
                this.googleAnalyticsTrackingId = googleAnalyticsTrackingId;
                return this;
            }

            public Builder setGoogleAnalyticsInterceptor(Interceptor googleAnalyticsInterceptor) {
                this.googleAnalyticsInterceptor = googleAnalyticsInterceptor;
                return this;
            }

            public Builder setUmengAppKey(String umengAppKey) {
                this.umengAppKey = umengAppKey;
                return this;
            }

            public Builder setUmengAutoTrackActivity(boolean umengAutoTrackActivity) {
                this.umengAutoTrackActivity = umengAutoTrackActivity;
                return this;
            }

            public Builder setUmengInterceptor(Interceptor umengInterceptor) {
                this.umengInterceptor = umengInterceptor;
                return this;
            }

            public Builder setBuglyAppId(String buglyAppId) {
                this.buglyAppId = buglyAppId;
                return this;
            }

            public Builder setBuglyDebugMode(boolean buglyDebugMode) {
                this.buglyDebugMode = buglyDebugMode;
                return this;
            }

            public Builder setAppsFlyerKey(String appsFlyerKey) {
                this.appsFlyerKey = appsFlyerKey;
                return this;
            }

            public Builder setUploadAppsInfo(boolean uploadAppsInfo) {
                this.uploadAppsInfo = uploadAppsInfo;
                return this;
            }

            public Builder setCategoryCanBeEmpty(boolean categoryCanBeEmpty) {
                this.categoryCanBeEmpty = categoryCanBeEmpty;
                return this;
            }

            public Builder setActionCanBeEmpty(boolean actionCanBeEmpty) {
                this.actionCanBeEmpty = actionCanBeEmpty;
                return this;
            }
        }
    }

    static String labelOf(Map<String, Object> params) {
        Object o = params != null ? params.get(LABEL) : null;
        return o != null ? o.toString() : null;
    }

    static Long valueOf(Map<String, Object> params) {
        Object o = params != null ? params.get(VALUE) : null;
        if (o instanceof Number)
            return ((Number) o).longValue();

        if (o instanceof String) {
            try {
                return Long.parseLong((String) o);
            } catch (Exception e) {
                return null;
            }
        }

        return null;
    }

    static Bundle toBundle(Map<String, Object> params) {
        Bundle bundle = new Bundle();
        if (params == null)
            return bundle;

        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof Boolean)
                bundle.putBoolean(key, (Boolean) value);
            if (value instanceof Byte)
                bundle.putByte(key, (Byte) value);
            if (value instanceof Short)
                bundle.putShort(key, (Short) value);
            if (value instanceof Integer)
                bundle.putInt(key, (Integer) value);
            if (value instanceof Long)
                bundle.putLong(key, (Long) value);
            if (value instanceof Float)
                bundle.putFloat(key, (Float) value);
            if (value instanceof Double)
                bundle.putDouble(key, (Double) value);
            if (value instanceof String)
                bundle.putString(key, (String) value);

            if (value instanceof boolean[])
                bundle.putBooleanArray(key, (boolean[]) value);
            if (value instanceof byte[])
                bundle.putByteArray(key, (byte[]) value);
            if (value instanceof short[])
                bundle.putShortArray(key, (short[]) value);
            if (value instanceof int[])
                bundle.putIntArray(key, (int[]) value);
            if (value instanceof long[])
                bundle.putLongArray(key, (long[]) value);
            if (value instanceof float[])
                bundle.putFloatArray(key, (float[]) value);
            if (value instanceof double[])
                bundle.putDoubleArray(key, (double[]) value);
            if (value instanceof String[])
                bundle.putStringArray(key, (String[]) value);
            if (value instanceof ArrayList)
                bundle.putStringArrayList(key, (ArrayList<String>) value);
        }

        return bundle;
    }

    static Properties toProperties(Map<String, Object> params) {
        Properties properties = new Properties();
        if (params == null)
            return properties;

        for (String key : params.keySet())
            properties.put(key, params.get(key));

        return properties;
    }

    static Map<String, String> toStringMap(Map<String, Object> params) {
        Map<String, String> results = new HashMap<String, String>();
        if (params == null)
            return results;

        for (String key : params.keySet())
            results.put(key, StringUtil.toString(params.get(key)));

        return results;
    }

    static String toJson(Map<String, Object> params) {
        if (params == null || params.size() <= 0)
            return null;

        JSONObject results = new JSONObject();
        for (String key : params.keySet()) {
            try {
                results.put(key, params.get(key));
            } catch (Exception e) {
            }
        }

        return results.toString();
    }

    private static boolean checkSendEventInterceptor(Interceptor interceptor, String category, String action, Map<String, Object> params) {
        return interceptor == null || interceptor.sendEvent(category, action, params);
    }

    private static boolean checkOnPageBeginInterceptor(Interceptor interceptor, Object page, String extra) {
        return interceptor == null || interceptor.onPageBegin(page, extra);
    }

    private static boolean checkOnPageEndInterceptor(Interceptor interceptor, Object page) {
        return interceptor == null || interceptor.onPageEnd(page);
    }

    private static boolean checkOnInterfaceBeginInterceptor(Interceptor interceptor, String name) {
        return interceptor == null || interceptor.onInterfaceBegin(name);
    }
}
