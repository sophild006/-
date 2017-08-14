package com.solid.analytics.util.log;

public interface Logger {

    String getName();

    boolean isTraceEnabled();

    void trace(String msg);

    void trace(String msg, Throwable e);

    boolean isDebugEnabled();

    void debug(String msg);

    void debug(String msg, Throwable e);

    boolean isInfoEnabled();

    void info(String msg);

    void info(String msg, Throwable e);

    boolean isWarnEnabled();

    void warn(String msg);

    void warn(String msg, Throwable e);

    boolean isErrorEnabled();

    void error(String msg);

    void error(String msg, Throwable e);
}
