namespace java com.solid.analytics.model

struct Config {
    1: string analyticsUrl; // url for base analytics

    20: string channel; // channel

    30: bool uploadAppsInfo; // if upload apps info

    // interface url paths
    40: string activatePath = "/api/actived";
    41: string heartbeatPath = "/api/hb";
    42: string eventPath = "/api/event";
    43: string eventsPath = "/api/events";
    44: string propertiesPath = "/api/prop";
    45: string pageEventPath = "/api/page";
    46: string appsInfoPath = "/api/apps";

    60: string activateKey = "com.zoomy.user.solt@zoomy-media.com";
    61: string heartbeatKey = "com.zoomy.heartbeat.solt@zoomy-media.com";
    62: string eventKey = "com.zoomy.event.solt@zoomy-media.com";
    63: string eventsKey = "com.zoomy.event.solt@zoomy-media.com";
    64: string propertiesKey = "com.zoomy.prop.solt@zoomy-media.com";
    65: string pageEventKey = "com.zoomy.page.solt@zoomy-media.com";
    66: string appsInfoKey = "com.zoomy.apps.solt@zoomy-media.com";

    90: string suPaths =  "/system/bin/su,/system/xbin/su,/system/sbin/su,/sbin/su,/vendor/bin/su";
    91: string usageStatsFieldLaunchCount = "mLaunchCount";
}

struct Response {
    1: i32 ret = -1;
    2: string errinfo;
    3: i64 uid;
}

struct ActivedRequest {
    1: string pub_id;
    2: string pkgname;
    3: string region;
    4: string country;
    5: string timezone;
    6: string carrier;
    7: double longitude;
    8: double latitude;
    9: i32 facebook;
    10: i32 gpservice;
    11: string language;
    12: string model;
    13: string os;
    14: string os_ver;
    15: string app_ver;
    16: string android_id;
    17: string gaid;
    18: string idfa;
    19: string imsi;
    20: string imei;
    21: string imei2;
    22: string mac;
    23: string macsha1;
    24: string macmd5;
    25: string utm_source;
    26: string utm_term;
    27: string utm_medium;
    28: string utm_content;
    29: string utm_campaign;
    30: string ext1;
    31: string ext2;
    32: string ext3;
    33: string ext4;
    34: string ext5;
    35: string install_date;
    36: string serial;
    37: i32 bid;
    38: string referrer;
}

struct Event {
    1: string category;
    2: string event_code;
    3: string label;
    4: string value;
    5: string extra;
    6: string created;
    7: i32 event_occurred_ver;
}

struct EventRequest {
    1: i64 uid;
    2: string pkgname;
    3: string carrier;
    4: double longitude;
    5: double latitude;
    6: string android_id;
    7: string gaid;
    8: string idfa;
    9: string imsi;
    10: string imei;
    11: string app_ver;
    12: string serial;
    13: i32 bid;

    21: string category;
    22: string event_code;
    23: string label;
    24: string value;
    25: string extra;
    26: string created;
    27: i32 event_occurred_ver;
}

struct EventsRequest {
    1: i64 uid;
    2: string pkgname;
    3: string carrier;
    4: double longitude;
    5: double latitude;
    6: string android_id;
    7: string gaid;
    8: string idfa;
    9: string imsi;
    10: string imei;
    11: string app_ver;
    12: string serial;
    13: i32 bid;

    21: list<Event> events;
}

struct PageEvent {
    1: string act;
    2: string ext;
    3: i64 st;
    4: i64 et;
}

struct Property {
    1: string name;
    2: string value;
}

struct PropertyList {
    1: list<Property> properties;
}

struct Properties {
    1: map<string, string> properties;
}

struct App {
    1: i32 ver; // app version code
    2: i64 fi; // first install time in millisecond
    3: i64 lu; // last update time in millisecond
    4: i32 af; // application flags, FLAG_SYSTEM = 1<<0
}

struct AppUsage {
    1: i64 bt; // begin time
    2: i64 et; // end time
    3: i64 ftt; // foreground total time
    4: i32 lc; // launch count
}

struct AppUsages {
    1: i64 bt; // begin time of stats
    2: i64 et; // end time of stats
    3: map<string, AppUsage> usages; // usages
}

struct Apps {
    1: map<string, App> apps; // apps, key is app package name

    2: AppUsages usages; // app usages
}
