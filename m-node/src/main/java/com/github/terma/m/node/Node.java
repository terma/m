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

import static java.util.Collections.singletonList;

public class Node {

    private static final long INTERVAL = 5000;

    private static Sigar sigar = new Sigar();

    private static void send(String serverHost, int serverPort, List<Event> events) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL("http", serverHost, serverPort, "/node").openConnection();
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

        startCheck(nodeConfig.serverHost, nodeConfig.serverPort, checkers);
    }

    private static void startCheck(String serverHost, int serverPort, List<Checker> checkers) {
        while (true) {
            try {
                List<Event> events = new ArrayList<>();
                for (Checker checker : checkers) {
                    events.addAll(checker.get());
                }
                send(serverHost, serverPort, events);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(INTERVAL);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private static List<Checker> buildCheckers(NodeConfig nodeConfig) {
        List<Checker> checkers = new ArrayList<>();
        for (CheckConfig checkConfig : nodeConfig.checks) {
            if (checkConfig.name.equals("host.mem")) {
                checkers.add(new HostMem());
            } else if (checkConfig.name.equals("host.cpu")) {
                checkers.add(new HostCpu());
            } else if (checkConfig.name.equals("host.net")) {
                checkers.add(new HostNet());
            } else if (checkConfig.name.equals("jvm")) {
                checkers.add(new Jvm(checkConfig.processPattern));
            }
        }
        return checkers;
    }

    interface Checker {
        List<Event> get() throws Exception;
    }

    static class HostMem implements Checker {
        public List<Event> get() throws Exception {
            return Arrays.asList(
                    new Event("host.mem.used", sigar.getMem().getUsed()),
                    new Event("host.mem.total", sigar.getMem().getTotal()),
                    new Event("host.swap.used", sigar.getSwap().getUsed())
            );
        }
    }

    static class HostNet implements Checker {
        public List<Event> get() throws Exception {
            long total = 0;
            for (String intr : sigar.getNetInterfaceList()) {
                NetInterfaceStat stat = sigar.getNetInterfaceStat(intr);
                total += stat.getRxBytes() + stat.getTxBytes();
            }
            return singletonList(new Event("host.net.total", total));
        }
    }

    static class HostCpu implements Checker {
        public List<Event> get() throws Exception {
            return singletonList(new Event("host.cpu", Math.round(sigar.getCpuPerc().getCombined() * 100)));
        }
    }

}
