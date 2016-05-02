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

import com.github.terma.fastselect.AbstractRequest;
import com.github.terma.fastselect.FastSelect;
import com.github.terma.fastselect.LongBetweenRequest;
import com.github.terma.fastselect.ShortRequest;
import com.github.terma.m.shared.Event;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Thread safe
 */
class Events {

    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private final Lock READ_LOCK = LOCK.readLock();
    private final Lock WRITE_LOCK = LOCK.writeLock();
    private final Repo repo;
    private final Map<Short, String> codeToMetrics = new HashMap<>();
    private final Map<String, Short> metricToCodes = new HashMap<>();

    // todo start use clear if implemented
    // doesn't support clear so can't be final
    private FastSelect<Event> fastSelect;

    public Events(final String dataPath) {
        WRITE_LOCK.lock();
        try {
            createFastSelect();
            try {
                if (dataPath == null) {
                    repo = null;
                } else {
                    repo = new Repo(dataPath);
                    metricToCodes.putAll(repo.readMetricCodes());
                    for (Map.Entry<String, Short> p : metricToCodes.entrySet())
                        codeToMetrics.put(p.getValue(), p.getKey());
                    fastSelect.addAll(repo.readEvents());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    private void createFastSelect() {
        fastSelect = new FastSelect<>(1000, Event.class,
                Arrays.asList(
                        new FastSelect.Column("metricCode", short.class, 300000),
                        new FastSelect.Column("timestamp", long.class, 300000),
                        new FastSelect.Column("value", long.class, 300000)));
    }

    private int[] findMetricCodes(final String pattern) {
        List<Short> codes = new ArrayList<>();
        for (Map.Entry<String, Short> metricToCode : metricToCodes.entrySet()) {
            if (pattern == null || metricToCode.getKey().contains(pattern)) codes.add(metricToCode.getValue());
        }

        final int[] result = new int[codes.size()];
        for (int i = 0; i < codes.size(); i++) result[i] = codes.get(i);
        return result;
    }

    public Map<String, List<Point>> get(final int parts, final long min, final long max, String pattern) {
        READ_LOCK.lock();
        try {
            final AbstractRequest[] where = {
                    new LongBetweenRequest("timestamp", min, max),
                    new ShortRequest("metricCode", findMetricCodes(pattern))
            };
            final DataCallback callback = new DataCallback(fastSelect, parts, min, max);
            fastSelect.select(where, callback);
            Map<Short, Acc[]> result = callback.getResult();

            Map<String, List<Point>> tt = new TreeMap<>();

            for (Map.Entry<Short, Acc[]> p : result.entrySet()) {
                List<Point> points = new ArrayList<>();
                for (Acc acc : p.getValue()) {
                    points.add(new Point(acc.timestamp, acc.getAvg()));
                }

                tt.put(codeToMetrics.get(p.getKey()), points);
            }
            return tt;
        } finally {
            READ_LOCK.unlock();
        }
    }

    public void add(List<Event> newEvents) throws IOException {
        WRITE_LOCK.lock();
        try {
            for (Event event : newEvents) {
                Short code = metricToCodes.get(event.metric);
                if (code == null) {
                    code = (short) metricToCodes.size();
                    metricToCodes.put(event.metric, code);
                    codeToMetrics.put(code, event.metric);
                }
                event.metricCode = code;
            }

            if (repo != null) {
                repo.storeMetricCodes(metricToCodes);
                repo.addEvents(newEvents);
            }
            fastSelect.addAll(newEvents);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    public long min() {
        READ_LOCK.lock();
        try {
            final MinTimestampCallback callback = new MinTimestampCallback(fastSelect);
            fastSelect.select(new AbstractRequest[0], callback);
            return callback.getResult();
        } finally {
            READ_LOCK.unlock();
        }
    }

    public void clear() {
        WRITE_LOCK.lock();
        try {
            try {
                repo.clear();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
            createFastSelect();
            metricToCodes.clear();
            codeToMetrics.clear();
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    static class Point {

        final long timestamp;
        final long value;

        public Point(long timestamp, long value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        @Override
        public String toString() {
            return "Point {timestamp: " + timestamp + ", value: " + value + '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;

            if (timestamp != point.timestamp) return false;
            return value == point.value;

        }

        @Override
        public int hashCode() {
            int result = (int) (timestamp ^ (timestamp >>> 32));
            result = 31 * result + (int) (value ^ (value >>> 32));
            return result;
        }
    }

}
