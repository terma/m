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

import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class DataServlet extends HttpServlet {

    private static final int PARTS = 100;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String minString = request.getParameter("min");
        final String maxString = request.getParameter("max");

        final long min = minString != null ? Long.parseLong(minString) : EventsFactory.get().min();
        final long max = maxString != null ? Long.parseLong(maxString) : System.currentTimeMillis();

        final String metric = request.getParameter("metric");
        final String callback = request.getParameter("callback");

        final Map<String, List<Events.Point>> events = EventsFactory.get().get(PARTS, min, max, metric);

        System.out.println("[" + new Date(min) + " : " + new Date(max) + "] events " + events.size());

        response.getWriter().write(callback + "(" + new Gson().toJson(events) + ");");
    }

}
