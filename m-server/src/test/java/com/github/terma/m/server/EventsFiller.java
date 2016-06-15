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
        EventsImpl events = new EventsImpl("/Users/terma/Projects/m/data");

        final int count = 10000000;
        long timestamp = System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(30);
        List<Event> buffer = new ArrayList<>();
        Random random = new Random();

        int bufferSize = 10000;
        System.out.println("one . is " + bufferSize);

        int i = 0;
        while (i < count) {
            List<Event> cycle = new ArrayList<>();
            cycle.add(new Event("host190.host.cpu", timestamp, random.nextInt(100)));
            cycle.add(new Event("host280.host.cpu", timestamp, random.nextInt(100)));
            cycle.add(new Event("host321.host.cpu", timestamp, random.nextInt(100)));
            cycle.add(new Event("host190.APP1-SERVICE1.jvm.mem.used", timestamp, 8000000 - i * 40));
            cycle.add(new Event("host280.APP1-SERVICE2.jvm.mem.used", timestamp, i * 40));
            cycle.add(new Event("host321.APP2.jvm.mem.used", timestamp, random.nextInt(8000000)));

            for (int c = 0; c < 40; c++) {
                cycle.add(new Event("custom.event" + c, timestamp, random.nextInt(100)));
            }

            buffer.addAll(cycle);
            i += cycle.size();
            timestamp += 1000;

            if (buffer.size() > bufferSize) {
                flush(buffer, events);
                System.out.print('.');
            }
        }

        flush(buffer, events);

        final long time = System.currentTimeMillis() - start;
        System.out.println();
        System.out.println("Filled " + count + " in " + time + " msec");
    }

    private static void flush(List<Event> buffer, EventsImpl events) throws IOException {
        events.add(buffer);
        buffer.clear();
    }


}
