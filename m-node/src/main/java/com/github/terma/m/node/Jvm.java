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

import com.github.terma.m.shared.Event;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
class Jvm extends HostAwareChecker {

    private final Sigar sigar = new Sigar();
    private final Pattern processPattern;
    private final Pattern jmxPortPattern = Pattern.compile("-Dcom.sun.management.jmxremote.port=(\\d+)");

    public Jvm(final String host, final String processPattern) {
        super(host);
        this.processPattern = Pattern.compile(processPattern);
    }

    private Map<String, String> findJmxPorts() throws SigarException {
        Map<String, String> jmxPorts = new HashMap<String, String>();
        for (long pid : sigar.getProcList()) {
            String app = null;

            String[] args;
            try {
                args = sigar.getProcArgs(pid);
            } catch (SigarException e) {
                // nothing we don't have access to that PID
                continue;
            }

            for (String arg : args) {
                Matcher matcher = processPattern.matcher(arg);
                if (matcher.find()) {
                    app = matcher.group(1);
                    break;
                }
            }

            if (app != null) {
                for (String arg : args) {
                    Matcher matcher = jmxPortPattern.matcher(arg);
                    if (matcher.find()) {
                        jmxPorts.put(app, matcher.group(1));
                        break;
                    }
                }
            }
        }

        return jmxPorts;
    }

    public List<Event> get() throws Exception {
        List<Event> events = new ArrayList<Event>();

        for (Map.Entry<String, String> appAndJmxPort : findJmxPorts().entrySet()) {
            JMXServiceURL url = new JMXServiceURL("rmi", "", 0, "/jndi/rmi://" + host + ":" + appAndJmxPort.getValue() + "/jmxrmi");
            try (JMXConnector mConnector = JMXConnectorFactory.connect(url)) {
                MBeanServerConnection mMBSC = mConnector.getMBeanServerConnection();
                final ObjectName memObjectName = new ObjectName("java.lang:type=Memory");

                final CompositeDataSupport heapMemoryUsage = (CompositeDataSupport) mMBSC.getAttribute(memObjectName, "HeapMemoryUsage");
                events.add(new Event(host + "." + appAndJmxPort.getKey() + ".jvm.mem.heap.total", (Long) heapMemoryUsage.get("max")));
                events.add(new Event(host + "." + appAndJmxPort.getKey() + ".jvm.mem.heap.used", (Long) heapMemoryUsage.get("used")));

                final CompositeDataSupport nonHeapMemoryUsage = (CompositeDataSupport) mMBSC.getAttribute(memObjectName, "NonHeapMemoryUsage");
                events.add(new Event(host + "." + appAndJmxPort.getKey() + ".jvm.mem.nonheap.total", (Long) nonHeapMemoryUsage.get("max")));
                events.add(new Event(host + "." + appAndJmxPort.getKey() + ".jvm.mem.nonheap.used", (Long) nonHeapMemoryUsage.get("used")));

                final ObjectName cpuObjectName = new ObjectName("java.lang:type=OperatingSystem");
                final long cpuLoad = Math.round(((Double) mMBSC.getAttribute(cpuObjectName, "ProcessCpuLoad")) * 100);
                events.add(new Event(host + "." + appAndJmxPort.getKey() + ".jvm.cpu", cpuLoad));
            }
        }
        return events;
    }

}
