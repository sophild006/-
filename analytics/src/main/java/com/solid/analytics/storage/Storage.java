package com.solid.analytics.storage;

import com.solid.analytics.model.Apps;
import com.solid.analytics.model.Event;
import com.solid.analytics.model.PageEvent;
import com.solid.analytics.model.Properties;

import java.util.List;

public interface Storage {

    boolean isActiveSync() throws Exception;

    void markActiveSync() throws Exception;

    void saveProperties(Properties properties) throws Exception;

    Properties loadProperties() throws Exception;

    boolean isPropertiesSync() throws Exception;

    void markPropertiesSync() throws Exception;

    void saveApps(Apps apps) throws Exception;

    Apps loadApps() throws Exception;

    void saveEvent(Event event) throws Exception;

    List<Event> loadEvents(int limit, List<Long> ids) throws Exception;

    void deleteEvents(List<Long> ids) throws Exception;

    void savePageEvent(PageEvent pageEvent) throws Exception;

    List<PageEvent> loadPageEvents(int limit, List<Long> ids) throws Exception;

    void deletePageEvents(List<Long> ids) throws Exception;
}
