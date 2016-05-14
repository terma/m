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

import javax.management.JMException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Find java processes with enabled JMX on host by provided pattern. Connect and evaluate expression
 * by taken values from MBeans
 * <p>
 * Example of configuration in virtual JSON:
 * <pre>
 * expressions: [
 *   {
 *     metric: "metricPatternCouldHasPlaceholders",
 *     query: "mBeanQueryCouldReturnMultiple", // object name will could be used for placeholders
 *     expression: "simpleAttributeNameOrOperations"
 *   }
 * ]
 * </pre>
 * Configuration by properties as currently supported:
 * <pre>
 * jmxPort=12,
 * expression.1.metric="metricPatternCouldHasPlaceholders",
 * expression.1.query="mBeanQueryCouldReturnMultiple",
 * expression.1.expression="simpleAttributeNameOrOperations",
 * </pre>
 */
public class Jmx extends HostAwareChecker {

    private static final Logger LOGGER = Logger.getLogger(Jmx.class.getName());

    private static final String EXPRESSION_PREFIX = "expression.";
    private static final Pattern EXPRESSION_ID_PATTERN = Pattern.compile("expression.(\\d+).metric");

    private final JmxUrlLocator jmxUrlLocator;
    private final JmxConnectionFactory jmxConnectionFactory;
    private final SystemTime systemTime;
    private final List<ExpressionConfig> expressionConfigs;

    public Jmx(final String host, final Map<String, String> params) {
        this(host, params, new JmxConnectionFactoryImpl(), new SystemTime());
    }

    public Jmx(final String host, final Map<String, String> params, JmxConnectionFactory jmxConnectionFactory, SystemTime systemTime) {
        super(host);

        expressionConfigs = new ArrayList<>();

        if (params != null) {
            for (Map.Entry<String, String> param : params.entrySet()) {
                final Matcher matcher = EXPRESSION_ID_PATTERN.matcher(param.getKey());
                if (matcher.find()) {
                    String id = matcher.group(1);

                    ExpressionConfig expressionConfig = new ExpressionConfig();
                    expressionConfig.metric = params.get(EXPRESSION_PREFIX + id + ".metric");
                    expressionConfig.query = params.get(EXPRESSION_PREFIX + id + ".query");
                    expressionConfig.expression = JmxExpression.parse(params.get(EXPRESSION_PREFIX + id + ".expression"));
                    expressionConfigs.add(expressionConfig);
                }
            }
        }

        this.jmxUrlLocator = JmxUrlLocator.create(params);
        this.jmxConnectionFactory = jmxConnectionFactory;
        this.systemTime = systemTime;
    }

    @Override
    public List<Event> get() throws Exception {
        final List<Event> events = new ArrayList<>();

        final long startUrls = System.currentTimeMillis();
        final List<ValueWithContext<String>> jmxUrls = jmxUrlLocator.get();
        LOGGER.info("Get JMX URLs: " + jmxUrls.size() + " in " + (System.currentTimeMillis() - startUrls) + " msec");

        for (final ValueWithContext<String> jmxUrl : jmxUrls) getForUrl(jmxUrl, events);
        return events;
    }

    private void getForUrl(ValueWithContext<String> jmxUrl, List<Event> events) throws IOException, JMException {
        final long startConnect = System.currentTimeMillis();
        try (final JmxConnection jmxConnection = jmxConnectionFactory.connect(jmxUrl.getValue())) {
            LOGGER.info("Connect to JMX: " + jmxUrl.getValue() + " in " + (System.currentTimeMillis() - startConnect) + " msec");

            final long startExpression = System.currentTimeMillis();
            final Map<String, String> context = jmxUrl.getContext();
            for (ExpressionConfig expressionConfig : expressionConfigs) {
                List<String> objectNames = jmxConnection.findObjectNames(expressionConfig.query);

                for (String objectName : objectNames) {
                    Long value = expressionConfig.expression.evaluate(jmxConnection, objectName);
                    if (value != null) {
                        context.put("objectName", objectName);

                        String metric = StringUtils.enrich(expressionConfig.metric, context);
                        events.add(new Event(metric, systemTime.getMillis(), value));
                    }
                }
            }
            LOGGER.info("Eval " + expressionConfigs.size() + " expressions, events: " + events.size()
                    + " in " + (System.currentTimeMillis() - startExpression) + " msec");
        }
    }

    private static class ExpressionConfig {
        String metric;
        String query;
        JmxExpression expression;
    }

}

