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

import com.github.terma.m.shared.Event;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * We can't create test for {@link GigaSpaceJmxUrlLocator} as it depends on
 * {@link org.hyperic.sigar.Sigar} which needs to have
 * native libraries in special path. Plus it needs dedicated {@link org.openspaces.core.GigaSpace}
 */
public class JmxGigaSpaceIntegration {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.library.path", "/Users/terma/Downloads/sigar-1.6.4-native");

        Map<String, String> params = new HashMap<String, String>() {{
            put("expression.test", "*:type=GSC,name=GSC,*.ServiceCount");
            put("gigaSpaceLocators", "127.0.0.1:4700");
        }};

        Jmx jmx = new Jmx(null, params);

        List<Event> events = jmx.get();

        Assert.assertTrue(events.size() > 0);
        System.out.println(events);
    }


}
