/*

    Copyright 2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.github.terma.m.server;

import com.github.terma.m.shared.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * Thread safe
 */
@SuppressWarnings("WeakerAccess")
class EventsLoader implements Events {

    private static final Logger LOGGER = Logger.getLogger(EventsLoader.class.getName());

    private final ReentrantLock lock = new ReentrantLock();

    private List<Event> buffer;
    private Events events;
    private Exception exception;

    public EventsLoader(final EventsFactory eventsFactory) {
        lock.lock();
        try {
            buffer = new ArrayList<>();

            final Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    restore(eventsFactory);
                }
            });
            thread.setDaemon(true);
            thread.start();
        } finally {
            lock.unlock();
        }
    }

    private void restore(final EventsFactory eventsFactory) {
        LOGGER.info("Start restore events...");
        try {
            events = eventsFactory.createInstance();
            LOGGER.info("Events restored");

            LOGGER.info("Start add buffered events " + buffer.size() + "...");
            lock.lock();
            try {
                events.add(buffer);
                buffer = null;
            } finally {
                lock.unlock();
            }
            LOGGER.info("Buffered events added");
        } catch (Exception e) {
            exception = e;
        }
    }

    @Override
    public Map<String, List<EventsImpl.Point>> get(final int parts, final long min, final long max, String pattern) {
        return getEvents().get(parts, min, max, pattern);
    }

    @Override
    public void add(List<Event> newEvents) throws IOException {
        lock.lock();
        try {
            if (events != null) events.add(newEvents);
            else buffer.addAll(newEvents);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public long min() {
        return getEvents().min();
    }

    @Override
    public void clear() {
        getEvents().clear();
    }

    @Override
    public long space() {
        return getEvents().space();
    }

    @Override
    public int events() {
        return getEvents().events();
    }

    @Override
    public long sum(long min, long max, String metric) {
        return 0;
    }

    private Events getEvents() {
        lock.lock();
        try {
            if (exception != null) {
                throw new EventsLoadException(exception);
            } else if (events == null) {
                throw new EventsNotReadyException();
            } else {
                return events;
            }
        } finally {
            lock.unlock();
        }
    }

}
