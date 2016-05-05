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

import com.github.terma.m.shared.CheckConfig;
import com.github.terma.m.shared.Event;
import com.github.terma.m.shared.NodeConfig;
import com.google.gson.Gson;
import org.apache.commons.io.IOUtils;
import org.hyperic.sigar.NetInterfaceStat;
import org.hyperic.sigar.Sigar;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;

public class Node {

    private static final Sigar sigar = new Sigar();

    private static void send(String serverHost, int serverPort, String context, List<Event> events) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http", serverHost, serverPort, context + "/node").openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/json");
        connection.setRequestProperty("charset", "utf-8");
        connection.setUseCaches(false);
        connection.setInstanceFollowRedirects(false);
        connection.connect();
        OutputStream outputStream = connection.getOutputStream();
        outputStream.write(new Gson().toJson(events).getBytes());
        connection.getInputStream().read();
        outputStream.close();
    }

    public static void main(String[] args) throws IOException {
        String configJson = IOUtils.toString(Node.class.getResourceAsStream("/config.json"));
        NodeConfig nodeConfig = new Gson().fromJson(configJson, NodeConfig.class);
        System.out.println("Use config: " + configJson);

        final List<Checker> checkers = buildCheckers(nodeConfig);

        startCheck(nodeConfig, checkers);
    }

    private static void startCheck(NodeConfig nodeConfig, List<Checker> checkers) {
        while (true) {
            try {
                List<Event> events = new ArrayList<>();
                for (Checker checker : checkers) {
                    events.addAll(checker.get());
                }
                send(nodeConfig.serverHost, nodeConfig.serverPort, nodeConfig.serverContext, events);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(nodeConfig.secToRefresh));
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static List<Checker> buildCheckers(NodeConfig nodeConfig) {
        List<Checker> checkers = new ArrayList<>();
        for (CheckConfig checkConfig : nodeConfig.checks) {
            if (checkConfig.name.equals("host.mem")) {
                checkers.add(new HostMem(nodeConfig.host));
            } else if (checkConfig.name.equals("host.cpu")) {
                checkers.add(new HostCpu(nodeConfig.host));
            } else if (checkConfig.name.equals("host.net")) {
                checkers.add(new HostNet(nodeConfig.host));
            } else if (checkConfig.name.equals("jvm")) {
                checkers.add(new Jvm(nodeConfig.host, null));
            }
        }
        return checkers;
    }

    static class HostMem extends HostAwareChecker {

        HostMem(String host) {
            super(host);
        }

        public List<Event> get() throws Exception {
            return Arrays.asList(
                    new Event(host + ".host.mem.used", sigar.getMem().getUsed()),
                    new Event(host + ".host.mem.total", sigar.getMem().getTotal()),
                    new Event(host + ".host.mem.swap.used", sigar.getSwap().getUsed())
            );
        }
    }

    static class HostNet extends HostAwareChecker {
        HostNet(String host) {
            super(host);
        }

        public List<Event> get() throws Exception {
            long tx = 0;
            long rx = 0;
            for (String intr : sigar.getNetInterfaceList()) {
                NetInterfaceStat stat = sigar.getNetInterfaceStat(intr);
                tx += stat.getTxBytes();
                rx += stat.getRxBytes();
            }
            return Arrays.asList(
                    new Event(host + ".host.net.rx", rx),
                    new Event(host + ".host.net.tx", tx)
            );
        }
    }

    static class HostCpu extends HostAwareChecker {
        HostCpu(String host) {
            super(host);
        }

        public List<Event> get() throws Exception {
            return singletonList(new Event(host + ".host.cpu", Math.round(sigar.getCpuPerc().getCombined() * 100)));
        }
    }

}
