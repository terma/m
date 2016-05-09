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
import org.junit.Before;
import org.junit.Test;
import org.openspaces.core.GigaSpace;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GigaSpaceTypeChangeTest {

    private SystemTime systemTime = mock(SystemTime.class);
    private GigaSpace gigaSpace;

    @Before
    public void init() {
        gigaSpace = GigaSpaceUtils.getGigaSpace("/./aaa", null, null);
        gigaSpace.clear(null);

        GigaSpaceUtils.registerType(gigaSpace, "typeB");

        addSpaceDocuments(gigaSpace);
    }

    private void addSpaceDocuments(GigaSpace gigaSpace) {
        for (int i = 0; i < 100; i++) {
            SpaceDocument spaceDocument = new SpaceDocument("typeB");
            spaceDocument.setProperty("A", String.valueOf(System.currentTimeMillis()) + String.valueOf(i));
            gigaSpace.write(spaceDocument);
        }
    }

    @Test
    public void getZeroWhenCallFirstTime() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("gigaSpaceUrl", "/./aaa");
            put("metric", "${typeName}");
        }};

        GigaSpaceTypeChange gigaSpaceTypeCount = new GigaSpaceTypeChange(null, params, systemTime);
        assertEquals(
                Arrays.asList(new Event("java.lang.Object", 0, 0), new Event("typeB", 0, 0)),
                gigaSpaceTypeCount.get()
        );
    }

    @Test
    public void getCountPerSec() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("gigaSpaceUrl", "/./aaa");
            put("typeNamePattern", "typeB");
            put("metric", "${typeName}");
        }};

        GigaSpaceTypeChange gigaSpaceTypeCount = new GigaSpaceTypeChange(null, params, systemTime);
        assertEquals(singletonList(new Event("typeB", 0, 0)), gigaSpaceTypeCount.get());

        // after 5 sec
        when(systemTime.getMillis()).thenReturn(5000L);

        // first changes
        assertEquals(singletonList(new Event("typeB", 5000, 20)), gigaSpaceTypeCount.get());

        // after 10 sec
        when(systemTime.getMillis()).thenReturn(10000L);

        // no changes
        assertEquals(singletonList(new Event("typeB", 10000, 0)), gigaSpaceTypeCount.get());

        // after 15 sec
        when(systemTime.getMillis()).thenReturn(15000L);

        addSpaceDocuments(gigaSpace);
        // a few more
        assertEquals(singletonList(new Event("typeB", 15000, 20)), gigaSpaceTypeCount.get());

        // after 20 sec
        when(systemTime.getMillis()).thenReturn(20000L);

        gigaSpace.clear(null);
        // a few less
        assertEquals(singletonList(new Event("typeB", 20000, -40)), gigaSpaceTypeCount.get());
    }

    @Test
    public void getEventsOnlyForTypesMatchedByRegexPattern() throws Exception {
        Map<String, String> params = new HashMap<String, String>() {{
            put("gigaSpaceUrl", "/./aaa");
            put("typeNamePattern", "pe[A-Z]*");
            put("metric", "${typeName}");
        }};

        GigaSpaceTypeChange gigaSpaceTypeCount = new GigaSpaceTypeChange(null, params, systemTime);
        assertEquals(singletonList(new Event("typeB", 0, 0)), gigaSpaceTypeCount.get());
    }

}
