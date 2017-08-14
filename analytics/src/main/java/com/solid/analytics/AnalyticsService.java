package com.solid.analytics;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.solid.analytics.model.ActivedRequest;
import com.solid.analytics.model.Apps;
import com.solid.analytics.model.Config;
import com.solid.analytics.model.Event;
import com.solid.analytics.model.EventsRequest;
import com.solid.analytics.model.PageEvent;
import com.solid.analytics.model.Properties;
import com.solid.analytics.model.Property;
import com.solid.analytics.model.PropertyList;
import com.solid.analytics.model.Response;
import com.solid.analytics.protocol.Protocol;
import com.solid.analytics.protocol.ProtocolNormal;
import com.solid.analytics.storage.DbStorage;
import com.solid.analytics.storage.Storage;
import com.solid.analytics.storage.StorageCompat;
import com.solid.analytics.thrift.TBase;
import com.solid.analytics.thrift.ThriftUtil;
import com.solid.analytics.transport.HttpTransport;
import com.solid.analytics.transport.Transport;
import com.solid.analytics.util.AndroidUtil;
import com.solid.analytics.util.ModelUtil;
import com.solid.analytics.util.StringUtil;
import com.solid.analytics.util.TimeUtil;
import com.solid.analytics.util.log.Logger;
import com.solid.analytics.util.log.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class AnalyticsService extends IntentService {

    static final Logger log = LoggerFactory.getLogger(AnalyticsService.class);

    public static final String ACTION_INIT = BuildConfig.APPLICATION_ID + ".INIT";
    public static final String ACTION_SET_PROPERTY_LIST = BuildConfig.APPLICATION_ID + ".SET_PROPERTY_LIST";
    public static final String ACTION_SEND_EVENT = BuildConfig.APPLICATION_ID + ".SEND_EVENT";
    public static final String ACTION_CHECK_PROCESS_UP_TIME = BuildConfig.APPLICATION_ID + ".CHECK_PROCESS_UP_TIME";
    public static final String ACTION_SEND_PAGE_EVENT = BuildConfig.APPLICATION_ID + ".SEND_PAGE_EVENT";
    public static final String ACTION_DEBUG_SYNC = BuildConfig.APPLICATION_ID + ".DEBUG_SYNC";
    public static final String ACTION_CONNECTIVITY_SYNC = BuildConfig.APPLICATION_ID + ".CONNECTIVITY_SYNC";
    public static final String ACTION_SCHEDULE_SYNC = BuildConfig.APPLICATION_ID + ".SCHEDULE_SYNC";
    public static final String ACTION_SCHEDULE_SYNC_LAZY = BuildConfig.APPLICATION_ID + ".SCHEDULE_SYNC_LAZY";

    public static final String EXTRA_DATA = "data";

    static final String PREF_NAME = "analytics_state";
    static final String PREF_KEY_UID = "uid";
    static final String PREF_KEY_LAST_SYNC_APPS = "last_sync_apps";
    static final String PREF_KEY_CONFIG = "config";

    static final Handler sHandler = new Handler(Looper.getMainLooper());

    public static void startInit(Context context, Config config) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_INIT);
            setExtraData(intent, config);
            context.startService(intent);
        } catch (Exception e) {
            log.warn("startInit", e);
        }
    }

    public static void startSetPropertyList(Context context, PropertyList propertyList) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_SET_PROPERTY_LIST);
            setExtraData(intent, propertyList);
            context.startService(intent);
        } catch (Exception e) {
            log.warn("startSetPropertyList", e);
        }
    }

    public static void startSendEvent(Context context, Event event) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_SEND_EVENT);
            setExtraData(intent, event);
            context.startService(intent);
        } catch (Exception e) {
            log.warn("startSendEvent", e);
        }
    }

    public static void startCheckProcessUpTime(Context context) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_CHECK_PROCESS_UP_TIME);
            context.startService(intent);
        } catch (Exception e) {
            log.warn("startCheckProcessUpTime", e);
        }
    }

    public static void startSendPageEvent(Context context, PageEvent pageEvent) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_SEND_PAGE_EVENT);
            setExtraData(intent, pageEvent);
            context.startService(intent);
        } catch (Exception e) {
            log.warn("startSendPageEvent", e);
        }
    }

    public static void startDebugSync(Context context) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_DEBUG_SYNC);
            context.startService(intent);
        } catch (Exception e) {
            log.warn("startDebugSync", e);
        }
    }

    public static void startConnectivitySync(Context context) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_CONNECTIVITY_SYNC);
            context.startService(intent);
        } catch (Exception e) {
            log.warn("startConnectivitySync", e);
        }
    }

    public static void scheduleSync(Context context) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_SCHEDULE_SYNC);
            PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TimeUtil.MINUTE * 1L, TimeUtil.MINUTE * 30L, pi);
        } catch (Exception e) {
            log.warn("scheduleSync", e);
        }
    }

    public static void scheduleSyncLazy(Context context) {
        try {
            Intent intent = new Intent(context, AnalyticsService.class);
            intent.setAction(ACTION_SCHEDULE_SYNC_LAZY);
            PendingIntent pi = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            am.cancel(pi);
            am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + TimeUtil.MINUTE * 10L, TimeUtil.HOUR * 6L, pi);
        } catch (Exception e) {
            log.warn("scheduleSyncLazy", e);
        }
    }

    public static <T extends TBase> T getExtraData(Intent intent, Class<T> cls) {
        if (intent == null)
            return null;

        byte[] bytes = intent.getByteArrayExtra(EXTRA_DATA);
        return ThriftUtil.deserialize(bytes, cls);
    }

    public static void setExtraData(Intent intent, TBase data) {
        byte[] bytes = ThriftUtil.serialize(data);
        if (bytes == null)
            return;

        intent.putExtra(EXTRA_DATA, bytes);
    }

    static Config sConfig;

    static Storage sStorage;

    static final String DB_NAME = "analytics.db";

    static Config getConfig(Context context) {
        if (sConfig != null)
            return sConfig;

        Config cfg = loadFromPref(context, PREF_KEY_CONFIG, Config.class);
        if (cfg == null)
            return null;

        sConfig = cfg;
        return sConfig;
    }

    static Storage getStorage(Context context) {
        if (sStorage != null)
            return sStorage;

        sStorage = new StorageCompat(
                new DbStorage(context, DB_NAME));
        return sStorage;
    }

    static Protocol sProtocol;

    static Protocol getProtocol(Context context) {
        if (sProtocol != null)
            return sProtocol;

        sProtocol = new ProtocolNormal();
        return sProtocol;
    }

    static String[] getSuPaths(Context context) {
        if (sConfig != null && sConfig.getSuPaths() != null)
            return sConfig.getSuPaths().split(",");

        return new Config().getSuPaths().split(",");
    }

    public AnalyticsService() {
        super("AnalyticsService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;

        if (log.isDebugEnabled())
            log.debug("onHandleIntent" + " intent:" + intent);
        final String action = intent.getAction();

        final long start = System.currentTimeMillis();
        try {
            if (ACTION_INIT.equals(action)) {
                handleInit(getExtraData(intent, Config.class));
                return;
            }

            if (ACTION_SET_PROPERTY_LIST.equals(action)) {
                handleSetPropertyList(getExtraData(intent, PropertyList.class));
                return;
            }

            if (ACTION_SEND_EVENT.equals(action)) {
                handleSendEvent(getExtraData(intent, Event.class));
                return;
            }

            if (ACTION_SEND_PAGE_EVENT.equals(action)) {
                handleSendPageEvent(getExtraData(intent, PageEvent.class));
                return;
            }

            if (ACTION_DEBUG_SYNC.equals(action)) {
                handleDebugSync();
                return;
            }

            if (ACTION_CONNECTIVITY_SYNC.equals(action)) {
                handleConnectivitySync();
                return;
            }

            if (ACTION_SCHEDULE_SYNC.equals(action)) {
                handleScheduleSync();
                return;
            }

            if (ACTION_SCHEDULE_SYNC_LAZY.equals(action)) {
                handleScheduleSyncLazy();
                return;
            }
        } catch (Exception e) {
            log.warn("onHandleIntent" + " action:" + action, e);
        } finally {
            if (log.isDebugEnabled())
                log.debug("onHandleIntent" + " action:" + action + " used:" + (System.currentTimeMillis() - start) + "ms");
        }
    }

    private void handleInit(Config config) throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleInit" + " config:" + ThriftUtil.toString(config));

        startProcessUpTimeChecker(this);

        if (config == null)
            return;

        sConfig = config;
        saveToPref(this, PREF_KEY_CONFIG, config);

        try {
        } finally {
            scheduleSync(this);
            scheduleSyncLazy(this);
        }
    }

    private void handleSetPropertyList(PropertyList propertyList) throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleSetPropertyList" + " propertyList:" + ThriftUtil.toString(propertyList));

        if (propertyList == null)
            return;

        Storage storage = getStorage(this);
        Properties oldProperties = storage.loadProperties();
        if (oldProperties == null)
            oldProperties = new Properties();

        Properties newProperties = new Properties(oldProperties);
        if (propertyList.getProperties() != null && propertyList.getPropertiesSize() > 0) {
            for (Property property : (List<Property>) propertyList.getProperties()) {
                if (property.getName() == null)
                    continue;

                if (property.getValue() != null) {
                    newProperties.putToProperties(property.getName(), property.getValue());
                } else if (newProperties.getProperties() != null) {
                    newProperties.getProperties().remove(property.getName());
                }
            }
        }

        if (newProperties.equals(oldProperties))
            return;

        storage.saveProperties(newProperties);
    }

    private void handleSendEvent(Event event) throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleSendEvent" + " event:" + ThriftUtil.toString(event));

        Storage storage = getStorage(this);
        storage.saveEvent(event);
    }

    static final Object sLastProcessLock = new Object();
    static long sLastProcessTime = SystemClock.elapsedRealtime();
    static long sLastProcessLaunch = 0;

    static final long CHECK_PROCESS_UP_INTERVAL = TimeUtil.SECOND * 60L;

    static class ProcessTimeChecker implements Runnable {

        Context mContext;

        @Override
        public void run() {
            if (mContext != null)
                AnalyticsService.startCheckProcessUpTime(mContext);

            sHandler.postDelayed(this, CHECK_PROCESS_UP_INTERVAL);
        }

        public void init(Context context) {
            mContext = context;
        }
    }

    static final ProcessTimeChecker sProcessTimeChecker = new ProcessTimeChecker();

    private static void startProcessUpTimeChecker(Context context) {
        sProcessTimeChecker.init(context.getApplicationContext());
        sHandler.post(new Runnable() {
            @Override
            public void run() {
                sHandler.removeCallbacks(sProcessTimeChecker);
                sHandler.post(sProcessTimeChecker);
            }
        });
    }

    private void handleSendPageEvent(PageEvent pageEvent) throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleSendPageEvent" + " pageEvent:" + ThriftUtil.toString(pageEvent));

        Storage storage = getStorage(this);
        storage.savePageEvent(pageEvent);
    }

    private void handleDebugSync() throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleDebugSync");

        Config config = getConfig(this);
        if (config == null) {
            log.warn("handleDebugSync not initialized!");
            return;
        }

        syncActive();

        syncEvent();

        // syncPageEvent();

        // syncProperties();

        syncApps(getLastSync(PREF_KEY_LAST_SYNC_APPS), System.currentTimeMillis());
    }

    private void handleConnectivitySync() throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleConnectivitySync");

        Config config = getConfig(this);
        if (config == null) {
            log.warn("handleConnectivitySync not initialized!");
            return;
        }

        syncActive();

        syncEvent();

        // syncPageEvent();

        // syncProperties();
    }

    private void handleScheduleSync() throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleScheduleSync");

        Config config = getConfig(this);
        if (config == null) {
            log.warn("handleScheduleSync not initialized!");
            return;
        }

        syncActive();

        syncEvent();

        // syncPageEvent();

        // syncProperties();
    }

    private void handleScheduleSyncLazy() throws Exception {
        if (log.isDebugEnabled())
            log.debug("handleScheduleSyncLazy");

        Config config = getConfig(this);
        if (config == null) {
            log.warn("handleScheduleSyncLazy not initialized!");
            return;
        }

        syncActive();

        checkSyncApps();
    }

    private long loadUid() {
        return sp().getLong(PREF_KEY_UID, 0L);
    }

    private boolean checkResponse(Response response) {
        if (response == null)
            return false;

        if (response.getUid() != 0 && response.getUid() != loadUid()) {
            sp().edit().putLong(PREF_KEY_UID, response.getUid()).apply();
        }

        return response.getRet() == 0;
    }

    private boolean syncActive() throws Exception {
        Config config = getConfig(this);
        if (config == null)
            return false;

        Storage storage = getStorage(this);
        if (storage.isActiveSync())
            return true;

        String referrer = AndroidUtil.getReferrer(this);
        if (StringUtil.isEmpty(referrer))
            return false;

        ActivedRequest info = ModelUtil.createActivedRequest(this, getSuPaths(this));

        Protocol protocol = getProtocol(this);
        Transport transport = new HttpTransport(this, config, config.getAnalyticsUrl() + config.getActivatePath(), Analytics.getDeviceId(this), config.getActivateKey());
        if (!checkResponse(transport.transfer(protocol.encode(info))))
            return false;

        storage.markActiveSync();
        return true;
    }

    private boolean syncProperties() throws Exception {
        Config config = getConfig(this);
        if (config == null)
            return false;

        Storage storage = getStorage(this);
        if (storage.isPropertiesSync())
            return true;

        Properties properties = storage.loadProperties();
        if (properties == null)
            return true;

        Protocol protocol = getProtocol(this);
        Transport transport = new HttpTransport(this, config, config.getAnalyticsUrl() + config.getPropertiesPath(), Analytics.getDeviceId(this), config.getPropertiesKey());
        if (!checkResponse(transport.transfer(protocol.encode(properties))))
            return false;

        storage.markPropertiesSync();
        return true;
    }

    private static final int EVENT_LIMIT = 50;

    private boolean syncEvent() throws Exception {
        Config config = getConfig(this);
        if (config == null)
            return false;

        Storage storage = getStorage(this);
        Protocol protocol = getProtocol(this);
        Transport transport = new HttpTransport(this, config, config.getAnalyticsUrl() + config.getEventsPath(), Analytics.getDeviceId(this), config.getEventsKey());
        EventsRequest eventsRequest = ModelUtil.createEventsRequest(this);
        eventsRequest.setUid(loadUid());
        while (true) {
            List<Long> ids = new ArrayList<Long>();
            List<Event> es = storage.loadEvents(EVENT_LIMIT, ids);
            if (es == null || es.size() <= 0)
                return true;

            Vector<Event> events = new Vector<Event>(es);
            if (events == null || events.size() <= 0)
                return true;

            eventsRequest.setEvents(events);
            if (!checkResponse(transport.transfer(protocol.encode(eventsRequest))))
                return false;

            storage.deleteEvents(ids);
        }
    }

    private boolean syncPageEvent() throws Exception {
        Config config = getConfig(this);
        if (config == null)
            return false;

        Storage storage = getStorage(this);
        Protocol protocol = getProtocol(this);
        Transport transport = new HttpTransport(this, config, config.getAnalyticsUrl() + config.getPageEventPath(), Analytics.getDeviceId(this), config.getPageEventKey());
        while (true) {
            List<Long> ids = new ArrayList<Long>();
            List<PageEvent> pageEvents = storage.loadPageEvents(EVENT_LIMIT, ids);
            if (pageEvents == null || pageEvents.size() <= 0)
                return true;

            if (!checkResponse(transport.transfer(protocol.encodePageEvents(pageEvents))))
                return false;

            storage.deletePageEvents(ids);
        }
    }

    private boolean syncApps(final long lastSync, final long current) throws Exception {
        Config config = getConfig(this);
        if (config == null)
            return false;

        if (!config.isUploadAppsInfo())
            return false;

        Storage storage = getStorage(this);

        Apps apps = storage.loadApps();
        if (apps == null)
            apps = new Apps();

        Apps diffApps = ModelUtil.loadAppsAndUsages(this, lastSync, current);
        if (diffApps == null)
            return false;

        ModelUtil.diffMergeApps(diffApps, apps);
        if (diffApps.getAppsSize() <= 0 && diffApps.getUsages() == null)
            return true;

        Protocol protocol = getProtocol(this);
        Transport transport = new HttpTransport(this, config, config.getAnalyticsUrl() + config.getAppsInfoPath(), Analytics.getDeviceId(this), config.getAppsInfoKey());
        if (!checkResponse(transport.transfer(protocol.encode(diffApps))))
            return false;

        // usages will not be saved
        apps.setUsages(null);

        storage.saveApps(apps);
        return true;
    }

    private boolean checkSyncApps() throws Exception {
        long current = System.currentTimeMillis();
        if (!checkSync(PREF_KEY_LAST_SYNC_APPS, current, TimeUtil.DAY * 1L))
            return false;

        boolean sync = syncApps(getLastSync(PREF_KEY_LAST_SYNC_APPS), current);
        if (!sync)
            return false;

        commitSync(PREF_KEY_LAST_SYNC_APPS, current);
        return true;
    }

    long getLastSync(final String key) {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long last = sp.getLong(key, AndroidUtil.getLastUpdateTime(this));
        return last;
    }

    boolean checkSync(final String key, final long current, final long interval) {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long last = sp.getLong(key, AndroidUtil.getLastUpdateTime(this));

        return (current - last) >= interval;
    }

    void commitSync(final String key, final long current) {
        SharedPreferences sp = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putLong(key, current);
        e.apply();
    }

    SharedPreferences sp() {
        return getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    static <T extends TBase> void saveToPref(Context context, String key, T res) {
        try {
            if (res == null)
                return;

            SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            if (sp == null)
                return;

            String v = new String(ThriftUtil.serialize(res), "utf-8");
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(key, v);
            editor.apply();
        } catch (Throwable e) {
            log.warn("saveToPref:", e);
            return;
        }
    }

    static <T extends TBase> T loadFromPref(Context context, String key, Class<T> clazz) {
        try {
            SharedPreferences sp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
            if (sp == null)
                return null;

            String v = sp.getString(key, null);
            if (StringUtil.isEmpty(v))
                return null;

            T cr = ThriftUtil.deserialize(v.getBytes("utf-8"), clazz);
            return cr;
        } catch (Throwable e) {
            log.warn("loadFromPref:", e);
            return null;
        }
    }
}
