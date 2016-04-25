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
import java.util.List;
import java.util.logging.Logger;

/**
 * Non thread sage
 */
public class Repo {

    private static final Logger LOGGER = Logger.getLogger(Repo.class.getName());

    private final String dataPath;
    private final DataOutputStream oos;

    public Repo(String dataPath) throws IOException {
        this.dataPath = dataPath;
        oos = new DataOutputStream(new FileOutputStream(dataPath, true));
        LOGGER.info("Open repo: " + dataPath);
    }

    public void close() {
        IOUtils.closeQuietly(oos);
    }

    public List<Event> readEvents() throws IOException {
        List<Event> events = new ArrayList<Event>();
        DataInputStream ois = new DataInputStream(new FileInputStream(dataPath));
        try {
            while (true) {
                String metric = ois.readUTF();
                long timestamp = ois.readLong();
                long value = ois.readLong();
                events.add(new Event(metric, timestamp, value));
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
        LOGGER.info(events.size() + " added");
    }

    private void writeEvent(Event event) throws IOException {
        oos.writeUTF(event.metric);
        oos.writeLong(event.timestamp);
        oos.writeLong(event.value);
    }

}
