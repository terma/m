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

import javax.management.JMException;
import java.io.IOException;

/**
 * type of expressions:
 * getter, divide operation
 */
abstract class JmxExpression {

    @SuppressWarnings("WeakerAccess")
    public static JmxExpression parse(final String expression) {
        final int dividePosition = expression.indexOf('/');
        if (dividePosition < 0) return new JmxGetter(expression);
        else return new JmxDivide(
                new JmxGetter(expression.substring(0, dividePosition)),
                new JmxGetter(expression.substring(dividePosition + 1)));
    }

    public abstract Long evaluate(JmxConnection jmxConnection) throws JMException, IOException;

    private static class JmxGetter extends JmxExpression {

        private final String expression;

        JmxGetter(String expression) {
            this.expression = expression;
        }

        @Override
        public Long evaluate(JmxConnection jmxConnection) throws JMException, IOException {
            int attributeStart = expression.lastIndexOf('.');
            if (attributeStart < 0) throw new IllegalArgumentException("Invalid expression format: " + expression
                    + " should be beanPath.attribute!");
            String path = expression.substring(0, attributeStart);
            String attribute = expression.substring(attributeStart + 1);
            Object value = jmxConnection.getAttribute(path, attribute);

            if (value == null) return null;
            else if (value instanceof Integer) return ((Integer) value).longValue();
            return (long) value;
        }

    }

    private static class JmxDivide extends JmxExpression {

        private final JmxGetter value;
        private final JmxGetter divider;

        JmxDivide(JmxGetter value, JmxGetter divider) {
            this.value = value;
            this.divider = divider;
        }

        @Override
        public Long evaluate(JmxConnection jmxConnection) throws JMException, IOException {
            final Long dividerValue = divider.evaluate(jmxConnection);
            if (dividerValue == null || dividerValue == 0) return null;
            else {
                final Long v = value.evaluate(jmxConnection);
                if (v == null) return null;
                else return v / dividerValue;
            }
        }
    }


}

