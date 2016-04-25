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

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private static final String CONFIG_PATH_SYSTEM_PROPERTY = "m.config.path";

    public String host;
    public int port;
    public String dataPath;
    public String user;
    public String privateKeyFile;
    public List<NodeConfig> nodes = new ArrayList<>();

    public static Config readConfig() {
        final String configPath = System.getProperty(CONFIG_PATH_SYSTEM_PROPERTY);
        try {
            return new Gson().fromJson(FileUtils.readFileToString(new File(configPath)), Config.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid path to config: " + configPath
                    + " set by property -D" + CONFIG_PATH_SYSTEM_PROPERTY + "!", e);
        }
    }
}
