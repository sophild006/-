package com.solid.analytics.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.appsflyer.AppsFlyerProperties;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;

import java.io.File;
import java.util.List;
import java.util.Locale;

public class AndroidUtil {

    public static String getImei(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String ret = tm.getDeviceId();
            return ret != null ? ret : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getImsi(Context ctx) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            String ret = telephonyManager.getSubscriberId();
            return ret != null ? ret : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getSimOperator(Context ctx) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            String ret = telephonyManager.getSimOperator();
            return ret != null ? ret : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getAndroidId(Context ctx) {
        try {
            return Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDeviceIdImeiAndroidId(Context context) {
        String androidId = "";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            return imei + androidId;
        } catch (Exception e) {
            return androidId;
        }
    }

    public static String getDeviceIdAndroidId(Context context) {
        String androidId = "";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return androidId;
        } catch (Exception e) {
            return androidId;
        }
    }

    public static String getDeviceIdSerialAndroidId(Context context) {
        String androidId = "";
        try {
            androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return Build.SERIAL + androidId;
        } catch (Exception e) {
            return androidId;
        }
    }

    public static String getMacAddress(Context ctx) {
        try {
            WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wm.getConnectionInfo();
            String ret = wifiInfo.getMacAddress();
            return ret != null ? ret : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getLauncherPackageName(Context ctx) {
        try {
            Intent queryIntent = new Intent(Intent.ACTION_MAIN);
            queryIntent.addCategory(Intent.CATEGORY_HOME);
            ResolveInfo resolveInfo = ctx.getPackageManager().resolveActivity(queryIntent, PackageManager.MATCH_DEFAULT_ONLY);
            if (resolveInfo == null)
                return "";

            String launcherPackageName = resolveInfo.activityInfo.packageName;
            return launcherPackageName != null ? launcherPackageName : "";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getVersionName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static String getMeta(Context context, String key) {
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            return applicationInfo.metaData.getString(key);
        } catch (Exception e) {
            return "";
        }
    }

    public static String queryMeta(Context context, String... keys) {
        try {
            if (keys == null || keys.length <= 0)
                return null;

            PackageManager pm = context.getPackageManager();
            ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle meta = applicationInfo.metaData;
            for (String key : keys) {
                Object m = meta.get(key);
                String value = m != null ? m.toString() : null;
                if (value != null && !value.isEmpty())
                    return value;
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getChannel(Context context) {
        return queryMeta(context, "channel", "CHANNEL");
    }

    public static String getTrafficId(Context context) {
        return queryMeta(context, "traffic_id", "TRAFFIC_ID");
    }

    public static long getFirstInstallTime(Context context) {
        try {
            final PackageManager pm = context.getPackageManager();
            final PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            long appInstallTime = packageInfo.firstInstallTime;
            return appInstallTime;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static long getLastUpdateTime(Context context) {
        try {
            final PackageManager pm = context.getPackageManager();
            final PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
            long appInstallTime = packageInfo.lastUpdateTime;
            return appInstallTime;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @SuppressLint("NewApi")
    public static void getScreenSize(Context context, Point point) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            final DisplayMetrics dm = context.getResources().getDisplayMetrics();
            point.set(dm.widthPixels, dm.heightPixels);
        } else {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            wm.getDefaultDisplay().getSize(point);
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            final PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return pi != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getInstaller(Context c) {
        try {
            PackageManager manager = c.getPackageManager();
            String installer = manager.getInstallerPackageName(c.getPackageName());
            return installer != null ? installer : "";
        } catch (Throwable e) {
            e.printStackTrace();
            return "";
        }
    }

    public static final String BUCKET_ID_DEBUG_FILE = "debug_bucket_id.txt";

    public static int getBucketId(Context context) {
        try {
            File f = new File(Environment.getExternalStorageDirectory(), BUCKET_ID_DEBUG_FILE);
            if (f.exists()) {
                return Integer.parseInt(StringUtil.toString(f, "utf-8").trim());
            }

            String md5 = Md5.md5(("" + Build.SERIAL + "_" + AndroidUtil.getAndroidId(context)).getBytes("utf-8"));
            return Integer.parseInt(md5.substring(0, 4), 16) * 100 / 65536;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static boolean existOneFileIn(String[] paths) {
        try {
            for (String path : paths)
                if (new File(path).exists())
                    return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getDisplayDpi(Context context) {
        try {
            final float density = context.getResources().getDisplayMetrics().density;

            if (density == 0.75f)
                return "ldpi";
            if (density == 1.0f)
                return "mdpi";
            if (density == 1.5f)
                return "hdpi";
            if (density == 2.0f)
                return "xhdpi";
            if (density == 3.0f)
                return "xxhdpi";

            return "density:" + density;
        } catch (Exception e) {
            return "unknown";
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return netInfo != null && netInfo.isConnectedOrConnecting();
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static String getNetworkOperator(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkOperator();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getNetworkOperatorName(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkOperatorName();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getNetworkType(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final int type = tm.getNetworkType();
        switch (type) {
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "1xrtt";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "cdma";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "edge";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "ehrpd";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "evdo_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "evdo_a";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "evdo_b";
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "gprs";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "hsdpa";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "hspa";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "hsupa";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "iden";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "umts";
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return "unkown";
            default:
                return "unkown";
        }
    }

    public static Address loadAddress(Context context) {
        try {
            // try get latitude and longitude
            double latitude = 0;
            double longitude = 0;
            LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            List<String> providers = lm.getProviders(new Criteria(), true);
            if (providers != null && providers.size() > 0) {
                Location lastKnownLocation = lm.getLastKnownLocation(providers.get(0));
                if (lastKnownLocation != null) {
                    latitude = lastKnownLocation.getLatitude();
                    longitude = lastKnownLocation.getLongitude();
                }
            }

            if (latitude == 0 && longitude == 0)
                return null;

            Address address = null;

            // try geocode
            try {
                Geocoder coder = new Geocoder(context.getApplicationContext(), Locale.ENGLISH);
                List<Address> addresses = coder.getFromLocation(latitude, longitude, 1);
                if (addresses != null && addresses.size() > 0) {
                    address = addresses.get(0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (address != null)
                return address;

            // use default
            address = new Address(Locale.getDefault());
            address.setLatitude(latitude);
            address.setLongitude(longitude);
            return address;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getGoogleAdId(Context context) {
        try {
            AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
            return adInfo.getId();
        } catch (Throwable e) {
            return null;
        }
    }

    public static String getReferrer(Context context) {
        try {
            return AppsFlyerProperties.getInstance().getReferrer(context);
        } catch (Throwable e) {
            return null;
        }
    }
}
