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
package com.github.terma.m.node;

import com.github.terma.m.node.jmx.*;
import com.github.terma.m.shared.Event;

import javax.management.openmbean.CompositeDataSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static com.github.terma.m.node.StringUtils.enrich;

@SuppressWarnings("WeakerAccess")
class Jvm extends HostAwareChecker {

    private static final Logger LOGGER = Logger.getLogger(Jvm.class.getName());

    private static final String MEM_OBJECT_NAME = "java.lang:type=Memory";
    private static final String CPU_OBJECT_NAME = "java.lang:type=OperatingSystem";

    private final JmxConnectionFactory jmxConnectionFactory = new JmxConnectionFactoryImpl();
    private final JmxUrlLocator jmxUrlLocator;
    private final String metricPrefix;

    public Jvm(final String host, final Map<String, String> params) {
        super(host);
        this.jmxUrlLocator = JmxUrlLocator.create(params);
        this.metricPrefix = params.get("metricPrefix");
    }

    public List<Event> get() throws Exception {
        List<Event> events = new ArrayList<>();

        final long startUrls = System.currentTimeMillis();
        final List<ValueWithContext<String>> jmxUrls = jmxUrlLocator.get();
        LOGGER.info("Get JMX URLs: " + jmxUrls.size() + " in " + (System.currentTimeMillis() - startUrls) + " msec");

        for (ValueWithContext<String> jmxUrl : jmxUrls) {
            final long startConnect = System.currentTimeMillis();
            try (JmxConnection jmxConnection = jmxConnectionFactory.connect(jmxUrl.getValue())) {
                LOGGER.info("Connect to JMX: " + jmxUrl.getValue() + " in " + (System.currentTimeMillis() - startConnect) + " msec");

                final long startExpression = System.currentTimeMillis();
                final CompositeDataSupport heapMemoryUsage = (CompositeDataSupport) jmxConnection.getAttribute(MEM_OBJECT_NAME, "HeapMemoryUsage");

                Map<String, String> context = jmxUrl.getContext();
                context.put("host", host);

                events.add(new Event(enrich(metricPrefix, context) + ".jvm.mem.heap.total", (Long) heapMemoryUsage.get("max")));
                events.add(new Event(enrich(metricPrefix, context) + ".jvm.mem.heap.used", (Long) heapMemoryUsage.get("used")));

                final CompositeDataSupport nonHeapMemoryUsage = (CompositeDataSupport) jmxConnection.getAttribute(MEM_OBJECT_NAME, "NonHeapMemoryUsage");
                events.add(new Event(enrich(metricPrefix, context) + ".jvm.mem.nonheap.total", (Long) nonHeapMemoryUsage.get("max")));
                events.add(new Event(enrich(metricPrefix, context) + ".jvm.mem.nonheap.used", (Long) nonHeapMemoryUsage.get("used")));

                final long cpuLoad = Math.round(((Double) jmxConnection.getAttribute(CPU_OBJECT_NAME, "ProcessCpuLoad")) * 100);
                events.add(new Event(enrich(metricPrefix, context) + ".jvm.cpu", cpuLoad));
                LOGGER.info("Eval events: " + events.size()
                        + " in " + (System.currentTimeMillis() - startExpression) + " msec");
            }
        }
        return events;
    }

}
