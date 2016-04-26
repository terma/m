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
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String startString = request.getParameter("start");
        final String endString = request.getParameter("end");

        final List<Event> eventsToSend = new ArrayList<>();
        if (startString == null) {
            // nothing
        } else {
            long end = Long.MAX_VALUE;
            if (endString != null) end = Long.parseLong(endString);

            final long start = Long.parseLong(startString);
            for (Event event : Events.get()) {
                if (event.timestamp > start && event.timestamp <= end) eventsToSend.add(event);
            }
        }
        response.getWriter().write(new Gson().toJson(eventsToSend));
    }

}
