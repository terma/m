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

import com.github.terma.fastselect.FastSelect;
import com.github.terma.m.shared.Event;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Non thread sage
 */
@SuppressWarnings("WeakerAccess")
public class Repo {

    private static final Logger LOGGER = Logger.getLogger(Repo.class.getName());

    private static final String EVENT_CODES_FILE_NAME = "event-codes.bin";
    private static final String EVENTS_FILE_NAME = "events.bin";

    private final String dataPath;
    private final File eventsFile;
    private final File eventCodesFile;

    public Repo(String dataPath) throws IOException {
        this.dataPath = dataPath;
        eventsFile = new File(dataPath, EVENTS_FILE_NAME);
        eventCodesFile = new File(dataPath, EVENT_CODES_FILE_NAME);
    }

    private static void batchAdd(FastSelect<Event> fastSelect, List<Event> events) {
        fastSelect.addAll(events);
        events.clear();
    }

    public void readEvents(FastSelect<Event> fastSelect) throws IOException {
        LOGGER.info("Restoring events from " + eventsFile + "...");
        final long start = System.currentTimeMillis();
        List<Event> events = new ArrayList<>();

        try (DataInputStream dis = new DataInputStream(new FileInputStream(new File(dataPath, EVENTS_FILE_NAME)))) {
            try {
                //noinspection InfiniteLoopStatement
                while (true) {
                    events.add(new Event(dis.readShort(), dis.readLong(), dis.readLong()));
                    if (events.size() > 1000) batchAdd(fastSelect, events);
                }
            } catch (EOFException e) {
                // just end
            }
        } catch (FileNotFoundException e) {
            // nothing, just no data
        }
        batchAdd(fastSelect, events);

        LOGGER.info(fastSelect.size() + " restored in " + (System.currentTimeMillis() - start) + " msec");
    }

    public void addEvents(final List<Event> events) throws IOException {
        try (DataOutputStream oos = new DataOutputStream(new FileOutputStream(eventsFile, true))) {
            for (Event event : events) {
                oos.writeShort(event.metricCode);
                oos.writeLong(event.timestamp);
                oos.writeLong(event.value);
            }
        }
    }

    public void storeMetricCodes(final Map<String, Short> metricCodes) throws IOException {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(eventCodesFile));
            for (Map.Entry<String, Short> metricCode : metricCodes.entrySet()) {
                dos.writeUTF(metricCode.getKey());
                dos.writeShort(metricCode.getValue());
            }
        } finally {
            IOUtils.closeQuietly(dos);
        }
    }

    public Map<String, Short> readMetricCodes() throws IOException {
        Map<String, Short> result = new HashMap<>();
        DataInputStream ois = null;
        try {
            ois = new DataInputStream(new FileInputStream(eventCodesFile));
            //noinspection InfiniteLoopStatement
            while (true) {
                result.put(ois.readUTF(), ois.readShort());
            }
        } catch (EOFException | FileNotFoundException e) {
            // just end
        } finally {
            IOUtils.closeQuietly(ois);
        }
        return result;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void clear() throws IOException {
        eventsFile.delete();
        eventCodesFile.delete();
    }

    public long space() {
        return eventCodesFile.length() + eventsFile.length();
    }

}
