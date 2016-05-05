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
package com.github.terma.m.node.jmx;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JmxUtils {

    private static final Pattern JMX_PORT_PATTERN = Pattern.compile("-Dcom.sun.management.jmxremote.port=(\\d+)");

    private JmxUtils() {
        throw new UnsupportedOperationException("Utility class not for instantiation.");
    }

    public static ObjectName createObjectName(final String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Can't create ObjectName for " + name, e);
        }
    }

    public static Map<String, String> findJmxPorts(
            final Sigar sigar, final Pattern processPattern) throws SigarException {
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
                    Matcher matcher = JMX_PORT_PATTERN.matcher(arg);
                    if (matcher.find()) {
                        jmxPorts.put(app, matcher.group(1));
                        break;
                    }
                }
            }
        }

        return jmxPorts;
    }

    public static String buildJmxUrl(String host, String port) {
//        return "/jndi/rmi://" + host + ":" + port + "/jmxrmi";
        return "service:jmx:rmi///jndi/rmi://" + host + ":" + port + "/jmxrmi";
    }

}
