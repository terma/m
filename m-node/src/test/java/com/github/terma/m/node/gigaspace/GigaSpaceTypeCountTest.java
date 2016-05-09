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
package com.github.terma.m.node.gigaspace;

import com.gigaspaces.document.SpaceDocument;
import com.github.terma.m.node.SystemTime;
import com.github.terma.m.shared.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openspaces.core.GigaSpace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.github.terma.m.node.gigaspace.GigaSpaceUtils.getGigaSpace;
import static com.github.terma.m.node.gigaspace.GigaSpaceUtils.registerType;
import static org.mockito.Mockito.mock;

public class GigaSpaceTypeCountTest {

    private SystemTime systemTime = mock(SystemTime.class);

    @Before
    public void init() {
        GigaSpace gigaSpace = getGigaSpace("/./aaa", null, null);
        gigaSpace.clear(null);

        registerType(gigaSpace, "typeB");

        SpaceDocument spaceDocument = new SpaceDocument("typeB");
        spaceDocument.setProperty("A", 1);
        gigaSpace.write(spaceDocument);
    }

    @Test
    public void getEvents() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("gigaSpaceUrl", "/./aaa");
            put("metric", "${typeName}");
        }};

        GigaSpaceTypeCount gigaSpaceTypeCount = new GigaSpaceTypeCount(null, params, systemTime);
        Assert.assertEquals(
                Arrays.asList(new Event("java.lang.Object", 0, 0), new Event("typeB", 0, 1)),
                gigaSpaceTypeCount.get()
        );
    }

    @Test
    public void getEventsOnlyForTypesMatchedByRegexPattern() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("gigaSpaceUrl", "/./aaa");
            put("typeNamePattern", "pe[A-Z]*");
            put("metric", "${typeName}");
        }};

        GigaSpaceTypeCount gigaSpaceTypeCount = new GigaSpaceTypeCount(null, params, systemTime);
        Assert.assertEquals(
                Arrays.asList(new Event("typeB", 0, 1)),
                gigaSpaceTypeCount.get()
        );
    }

}
