package com.solid.analytics.protocol;

import com.solid.analytics.model.ActivedRequest;
import com.solid.analytics.model.Apps;
import com.solid.analytics.model.EventRequest;
import com.solid.analytics.model.EventsRequest;
import com.solid.analytics.model.PageEvent;
import com.solid.analytics.model.Properties;
import com.solid.analytics.util.log.Logger;
import com.solid.analytics.util.log.LoggerFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class ProtocolNormal implements Protocol {

    static final Logger log = LoggerFactory.getLogger(ProtocolNormal.class);

    @Override
    public byte[] encode(ActivedRequest activedRequest) throws Exception {
        if (activedRequest == null)
            return null;

        JSONObject obj = new JSONObject();
        activedRequest.write(obj);
        String str = obj.toString();
        if (log.isDebugEnabled())
            log.debug("encode activedRequest:" + str);
        return str.getBytes("utf-8");
    }

    @Override
    public byte[] encode(EventRequest eventRequest) throws Exception {
        if (eventRequest == null)
            return null;

        JSONObject obj = new JSONObject();
        eventRequest.write(obj);
        String str = obj.toString();
        if (log.isDebugEnabled())
            log.debug("encode eventRequest:" + str);
        return str.getBytes("utf-8");
    }

    @Override
    public byte[] encode(EventsRequest eventsRequest) throws Exception {
        if (eventsRequest == null)
            return null;

        JSONObject obj = new JSONObject();
        eventsRequest.write(obj);
        String str = obj.toString();
        if (log.isDebugEnabled())
            log.debug("encode eventsRequest:" + str);
        return str.getBytes("utf-8");
    }

    @Override
    public byte[] encode(Properties properties) throws Exception {
        if (properties == null)
            return null;

        JSONObject obj = new JSONObject();
        properties.write(obj);
        String str = obj.toString();
        if (log.isDebugEnabled())
            log.debug("encode properties:" + str);
        return str.getBytes("utf-8");
    }

    @Override
    public byte[] encodePageEvents(List<PageEvent> pageEvents) throws Exception {
        if (pageEvents == null)
            return null;

        JSONArray array = new JSONArray();
        for (PageEvent pageEvent : pageEvents) {
            if (pageEvent == null)
                continue;

            JSONObject o = new JSONObject();
            pageEvent.write(o);
            array.put(o);
        }
        JSONObject obj = new JSONObject();
        obj.put("events", array);
        String str = obj.toString();
        if (log.isDebugEnabled())
            log.debug("encode pageEvents:" + str);
        return str.getBytes("utf-8");
    }

    public byte[] encode(Apps apps) throws Exception {
        if (apps == null)
            return null;

        JSONObject obj = new JSONObject();
        apps.write(obj);
        String str = obj.toString();
        if (log.isDebugEnabled())
            log.debug("encode apps:" + str);
        return str.getBytes("utf-8");
    }
}
