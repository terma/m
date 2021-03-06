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
import java.util.regex.Pattern;

/**
 * Thread safe
 */
@SuppressWarnings("WeakerAccess")
class EventsImpl implements Events {

    private final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    private final Lock READ_LOCK = LOCK.readLock();
    private final Lock WRITE_LOCK = LOCK.writeLock();
    private final Repo repo;
    private final Map<Short, String> codeToMetrics = new HashMap<>();
    private final Map<String, Short> metricToCodes = new HashMap<>();

    // todo start use clear if implemented
    // doesn't support clear so can't be final
    private FastSelect<Event> fastSelect;

    public EventsImpl(final String dataPath) {
        WRITE_LOCK.lock();
        try {
            fastSelect = createFastSelect();
            try {
                if (dataPath == null) {
                    repo = null;
                } else {
                    repo = new Repo(dataPath);
                    metricToCodes.putAll(repo.readMetricCodes());
                    for (Map.Entry<String, Short> p : metricToCodes.entrySet())
                        codeToMetrics.put(p.getValue(), p.getKey());
                    repo.readEvents(fastSelect);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    // visibility for tests
    static FastSelect<Event> createFastSelect() {
        return new FastSelect<>(1000, Event.class,
                Arrays.asList(
                        new FastSelect.Column("metricCode", short.class, 300000),
                        new FastSelect.Column("timestamp", long.class, 300000),
                        new FastSelect.Column("value", long.class, 300000)));
    }

    private int[] findMetricCodes(final String regex) {
        final Pattern pattern = regex == null || regex.isEmpty() ? null : Pattern.compile(regex);

        final List<Short> codes = new ArrayList<>();
        for (Map.Entry<String, Short> metricToCode : metricToCodes.entrySet()) {
            if (pattern == null || pattern.matcher(metricToCode.getKey()).find()) codes.add(metricToCode.getValue());
        }

        final int[] result = new int[codes.size()];
        for (int i = 0; i < codes.size(); i++) result[i] = codes.get(i);
        return result;
    }

    /**
     * @param parts   - precision (points in same part will be AVG)
     * @param min     - inclusive
     * @param max     - exclusive
     * @param pattern - regex
     * @return map of events with list of points for period
     */
    public Map<String, List<Point>> get(final int parts, final long min, final long max, String pattern) {
        READ_LOCK.lock();
        try {
            final AbstractRequest[] where = createWhere(min, max, pattern);
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
            fastSelect = createFastSelect();
            metricToCodes.clear();
            codeToMetrics.clear();
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    public long space() {
        READ_LOCK.lock();
        try {
            return repo == null ? 0 : repo.space();
        } finally {
            READ_LOCK.unlock();
        }
    }

    public int events() {
        READ_LOCK.lock();
        try {
            return fastSelect.size();
        } finally {
            READ_LOCK.unlock();
        }
    }

    @Override
    public long sum(long min, long max, String metric) {
        READ_LOCK.lock();
        try {
            final SumCallback callback = new SumCallback(fastSelect);
            fastSelect.select(createWhere(min, max, metric), callback);
            return callback.getResult();
        } finally {
            READ_LOCK.unlock();
        }
    }

    private AbstractRequest[] createWhere(long min, long max, String metric) {
        return new AbstractRequest[]{
                new LongBetweenRequest("timestamp", min, max - 1),
                new ShortRequest("metricCode", findMetricCodes(metric))
        };
    }

    @SuppressWarnings("WeakerAccess")
    static class Point {

        final long timestamp;
        final Long value;

        public Point(final long timestamp, final Long value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public Point(final long timestamp) {
            this(timestamp, null);
        }

        public Point(final long timestamp, final int value) {
            this.timestamp = timestamp;
            this.value = (long) value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;
            return timestamp == point.timestamp && (value != null ? value.equals(point.value) : point.value == null);

        }

        @Override
        public int hashCode() {
            int result = (int) (timestamp ^ (timestamp >>> 32));
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Point {timestamp: " + timestamp + ", value: " + value + '}';
        }

    }

}
