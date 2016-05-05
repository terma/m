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

import com.github.terma.m.node.SystemTime;
import com.github.terma.m.shared.Event;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class JmxTest {

    private SystemTime systemTime = mock(SystemTime.class);
    private JmxConnection jmxConnection = mock(JmxConnection.class);
    private JmxConnectionFactory jmxConnectionFactory = mock(JmxConnectionFactory.class);

    @Test
    public void getEventForEachExpression() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.eventName1", "bean.moma");
            put("expression.eventName2", "bean.superBean");
            put("jmxHost", "host");
            put("jmxPort", "12");
        }};

        when(jmxConnectionFactory.connect(anyString())).thenReturn(jmxConnection);
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(12).thenReturn(23);

        Jmx jmx = new Jmx(null, params, jmxConnectionFactory, systemTime);

        Assert.assertEquals(
                Arrays.asList(new Event("eventName1", 0, 12), new Event("eventName2", 0, 23)),
                jmx.get()
        );

        verify(jmxConnection).getAttribute("bean", "moma");
        verify(jmxConnection).getAttribute("bean", "superBean");
    }

    @Test
    public void getEventAsMBeanAttributeValueWhenJmxHostPortSpecified() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.eventName", "bean.attribute");
            put("jmxHost", "host");
            put("jmxPort", "12");
        }};

        when(jmxConnectionFactory.connect(anyString())).thenReturn(jmxConnection);
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(12);

        Jmx jmx = new Jmx(null, params, jmxConnectionFactory, systemTime);

        Assert.assertEquals(
                Collections.singletonList(new Event("eventName", 0, 12)),
                jmx.get()
        );
    }

    @Test
    public void getEventWithEnrichedMetric() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.${jmxHost}.eventName", "bean.attribute");
            put("jmxHost", "host");
            put("jmxPort", "12");
        }};

        when(jmxConnectionFactory.connect(anyString())).thenReturn(jmxConnection);
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(12);

        Jmx jmx = new Jmx(null, params, jmxConnectionFactory, systemTime);

        Assert.assertEquals(
                Collections.singletonList(new Event("host.eventName", 0, 12)),
                jmx.get()
        );
    }

    @Test
    public void getEventWithComplexExpression() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.eventName", "bean.a/bean1.m");
            put("jmxHost", "host");
            put("jmxPort", "12");
        }};

        when(jmxConnectionFactory.connect(anyString())).thenReturn(jmxConnection);
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(50).thenReturn(200);

        Jmx jmx = new Jmx(null, params, jmxConnectionFactory, systemTime);

        Assert.assertEquals(
                Collections.singletonList(new Event("eventName", 0, 4)),
                jmx.get()
        );
    }

    @Ignore
    @Test
    public void getEventByConnectingToGigaSpace() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.eventName", "bean.a");
            put("gigaSpaceUrl", "host");
        }};

        when(jmxConnectionFactory.connect(anyString())).thenReturn(jmxConnection);
        when(jmxConnection.getAttribute(anyString(), anyString())).thenReturn(50);

        Jmx jmx = new Jmx(null, params, jmxConnectionFactory, systemTime);

        Assert.assertEquals(
                Collections.singletonList(new Event("eventName", 0, 4)),
                jmx.get()
        );
    }

    @Test
    public void getNoEventsIfJmxHostPortSpecifiedButCantConnect() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.eventName", "bean.attribute");
            put("jmxHost", "host");
            put("jmxPort", "12");
        }};

        when(jmxConnectionFactory.connect(anyString())).thenReturn(jmxConnection);

        Jmx jmx = new Jmx(null, params, jmxConnectionFactory, systemTime);

        Assert.assertEquals(Collections.emptyList(), jmx.get());
    }

}
