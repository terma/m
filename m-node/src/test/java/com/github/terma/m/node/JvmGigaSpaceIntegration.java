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

import java.util.HashMap;
import java.util.Map;

/**
 * We can't create test for {@link com.github.terma.m.node.jmx.GigaSpaceJmxUrlLocator} as it depends on
 * {@link org.hyperic.sigar.Sigar} which needs to have
 * native libraries in special path. Plus it needs dedicated {@link org.openspaces.core.GigaSpace}
 */
public class JvmGigaSpaceIntegration {

    public static void main(String[] args) throws Exception {
        System.setProperty("java.library.path", "/Users/terma/Downloads/sigar-1.6.4-native");

        Map<String, String> params = new HashMap<String, String>() {{
            put("metricPrefix", "${host}.${containerType}${containerId}");
            put("gigaSpaceLocators", "127.0.0.1:4700");
        }};

        IntegrationUtils.runChecker(new Jvm("h", params));
    }


}
