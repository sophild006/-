package com.solid.analytics.util;

import android.Manifest;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.os.Build;

import com.solid.analytics.model.ActivedRequest;
import com.solid.analytics.model.App;
import com.solid.analytics.model.AppUsage;
import com.solid.analytics.model.AppUsages;
import com.solid.analytics.model.Apps;
import com.solid.analytics.model.Config;
import com.solid.analytics.model.EventsRequest;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class ModelUtil {

    public static ActivedRequest createActivedRequest(Context context, String[] suPaths) throws Exception {
        final ActivedRequest active = new ActivedRequest();

        // active.setPub_id();
        active.setPkgname(context.getPackageName());
        // active.setRegion();
        // active.setCountry();
        active.setTimezone(TimeZone.getDefault().getID());
        active.setCarrier(AndroidUtil.getSimOperator(context));

        Address address = AndroidUtil.loadAddress(context);
        if (address != null) {
            active.setLatitude(address.getLatitude());
            active.setLongitude(address.getLongitude());
        }

        active.setFacebook(AndroidUtil.isAppInstalled(context, "com.facebook.katana") ? 1 : 0);
        active.setGpservice((AndroidUtil.isAppInstalled(context, "com.android.vending") || AndroidUtil.isAppInstalled(context, "com.google.market")) ? 1 : 0);

        active.setLanguage(Locale.getDefault().getLanguage());

        active.setModel(Build.MODEL);
        active.setOs("android");
        active.setOs_ver(Build.VERSION.RELEASE);

        active.setApp_ver(Integer.toString(AndroidUtil.getVersionCode(context)));
        active.setAndroid_id(AndroidUtil.getAndroidId(context));
        active.setGaid(AndroidUtil.getGoogleAdId(context)); // load default first
        // active.setIdfa();
        active.setImsi(AndroidUtil.getImsi(context));
        active.setImei(AndroidUtil.getImei(context));
        // active.setImei2();

        String mac = AndroidUtil.getMacAddress(context);
        active.setMac(mac);
        if (!StringUtil.isEmpty(mac)) active.setMacsha1(Sha1.sha1(mac));
        if (!StringUtil.isEmpty(mac)) active.setMacmd5(Md5.md5(mac));

        String referrer = AndroidUtil.getReferrer(context);
        String decodedReferrer = referrer;
        try {
            decodedReferrer = URLDecoder.decode(referrer, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Map<String, String> referrerParams = StringUtil.decodeUrlParameters(decodedReferrer);
        if (referrerParams != null) {
            active.setPub_id(referrerParams.get("af_siteid"));
            active.setUtm_source(referrerParams.get("utm_source"));
            active.setUtm_term(referrerParams.get("utm_term"));
            active.setUtm_medium(referrerParams.get("utm_medium"));
            active.setUtm_content(referrerParams.get("utm_content"));
            active.setUtm_campaign(referrerParams.get("utm_campaign"));
        }

        // active.setExt1();
        // active.setExt2();
        // active.setExt3();
        // active.setExt4();
        // active.setExt5();

        active.setInstall_date(getCurrentDate());

        active.setSerial(Build.SERIAL);
        active.setBid(AndroidUtil.getBucketId(context));

        active.setReferrer(referrer);

        return active;
    }

    public static EventsRequest createEventsRequest(Context context) throws Exception {
        final EventsRequest events = new EventsRequest();

        events.setPkgname(context.getPackageName());
        events.setCarrier(AndroidUtil.getSimOperator(context));

        Address address = AndroidUtil.loadAddress(context);
        if (address != null) {
            events.setLatitude(address.getLatitude());
            events.setLongitude(address.getLongitude());
        }

        events.setAndroid_id(AndroidUtil.getAndroidId(context));
        events.setGaid(AndroidUtil.getGoogleAdId(context));
        // events.setIdfa();
        events.setImsi(AndroidUtil.getImsi(context));
        events.setImei(AndroidUtil.getImei(context));
        events.setApp_ver(Integer.toString(AndroidUtil.getVersionCode(context)));

        events.setSerial(Build.SERIAL);
        events.setBid(AndroidUtil.getBucketId(context));

        return events;
    }

    static final SimpleDateFormat sDateFormat = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");

    public static String getCurrentDate() {
        Date now = new Date(System.currentTimeMillis());
        return sDateFormat.format(now);
    }

    private static Field getField(Class clz, String name) {
        try {
            Field field = clz.getField(name);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            return null;
        }
    }

    private static int getIntField(Field field, Object obj, int fallback) {
        try {
            if (field == null)
                return fallback;

            return field.getInt(obj);
        } catch (Exception e) {
            return fallback;
        }
    }

    public static boolean checkPermissionPackageUsageStats(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return false;

        PackageManager pm = context.getPackageManager();
        int ret = pm.checkPermission(Manifest.permission.PACKAGE_USAGE_STATS, context.getPackageName());
        if (ret == PackageManager.PERMISSION_GRANTED)
            return true;

        try {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            long current = System.currentTimeMillis();
            List<UsageStats> usages = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, current - TimeUtil.DAY * 1L, current);
            return usages != null && usages.size() > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static Apps loadAppsAndUsages(Context context, long lastSync, long current) {
        try {
            PackageManager pm = context.getPackageManager();
            List<PackageInfo> pis = pm.getInstalledPackages(0);
            Apps apps = new Apps();
            for (PackageInfo pi : pis)
                if (pi != null && pi.packageName != null)
                    apps.putToApps(pi.packageName, new App(pi.versionCode, pi.firstInstallTime, pi.lastUpdateTime, (pi.applicationInfo != null ? pi.applicationInfo.flags : 0)));

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && checkPermissionPackageUsageStats(context)) {
                    UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                    Map<String, UsageStats> usages = usm.queryAndAggregateUsageStats(lastSync, current);
                    Field launchCountField = getField(UsageStats.class, new Config().getUsageStatsFieldLaunchCount());
                    if (usages != null && usages.size() > 0) {
                        AppUsages appUsages = new AppUsages(lastSync, current, null);
                        apps.setUsages(appUsages);
                        for (String pkg : usages.keySet()) {
                            UsageStats us = usages.get(pkg);
                            appUsages.putToUsages(pkg, new AppUsage(us.getFirstTimeStamp(), us.getLastTimeStamp(), us.getTotalTimeInForeground(), getIntField(launchCountField, us, -1)));
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            return apps;
        } catch (Exception e) {
            return null;
        }
    }

    public static void diffMergeApps(Apps src, Apps dst) {
        if (src.getApps() == null)
            return;

        Hashtable<String, App> srcApps = src.getApps();
        List<String> srcPkgs = new ArrayList<String>(srcApps.keySet());
        for (String pkg : srcPkgs) {
            if (pkg == null)
                continue;

            App srcApp = srcApps.get(pkg);
            if (srcApp == null)
                continue;

            Hashtable dstApps = dst.getApps();
            App dstApp = dstApps != null ? (App) dstApps.get(pkg) : null;

            if (!srcApp.equals(dstApp)) {
                dst.putToApps(pkg, srcApp);
            } else {
                srcApps.remove(pkg);
            }
        }
    }
}
