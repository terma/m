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
import com.github.terma.m.shared.Event;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Thread safe
 */
class Events {

    private static final CopyOnWriteArrayList<Event> EVENTS = new CopyOnWriteArrayList<>();
    private static final Repo REPO;

    static {
        try {
            REPO = new Repo(Config.readConfig().dataPath);
            EVENTS.addAll(REPO.readEvents());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Event> get() {
        return EVENTS;
    }

    public static synchronized void add(List<Event> newEvents) throws IOException {
        REPO.storeEvents(newEvents);
        EVENTS.addAll(newEvents);
    }

}
