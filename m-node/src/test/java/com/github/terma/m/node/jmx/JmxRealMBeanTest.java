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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;

public class JmxRealMBeanTest {

    @org.junit.Test
    public void getEventForEachExpression() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.1.metric", "");
            put("expression.1.query", "domain:type=a*");
            put("expression.1.expression", "Value");
            put("localJmx", "true");
        }};

        Jmx jmx = new Jmx(null, params);

        MBeanServer server = ManagementFactory.getPlatformMBeanServer();

        for (int i = 0; i < 5; i++) {
            final ObjectName objectName = new ObjectName("domain:type=araba" + i);
            TestMBean mbean = new Test();
            server.registerMBean(mbean, objectName);
        }

        Assert.assertEquals(5, jmx.get().size());
    }

}
