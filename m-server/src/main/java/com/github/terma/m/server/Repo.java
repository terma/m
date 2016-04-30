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
public class Repo {

    private static final Logger LOGGER = Logger.getLogger(Repo.class.getName());

    private static final String EVENT_CODE_FILE_NAME = "event-codes.bin";
    private static final String EVENTS_FILE_NAME = "events.bin";

    private final String dataPath;
    private final DataOutputStream oos;

    public Repo(String dataPath) throws IOException {
        this.dataPath = dataPath;
        File file = new File(dataPath, EVENTS_FILE_NAME);
        oos = new DataOutputStream(new FileOutputStream(file, true));
        LOGGER.info("Open repo: " + file);
    }

    public void close() {
        IOUtils.closeQuietly(oos);
    }

    public List<Event> readEvents() throws IOException {
        List<Event> events = new ArrayList<Event>();
        DataInputStream ois = new DataInputStream(new FileInputStream(new File(dataPath, EVENTS_FILE_NAME)));
        try {
            while (true) {
                events.add(new Event(ois.readShort(), ois.readLong(), ois.readLong()));
            }
        } catch (EOFException e) {
            // just end
        } catch (FileNotFoundException e) {
            // nothing
        } finally {
            IOUtils.closeQuietly(ois);
        }
        LOGGER.info(events.size() + " restored");
        return events;
    }

    public void storeEvents(List<Event> events) throws IOException {
        for (Event event : events) writeEvent(event);
        oos.flush();
//        LOGGER.info(events.size() + " added");
    }

    private void writeEvent(Event event) throws IOException {
        oos.writeShort(event.metricCode);
        oos.writeLong(event.timestamp);
        oos.writeLong(event.value);
    }

    public void storeMetricCodes(Map<String, Short> metricCodes) throws IOException {
        DataOutputStream dos = null;
        try {
            dos = new DataOutputStream(new FileOutputStream(new File(dataPath, EVENT_CODE_FILE_NAME)));
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
            ois = new DataInputStream(new FileInputStream(new File(dataPath, EVENT_CODE_FILE_NAME)));
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

}
