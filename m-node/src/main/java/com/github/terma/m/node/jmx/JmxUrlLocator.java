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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.github.terma.m.node.jmx.JmxUtils.buildJmxUrl;
import static com.github.terma.m.node.jmx.JmxUtils.findJmxPorts;

@SuppressWarnings("WeakerAccess")
public abstract class JmxUrlLocator {

    public static JmxUrlLocator create(final Map<String, String> params) {
        if (params.containsKey("processPattern")) {
            return new JmxByProcess(params);
        } else if (params.containsKey("localJmx")) {
            return new LocalJmxUrl(params);
        } else if (params.containsKey("jmxHost")) {
            return new JmxUrl(params);
        } else if (params.containsKey("gigaSpaceLocators")) {
            return new GigaSpaceJmxUrlLocator(params);
        } else {
            throw new UnsupportedOperationException("Can't understand how to find JMX port! Params: " + params);
        }
    }

    public abstract List<ValueWithContext<String>> get();

    private static class JmxByProcess extends JmxUrlLocator {

        private final Sigar sigar = new Sigar();
        private final Pattern processPattern;

        public JmxByProcess(Map<String, String> params) {
            this.processPattern = Pattern.compile(params.get("processPattern"));
        }

        @Override
        public List<ValueWithContext<String>> get() {
            final List<ValueWithContext<String>> jmxUrls = new ArrayList<>();
            try {
                for (Map.Entry<String, String> appAndPort : findJmxPorts(sigar, processPattern).entrySet()) {
                    ValueWithContext<String> valueWithContext =
                            new ValueWithContext<>(buildJmxUrl("localhost", appAndPort.getValue()))
                                    .withProperty("appName", appAndPort.getKey());
                    jmxUrls.add(valueWithContext);
                }
            } catch (SigarException e) {
                throw new IllegalArgumentException("Can't get JMX ports by process pattern: " + processPattern, e);
            }
            return jmxUrls;
        }
    }

    private static class JmxUrl extends JmxUrlLocator {

        private final String jmxUrl;
        private final Map<String, String> params;

        public JmxUrl(final Map<String, String> params) {
            this.params = params;
            this.jmxUrl = buildJmxUrl(params.get("jmxHost"), params.get("jmxPort"));
        }

        @Override
        public List<ValueWithContext<String>> get() {
            return Collections.singletonList(new ValueWithContext<>(jmxUrl, params));
        }

    }

    private static class LocalJmxUrl extends JmxUrlLocator {

        private final Map<String, String> params;

        public LocalJmxUrl(final Map<String, String> params) {
            this.params = params;
        }

        @Override
        public List<ValueWithContext<String>> get() {
            return Collections.singletonList(new ValueWithContext<String>(null, params));
        }

    }

}