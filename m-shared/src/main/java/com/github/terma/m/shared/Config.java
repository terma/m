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
import org.apache.commons.io.IOUtils;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Config {

    private static final String CONFIG_PATH_SYSTEM_PROPERTY = "m.config.path";
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String FILE_PREFIX = "file:";
    private static final String IGNORE_PREFIX = "ignore:";

    public String host;
    public int port;
    public String dataPath;
    public String user;
    public String privateKeyFile;
    public List<NodeConfig> nodes = new ArrayList<>();

    public static Config readConfig() {
        final String configPath = System.getProperty(CONFIG_PATH_SYSTEM_PROPERTY);
        if (configPath == null)
            throw new IllegalArgumentException("No config property -D" + CONFIG_PATH_SYSTEM_PROPERTY);

        if (configPath.startsWith(CLASSPATH_PREFIX)) {
            InputStream inputStream = Config.class.getResourceAsStream(configPath.substring(CLASSPATH_PREFIX.length()));
            if (inputStream == null) throw new IllegalArgumentException(
                    "Can't find config in classpath by: " + configPath);

            try {
                return new Gson().fromJson(IOUtils.toString(inputStream), Config.class);
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid path to config: " + configPath
                        + " set by property -D" + CONFIG_PATH_SYSTEM_PROPERTY + "!", e);
            }
        } else if (configPath.startsWith(FILE_PREFIX)) {
            try {
                return new Gson().fromJson(new FileReader(configPath.substring(FILE_PREFIX.length())), Config.class);
            } catch (IOException e) {
                throw new IllegalArgumentException("Invalid path to config: " + configPath
                        + " set by property -D" + CONFIG_PATH_SYSTEM_PROPERTY + "!", e);
            }
        } else if (configPath.startsWith(IGNORE_PREFIX)) {
            // skip for test and other
            return new Config();
        } else {
            throw new UnsupportedOperationException("Unsupported type of config path: " + configPath);
        }
    }
}
