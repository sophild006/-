package com.solid.analytics.protocol;

import com.solid.analytics.model.ActivedRequest;
import com.solid.analytics.model.Apps;
import com.solid.analytics.model.EventRequest;
import com.solid.analytics.model.EventsRequest;
import com.solid.analytics.model.PageEvent;
import com.solid.analytics.model.Properties;

import java.util.List;

public interface Protocol {

    byte[] encode(ActivedRequest activedRequest) throws Exception;

    byte[] encode(EventRequest eventRequest) throws Exception;

    byte[] encode(EventsRequest eventsRequest) throws Exception;

    byte[] encode(Properties properties) throws Exception;

    byte[] encodePageEvents(List<PageEvent> pageEvents) throws Exception;

    byte[] encode(Apps apps) throws Exception;
}
