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
package com.github.terma.m.shared;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    @Test
    public void supportLoadConfigFromClasspath() {
        System.setProperty("m.config.path", "classpath:/config-test-min.json");
        Config config = Config.readConfig();
        Assert.assertEquals(8080, config.port);
    }

    @Test
    public void supportLoadConfigMin() {
        System.setProperty("m.config.path", "classpath:/config-test-min.json");
        Config config = Config.readConfig();
        Assert.assertEquals(8080, config.port);
    }

    @Test
    public void supportLoadConfig() {
        System.setProperty("m.config.path", "classpath:/config-test.json");
        Config config = Config.readConfig();
        Assert.assertEquals(8080, config.port);
        Assert.assertEquals(2, config.nodes.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfCantLoadConfig() {
        System.setProperty("m.config.path", "classpath:/config-not-existent.json");
        Config.readConfig();
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfNoConfigProperty() {
        System.getProperties().remove("m.config.path");
        Config.readConfig();
    }

}
