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

import com.github.terma.m.node.HostAwareChecker;
import com.github.terma.m.node.StringUtils;
import com.github.terma.m.node.SystemTime;
import com.github.terma.m.shared.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Find java processes with enabled JMX on host by provided pattern. Connect and evaluate expression
 * by taken values from MBeans
 */
class Jmx extends HostAwareChecker {

    private static final Logger LOGGER = Logger.getLogger(Jmx.class.getName());

    private static final String EXPRESSION_PREFIX = "expression.";

    private final Map<String, JmxExpression> expressions;
    private final JmxUrlLocator jmxUrlLocator;
    private final JmxConnectionFactory jmxConnectionFactory;
    private final SystemTime systemTime;

    public Jmx(final String host, final Map<String, String> params) {
        this(host, params, new JmxConnectionFactoryImpl(), new SystemTime());
    }

    public Jmx(final String host, final Map<String, String> params, JmxConnectionFactory jmxConnectionFactory, SystemTime systemTime) {
        super(host);

        expressions = new HashMap<>();

        if (params != null) {

            for (Map.Entry<String, String> param : params.entrySet()) {
                if (param.getKey().startsWith(EXPRESSION_PREFIX)) {
                    expressions.put(param.getKey().replace(EXPRESSION_PREFIX, ""), JmxExpression.parse(param.getValue()));
                }
            }
        }

        this.jmxUrlLocator = JmxUrlLocator.create(params);
        this.jmxConnectionFactory = jmxConnectionFactory;
        this.systemTime = systemTime;
    }

    @Override
    public List<Event> get() throws Exception {
        List<Event> events = new ArrayList<>();

        for (final ValueWithContext<String> jmxUrl : jmxUrlLocator.get()) {
            LOGGER.info("Getting data from " + jmxUrl.getValue() + "...");
            try (final JmxConnection jmxConnection = jmxConnectionFactory.connect(jmxUrl.getValue())) {
                for (Map.Entry<String, JmxExpression> expression : expressions.entrySet()) {
                    Long value = expression.getValue().evaluate(jmxConnection);
                    if (value != null) {
                        String metric = StringUtils.enrich(expression.getKey(), jmxUrl.getContext());
                        events.add(new Event(metric, systemTime.getMillis(), value));
                    }
                }
            }
        }
        return events;
    }

}

