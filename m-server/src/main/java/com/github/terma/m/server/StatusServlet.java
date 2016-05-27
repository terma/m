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
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class StatusServlet extends HttpServlet {

    private final Gson gson = new Gson();

    @Override
    protected void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        Map<String, NodeManager.State> states = NodeManager.INSTANCE.getStatus();

        JsonObject result = new JsonObject();
        result.add("nodes", gson.toJsonTree(states));

        JsonObject events = new JsonObject();
        result.add("events", events);

        try {
            events.addProperty("space", EventsHolder.get().space());
            events.addProperty("count", EventsHolder.get().events());
        } catch (EventsNotReadyException e) {
            events.addProperty("error", "restoring-in-progress");
        } catch (EventsLoadException e) {
            events.addProperty("error", "restoring-failed");
        }

        response.getWriter().write(gson.toJson(result));
    }

}
