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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SumDataServlet extends HttpServlet {

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {
        final String minString = request.getParameter("min");
        final String maxString = request.getParameter("max");

        final String metric = request.getParameter("metric");

        try {
            final long min = minString != null ? Long.parseLong(minString) : EventsHolder.get().min();
            final long max = maxString != null ? Long.parseLong(maxString) : System.currentTimeMillis();

            response.getWriter().print(EventsHolder.get().sum(min, max, metric));
        } catch (EventsNotReadyException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Restoring...");
        } catch (EventsLoadException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

}
