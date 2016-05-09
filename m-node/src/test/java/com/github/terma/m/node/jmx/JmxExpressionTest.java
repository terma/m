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

import org.junit.Assert;
import org.junit.Test;

import javax.management.JMException;
import java.io.IOException;

import static org.mockito.Mockito.*;

public class JmxExpressionTest {

    private JmxConnection jmxConnection = mock(JmxConnection.class);

    @Test
    public void evaluateLongAttribute() throws IOException, JMException {
        JmxExpression jmxExpression1 = JmxExpression.parse("package:type=Bean.attribute1");
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(0L);
        Assert.assertEquals(new Long(0), jmxExpression1.evaluate(jmxConnection));

        JmxExpression jmxExpression2 = JmxExpression.parse("package:type=Bean.attribute2");
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(Long.MAX_VALUE);
        Assert.assertEquals(Long.valueOf(Long.MAX_VALUE), jmxExpression2.evaluate(jmxConnection));
    }

    @Test
    public void evaluateIntegerAttribute() throws IOException, JMException {
        JmxExpression jmxExpression = JmxExpression.parse("package:type=Bean.attribute1");
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(12);
        Assert.assertEquals(new Long(12), jmxExpression.evaluate(jmxConnection));
    }

    @Test
    public void evaluateDivideExpression() throws IOException, JMException {
        JmxExpression jmxExpression = JmxExpression.parse("package:type=Bean.a/package:type=Bean.b");
        when(jmxConnection.getAttribute(anyString(), eq("a"))).thenReturn(100);
        when(jmxConnection.getAttribute(anyString(), eq("b"))).thenReturn(20);
        Assert.assertEquals(new Long(5), jmxExpression.evaluate(jmxConnection));
    }

    @Test
    public void evaluateDivideExpressionToNullIfDividerZero() throws IOException, JMException {
        JmxExpression jmxExpression = JmxExpression.parse("package:type=Bean.a/package:type=Bean.b");
        when(jmxConnection.getAttribute(anyString(), eq("a"))).thenReturn(100);
        when(jmxConnection.getAttribute(anyString(), eq("b"))).thenReturn(0);
        Assert.assertNull(jmxExpression.evaluate(jmxConnection));
    }

    @Test
    public void evaluateDivideExpressionToNullIfDividerOneOperandIsNull() throws IOException, JMException {
        JmxExpression jmxExpression = JmxExpression.parse("package:type=Bean.a/package:type=Bean.b");
        when(jmxConnection.getAttribute(anyString(), eq("a"))).thenReturn(null);
        when(jmxConnection.getAttribute(anyString(), eq("b"))).thenReturn(5);
        Assert.assertNull(jmxExpression.evaluate(jmxConnection));

        when(jmxConnection.getAttribute(anyString(), eq("a"))).thenReturn(100);
        when(jmxConnection.getAttribute(anyString(), eq("b"))).thenReturn(null);
        Assert.assertNull(jmxExpression.evaluate(jmxConnection));
    }

    @Test
    public void evaluateAttributeWithNullValueToNull() throws IOException, JMException {
        JmxExpression jmxExpression1 = JmxExpression.parse("package:type=Bean.attribute1");
        Assert.assertNull(jmxExpression1.evaluate(jmxConnection));
    }

}
