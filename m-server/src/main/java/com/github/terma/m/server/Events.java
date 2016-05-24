package com.github.terma.m.server;

import com.github.terma.m.shared.Event;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface Events {

    Map<String, List<EventsImpl.Point>> get(final int parts, final long min, final long max, String pattern);

    void add(List<Event> newEvents) throws IOException;

    long min();

    void clear();

    long space();

    int events();

}
