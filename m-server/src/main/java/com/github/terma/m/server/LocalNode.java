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

import com.github.terma.m.node.Node;
import com.github.terma.m.shared.Config;
import com.github.terma.m.shared.NodeConfig;


public class LocalNode {

    public static void start(final Config server, final NodeConfig nodeConfig) {
        nodeConfig.serverHost = server.host;
        nodeConfig.serverPort = server.port;
        nodeConfig.serverContext = server.context;
        nodeConfig.secToRefresh = server.secToRefresh;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Node.run(nodeConfig);
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

}
