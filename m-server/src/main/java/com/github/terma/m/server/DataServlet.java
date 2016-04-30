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
import java.util.List;
import java.util.Map;

public class DataServlet extends HttpServlet {

    private static final int PARTS = 50;

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final long min = Long.parseLong(request.getParameter("min"));
        final String maxString = request.getParameter("max");
        final String pattern = request.getParameter("pattern");

        long max = System.currentTimeMillis();
        if (maxString != null) max = Long.parseLong(maxString);

        final Map<String, List<Events.Point>> events = EventsFactory.get().get(PARTS, min, max, pattern);

        response.getWriter().write(new Gson().toJson(events));
    }

}
