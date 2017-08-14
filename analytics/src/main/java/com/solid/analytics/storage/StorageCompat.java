package com.solid.analytics.storage;

import com.solid.analytics.model.Apps;
import com.solid.analytics.model.Event;
import com.solid.analytics.model.PageEvent;
import com.solid.analytics.model.Properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StorageCompat implements Storage {

    private final Storage mMain;

    private final Storage[] mStorages;

    public StorageCompat(Storage main, Storage... compatibles) {
        if (main == null || compatibles == null)
            throw new IllegalArgumentException("can not be null!");
        if (compatibles.length >= 15)
            throw new IllegalArgumentException("compatibles length must not larger than 15!");
        for (Storage storage : compatibles)
            if (storage == null)
                throw new IllegalArgumentException("compatibles can not be null!");

        mMain = main;
        ArrayList<Storage> storages = new ArrayList<Storage>();
        storages.add(mMain);
        storages.addAll(Arrays.asList(compatibles));
        mStorages = storages.toArray(new Storage[storages.size()]);
    }

    @Override
    public boolean isActiveSync() throws Exception {
        return mMain.isActiveSync();
    }

    @Override
    public void markActiveSync() throws Exception {
        mMain.markActiveSync();
    }

    @Override
    public void saveProperties(Properties properties) throws Exception {
        mMain.saveProperties(properties);
    }

    @Override
    public Properties loadProperties() throws Exception {
        return mMain.loadProperties();
    }

    @Override
    public boolean isPropertiesSync() throws Exception {
        return mMain.isPropertiesSync();
    }

    @Override
    public void markPropertiesSync() throws Exception {
        mMain.markPropertiesSync();
    }

    @Override
    public void saveApps(Apps apps) throws Exception {
        mMain.saveApps(apps);
    }

    @Override
    public Apps loadApps() throws Exception {
        return mMain.loadApps();
    }

    static long mask(long id, int idx) {
        return id | (((long) idx) << 60);
    }

    static long restore(long maskedId) {
        return maskedId & 0x0FFFFFFFFFFFFFFFL;
    }

    @Override
    public void saveEvent(Event event) throws Exception {
        mMain.saveEvent(event);
    }

    @Override
    public List<Event> loadEvents(int limit, List<Long> ids) throws Exception {
        final int origin = ids != null ? ids.size() : 0;
        for (int i = 0; i < mStorages.length; i++) {
            Storage storage = mStorages[i];
            List<Event> events = storage.loadEvents(limit, ids);
            if (events == null || events.size() <= 0)
                continue;

            if (ids != null && ids.size() > origin) {
                for (int j = origin; j < ids.size(); j++) {
                    ids.set(j, mask(ids.get(j), i));
                }
            }
            return events;
        }

        return null;
    }

    @Override
    public void deleteEvents(List<Long> ids) throws Exception {
        if (ids == null || ids.size() <= 0)
            return;

        // group
        Map<Integer, List<Long>> group = new HashMap<Integer, List<Long>>();
        for (Long id : ids) {
            if (id == null)
                continue;

            int i = (int) (id >> 60);
            List<Long> is = group.get(i);
            if (is == null) {
                is = new ArrayList<Long>();
                group.put(i, is);
            }
            is.add(restore(id));
        }

        // delete
        for (Integer i : group.keySet()) {
            mStorages[i].deleteEvents(group.get(i));
        }
    }

    @Override
    public void savePageEvent(PageEvent pageEvent) throws Exception {
        mMain.savePageEvent(pageEvent);
    }

    @Override
    public List<PageEvent> loadPageEvents(int limit, List<Long> ids) throws Exception {
        final int origin = ids != null ? ids.size() : 0;
        for (int i = 0; i < mStorages.length; i++) {
            Storage storage = mStorages[i];
            List<PageEvent> pageEvents = storage.loadPageEvents(limit, ids);
            if (pageEvents == null || pageEvents.size() <= 0)
                continue;

            if (ids != null && ids.size() > origin) {
                for (int j = origin; j < ids.size(); j++) {
                    ids.set(j, mask(ids.get(j), i));
                }
            }
            return pageEvents;
        }

        return null;
    }

    @Override
    public void deletePageEvents(List<Long> ids) throws Exception {
        if (ids == null || ids.size() <= 0)
            return;

        // group
        Map<Integer, List<Long>> group = new HashMap<Integer, List<Long>>();
        for (Long id : ids) {
            if (id == null)
                continue;

            int i = (int) (id >> 60);
            List<Long> is = group.get(i);
            if (is == null) {
                is = new ArrayList<Long>();
                group.put(i, is);
            }
            is.add(restore(id));
        }

        // delete
        for (Integer i : group.keySet()) {
            mStorages[i].deletePageEvents(group.get(i));
        }
    }
}
