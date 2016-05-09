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
package com.github.terma.m.server;

import com.github.terma.fastselect.AbstractRequest;
import com.github.terma.fastselect.FastSelect;
import com.github.terma.m.shared.Event;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RepoTest {

    private FastSelect<Event> fastSelect = Events.createFastSelect();

    @Test
    public void storeRestoreEvents() throws IOException {
        Path d = Files.createTempDirectory("aaa");
        Repo repo = new Repo(d.toString());

        repo.addEvents(Collections.singletonList(new Event((short) 1, 12, 988)));

        repo.readEvents(fastSelect);

        Assert.assertEquals(
                Collections.singletonList(new Event((short) 1, 12, 988)),
                fastSelect.select(new AbstractRequest[0]));
    }

    @Test
    public void storeRestoreMetricCodes() throws IOException {
        Path d = Files.createTempDirectory("aaa");
        Repo repo = new Repo(d.toString());

        Map<String, Short> metricCodes = new HashMap<>();
        metricCodes.put("aa.b", (short) 12);
        metricCodes.put("aa.ddd", Short.MAX_VALUE);

        repo.storeMetricCodes(metricCodes);

        Assert.assertEquals(metricCodes, repo.readMetricCodes());
    }

    @Test
    public void restoreEmptyMetricCodesIfNoFile() throws IOException {
        Path d = Files.createTempDirectory("aaa");
        Repo repo = new Repo(d.toString());

        Assert.assertEquals(Collections.EMPTY_MAP, repo.readMetricCodes());
    }

}
