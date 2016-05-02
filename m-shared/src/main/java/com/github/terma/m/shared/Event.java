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
package com.github.terma.m.shared;

public class Event {

    public final long timestamp;
    public final long value;

    public String metric;
    public short metricCode;

    public Event(short metricCode, long timestamp, long value) {
        this.metricCode = metricCode;
        this.timestamp = timestamp;
        this.value = value;
    }

    public Event(String metric, long value) {
        this(metric, System.currentTimeMillis(), value);
    }

    public Event(String metric, long timestamp, long value) {
        this.metric = metric;
        this.timestamp = timestamp;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Event event = (Event) o;

        if (timestamp != event.timestamp) return false;
        if (value != event.value) return false;
        if (metricCode != event.metricCode) return false;
        return metric != null ? metric.equals(event.metric) : event.metric == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (int) (value ^ (value >>> 32));
        result = 31 * result + (metric != null ? metric.hashCode() : 0);
        result = 31 * result + (int) metricCode;
        return result;
    }

    @Override
    public String toString() {
        return "Event {metric: '" + metric + "', code: " + metricCode + ", timestamp: " + timestamp + ", value: " + value + '}';
    }


}
