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

import com.github.terma.m.shared.Event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EventsFiller {

    public static void main(String[] args) throws IOException {
        System.out.println("Start filling...");
        final long start = System.currentTimeMillis();
        Events events = new Events("/Users/terma/Projects/m/data");

        int count = 200000;
        long timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5);
        List<Event> buffer = new ArrayList<>();
        Random random = new Random();
        int i = 0;
        while (i < count) {
            buffer.add(new Event("host190.host.cpu", timestamp, random.nextInt(100)));
            buffer.add(new Event("host280.host.cpu", timestamp, random.nextInt(100)));
            buffer.add(new Event("host321.host.cpu", timestamp, random.nextInt(100)));
            buffer.add(new Event("host190.APP1-SERVICE1.jvm.mem.used", timestamp, 8000000 - i * 40));
            buffer.add(new Event("host280.APP1-SERVICE2.jvm.mem.used", timestamp, i * 40));
            buffer.add(new Event("host321.APP2.jvm.mem.used", timestamp, random.nextInt(8000000)));
            timestamp += 1000;
            i += 6;

            if (buffer.size() > 1000) flush(buffer, events);
        }

        flush(buffer, events);

        final long time = System.currentTimeMillis() - start;
        System.out.println("Filled " + count + " in " + time + " msec");
    }

    private static void flush(List<Event> buffer, Events events) throws IOException {
        events.add(buffer);
        buffer.clear();
    }


}
