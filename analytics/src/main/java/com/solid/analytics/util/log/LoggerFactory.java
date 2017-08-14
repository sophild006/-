package com.solid.analytics.util.log;

import android.os.Environment;
import android.util.Log;

import java.io.File;

public class LoggerFactory {

    public static final String ROOT_NAME = "Global";

    public static final String MODULE_NAME = "Analytics";

    private static final ExternalDebugChecker mRootExternalDebugChecker = new ExternalDebugChecker(ROOT_NAME);

    private static final ExternalDebugChecker mSdkExternalDebugChecker = new ExternalDebugChecker(MODULE_NAME);

    static boolean sTraceEnabled = false;
    static boolean sDebugEnabled = false;
    static boolean sInfoEnabled = true;
    static boolean sWarnEnabled = true;
    static boolean sErrorEnabled = true;

    public static boolean isTraceEnabled() {
        return sTraceEnabled;
    }

    public static void setTraceEnabled(boolean traceEnabled) {
        sTraceEnabled = traceEnabled;
    }

    public static boolean isDebugEnabled() {
        return sDebugEnabled;
    }

    public static void setDebugEnabled(boolean debugEnabled) {
        sDebugEnabled = debugEnabled;
    }

    public static boolean isInfoEnabled() {
        return sInfoEnabled;
    }

    public static void setInfoEnabled(boolean infoEnabled) {
        sInfoEnabled = infoEnabled;
    }

    public static boolean isWarnEnabled() {
        return sWarnEnabled;
    }

    public static void setWarnEnabled(boolean warnEnabled) {
        sWarnEnabled = warnEnabled;
    }

    public static boolean isErrorEnabled() {
        return sErrorEnabled;
    }

    public static void setErrorEnabled(boolean errorEnabled) {
        sErrorEnabled = errorEnabled;
    }

    public static Logger getLogger(String name) {
        final String loggerName = (name != null && !name.isEmpty()) ? name : mSdkExternalDebugChecker.mName;
        final String tag = (name != null && !name.isEmpty()) ? (mSdkExternalDebugChecker.mName + "." + name) : mSdkExternalDebugChecker.mName;
        return new Logger() {

            private final ExternalDebugChecker mExternalDebugChecker = new ExternalDebugChecker(tag);

            private boolean isExternalDebug() {
                return mRootExternalDebugChecker.isExternalDebug() || mSdkExternalDebugChecker.isExternalDebug() || mExternalDebugChecker.isExternalDebug();
            }

            @Override
            public String getName() {
                return loggerName;
            }

            @Override
            public boolean isTraceEnabled() {
                return LoggerFactory.isTraceEnabled() || isExternalDebug();
            }

            @Override
            public void trace(String msg) {
                if (!isTraceEnabled())
                    return;

                Log.d(tag, msg);
            }

            @Override
            public void trace(String msg, Throwable e) {
                if (!isTraceEnabled())
                    return;

                Log.d(tag, msg, e);
            }

            @Override
            public boolean isDebugEnabled() {
                return LoggerFactory.isDebugEnabled() || isExternalDebug();
            }

            @Override
            public void debug(String msg) {
                if (!isDebugEnabled())
                    return;

                Log.d(tag, msg);
            }

            @Override
            public void debug(String msg, Throwable e) {
                if (!isDebugEnabled())
                    return;

                Log.d(tag, msg, e);
            }

            @Override
            public boolean isInfoEnabled() {
                return LoggerFactory.isInfoEnabled() || isExternalDebug();
            }

            @Override
            public void info(String msg) {
                if (!isInfoEnabled())
                    return;

                Log.i(tag, msg);
            }

            @Override
            public void info(String msg, Throwable e) {
                if (!isInfoEnabled())
                    return;

                Log.i(tag, msg, e);
            }

            @Override
            public boolean isWarnEnabled() {
                return LoggerFactory.isWarnEnabled() || isExternalDebug();
            }

            @Override
            public void warn(String msg) {
                if (!isWarnEnabled())
                    return;

                Log.w(tag, msg);
            }

            @Override
            public void warn(String msg, Throwable e) {
                if (!isWarnEnabled())
                    return;

                Log.w(tag, msg, e);
            }

            @Override
            public boolean isErrorEnabled() {
                return LoggerFactory.isErrorEnabled() || isExternalDebug();
            }

            @Override
            public void error(String msg) {
                if (!isErrorEnabled())
                    return;

                Log.e(tag, msg);
            }

            @Override
            public void error(String msg, Throwable e) {
                if (!isErrorEnabled())
                    return;

                Log.e(tag, msg, e);
            }
        };
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

    private static class ExternalDebugChecker {

        final String mName;
        Boolean mIsExternalDebug;

        public ExternalDebugChecker(String name) {
            mName = name;
        }

        public boolean isExternalDebug() {
            if (mIsExternalDebug == null) {
                mIsExternalDebug = existInExternalStorage("logger." + mName + ".debug");
            }

            return mIsExternalDebug != null ? mIsExternalDebug : false;
        }

        private static boolean existInExternalStorage(String file) {
            try {
                return new File(Environment.getExternalStorageDirectory(), file).exists();
            } catch (Exception e) {
                return false;
            }
        }
    }
}
