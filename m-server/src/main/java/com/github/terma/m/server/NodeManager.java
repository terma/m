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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("WeakerAccess")
public class NodeManager {

    public static NodeManager INSTANCE = new NodeManager();

    private final Config config;
    private final Lock lock = new ReentrantLock();
    private final Map<String, State> states = new HashMap<>();
    private final AtomicBoolean startInProgress = new AtomicBoolean(false);

    private NodeManager() {
        config = Config.readConfig();
        lock.lock();
        try {
            for (NodeConfig nodeConfig : config.nodes) {
                states.put(nodeConfig.host, new State(Status.STARTING));
            }
        } finally {
            lock.unlock();
        }
        startLiveChecker();
    }

    private void startNodes() {
        if (startInProgress.compareAndSet(false, true)) {
            updateStatuses(Status.STARTING);

            try {
                for (NodeConfig nodeConfig : config.nodes) {
                    try {
                        if ("localhost".equals(nodeConfig.host)) LocalNode.start(config, nodeConfig);
                        else NodeRunner.start(config, nodeConfig);
                        updateStatus(nodeConfig.host, Status.LIVE);
                    } catch (RuntimeException e) {
                        updateStatus(nodeConfig.host, Status.FAILED);
                        // log
                        System.err.println(e);
                        e.printStackTrace();
                    }
                }
            } finally {
                startInProgress.set(false);
            }
        } else {
            System.out.println("Starting already in progress. Skip double start.");
        }
    }

    public void responseFromNode(String nodeHost) {
        updateStatus(nodeHost, Status.LIVE);
    }

    public void asyncStartNodes() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startNodes();
            }
        }, "NODE-STARTER");
        thread.setDaemon(true);
        thread.start();
    }

    private void checkLive() {
        lock.lock();
        try {
            final long livePeriod = TimeUnit.SECONDS.toMillis(config.secToRefresh * 2);

            for (State state : states.values()) {
                if (state.status != Status.STARTING && System.currentTimeMillis() - state.getTimestamp() > livePeriod)
                    state.setStatus(Status.FAILED);
            }
        } finally {
            lock.unlock();
        }
    }

    private void startLiveChecker() {
        final Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                checkLive();
            }
        }, "NODE-LIVE-CHECKER");
        thread.setDaemon(true);
        thread.start();
    }

    private void updateStatuses(Status status) {
        lock.lock();
        try {
            states.clear();
            for (NodeConfig nodeConfig : config.nodes) {
                states.put(nodeConfig.host, new State(status));
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateStatus(String host, Status status) {
        lock.lock();
        try {
            State state = states.get(host);
            if (state == null) {
                state = new State(status);
                states.put(host, state);
            } else {
                state.setStatus(status);
            }
        } finally {
            lock.unlock();
        }
    }

    public Map<String, State> getStatus() {
        lock.lock();
        try {
            return new HashMap<>(states);
        } finally {
            lock.unlock();
        }
    }

    public enum Status {
        STARTING,
        FAILED,
        LIVE
    }

    public static class State {

        private Status status;
        private long timestamp;

        public State(Status newStatus) {
            setStatus(newStatus);
        }

        public long getTimestamp() {
            return timestamp;
        }

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status newStatus) {
            status = newStatus;
            timestamp = System.currentTimeMillis();
        }
    }

}
