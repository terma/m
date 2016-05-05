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

import com.github.terma.m.shared.Event;
import org.junit.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JvmProcessIntegration {

    public static void main(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("processPattern", "marker=(JVM_STABI\\w+)");
            put("metricPrefix", "${host}.${appName}");
        }};

        Checker checker = new Jvm("localhost", params);
        List<Event> events = checker.get();

        Assert.assertTrue(events.size() > 0);
        System.out.println(events);
    }

}
