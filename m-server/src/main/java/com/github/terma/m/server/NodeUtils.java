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

import com.github.terma.m.shared.Config;
import com.github.terma.m.shared.NodeConfig;

public class NodeUtils {

    public static void asyncStartNodes(Config config) {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public static void startNodes(final Config config) {
        for (NodeConfig nodeConfig : config.nodes) {
            if ("localhost".equals(nodeConfig.host)) LocalNode.start(config, nodeConfig);
            else NodeRunner.start(config, nodeConfig);
        }
    }

}
