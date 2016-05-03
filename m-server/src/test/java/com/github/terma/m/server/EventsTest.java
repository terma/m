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
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.util.Collections.singletonList;

public class EventsTest {

    private final Events events = new Events(null);

    @Test
    public void supportNullRepo() throws IOException {
        new Events(null);
    }

    @Test
    public void noDataEmptyResult() throws IOException {
        Assert.assertEquals(0, events.get(3, 0, 100, "nona").size());
    }

    @Test
    public void getData() throws IOException {
        events.add(singletonList(new Event("a", 55, 901)));

        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("a", Arrays.asList(
                            new Events.Point(50, 901),
                            new Events.Point(67),
                            new Events.Point(84),
                            new Events.Point(101)
                    ));
                }},
                events.get(3, 50, 100, ""));
    }

    @Test
    public void whenGetFewEventsInOneChunkThenTakeAverage() throws IOException {
        events.add(Arrays.asList(
                new Event("a", 50, 50),
                new Event("a", 55, 100),
                new Event("a", 56, 150)
        ));

        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("a", Arrays.asList(
                            new Events.Point(50, 100),
                            new Events.Point(67),
                            new Events.Point(84),
                            new Events.Point(101)
                    ));
                }},
                events.get(3, 50, 100, ""));
    }

    @Test
    public void shouldFilterByIncludeMinExcludeMax() throws IOException {
        events.add(Arrays.asList(
                new Event("a", 49, 500),
                new Event("a", 50, 1),
                new Event("a", 100, 1)
        ));

        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("a", Arrays.asList(
                            new Events.Point(50, 1),
                            new Events.Point(67),
                            new Events.Point(84),
                            new Events.Point(101)
                    ));
                }},
                events.get(3, 50, 100, ""));
    }

    @Test
    public void shouldAlwaysReturnAtTheEndAdditionalNodeWithoutData() throws IOException {
        events.add(Arrays.asList(
                new Event("a", 49, 500),
                new Event("a", 50, 1),
                new Event("a", 100, 1),
                new Event("a", 101, 500)
        ));

        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("a", Arrays.asList(
                            new Events.Point(25, 500),
                            new Events.Point(50, 1),
                            new Events.Point(75),
                            new Events.Point(100)
                    ));
                }},
                events.get(3, 25, 100, ""));
    }

    @Test
    public void shouldFilterMetricByPattern() throws IOException {
        events.add(Arrays.asList(
                new Event("metric.host1.b", 50, 1),
                new Event("metric.host1.c.used", 99, 1)
        ));

        Assert.assertEquals(Collections.EMPTY_MAP, events.get(3, 0, 200, "nona"));
        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("metric.host1.b", Arrays.asList(new Events.Point(50, 1), new Events.Point(67), new Events.Point(84), new Events.Point(101)));
                    put("metric.host1.c.used", Arrays.asList(new Events.Point(50), new Events.Point(67), new Events.Point(84, 1), new Events.Point(101)));
                }},
                events.get(3, 50, 100, ""));
        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("metric.host1.b", Arrays.asList(new Events.Point(50, 1), new Events.Point(67), new Events.Point(84), new Events.Point(101)));
                    put("metric.host1.c.used", Arrays.asList(new Events.Point(50), new Events.Point(67), new Events.Point(84, 1), new Events.Point(101)));
                }},
                events.get(3, 50, 100, null));
        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("metric.host1.c.used", Arrays.asList(new Events.Point(50), new Events.Point(67), new Events.Point(84, 1), new Events.Point(101)));
                }},
                events.get(3, 50, 100, ".c.used"));
    }

    @Test
    public void shouldFilterMetricByRegexPattern() throws IOException {
        events.add(Arrays.asList(
                new Event("metric.host1.b", 50, 1),
                new Event("metric.host1.c.used", 56, 10),
                new Event("metric.host2.c.used", 99, 1)
        ));

        Assert.assertEquals(Collections.EMPTY_MAP, events.get(3, 0, 200, "omeba"));
        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("metric.host1.c.used", Arrays.asList(new Events.Point(50, 10), new Events.Point(67), new Events.Point(84), new Events.Point(101)));
                    put("metric.host2.c.used", Arrays.asList(new Events.Point(50), new Events.Point(67), new Events.Point(84, 1), new Events.Point(101)));
                }},
                events.get(3, 50, 100, "host\\d+.c"));
    }

    @Test
    public void shouldReturnNullForValueIfNoDataForSomeParts() throws IOException {
        events.add(singletonList(new Event("metric.b", 50, 1)));

        Assert.assertEquals(
                new HashMap<String, List<Events.Point>>() {{
                    put("metric.b", Arrays.asList(new Events.Point(50, 1), new Events.Point(67, null), new Events.Point(84, null), new Events.Point(101, null)));
                }},
                events.get(3, 50, 100, null));
    }

    @Test
    public void shouldReturnNothingIfMaxLessMin() throws IOException {
        events.add(Arrays.asList(
                new Event("a", 49, 500),
                new Event("a", 50, 1),
                new Event("a", 100, 1)
        ));

        Assert.assertEquals(Collections.EMPTY_MAP, events.get(3, 101, 100, "nona"));
        Assert.assertEquals(Collections.EMPTY_MAP, events.get(3, 101, 0, "nona"));
        Assert.assertEquals(Collections.EMPTY_MAP, events.get(3, Long.MAX_VALUE, 0, "nona"));
        Assert.assertEquals(Collections.EMPTY_MAP, events.get(3, 100, 100, "nona"));
    }

}
