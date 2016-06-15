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
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@Ignore
public class DataServletTest {

    private HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    private HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    private PrintWriter writer = Mockito.mock(PrintWriter.class);

    @Before
    public void init() {
        System.setProperty("m.config.path", "ignore:");
//        EventsFactory.get().clear();
    }

    @Test
    public void whenNoPeriodSpecifiedProvideNothing() throws ServletException, IOException {
        Mockito.when(response.getWriter()).thenReturn(writer);

        new DataServlet().doGet(request, response);

        Mockito.verify(writer).write("[]");
    }

    @Test
    public void whenOnlyStartSpecifiedProvideFromToFuture() throws ServletException, IOException {
        EventsHolder.get().add(Arrays.asList(new Event("a", 4, 0), new Event("a1", 5, 0), new Event("b", 6, 0), new Event("c", Long.MAX_VALUE, 0)));

        Mockito.when(request.getParameter("start")).thenReturn("5");
        Mockito.when(response.getWriter()).thenReturn(writer);

        new DataServlet().doGet(request, response);

        Mockito.verify(writer).write("[{\"metric\":\"b\",\"timestamp\":6,\"value\":0},{\"metric\":\"c\",\"timestamp\":9223372036854775807,\"value\":0}]");
    }

    @Test
    public void whenStartAndEndSpecifiedProvideInRange() throws ServletException, IOException {
        EventsHolder.get().add(Arrays.asList(new Event("a", 4, 0), new Event("b", 5, 0), new Event("c", 6, 0), new Event("d", 7, 0)));

        Mockito.when(request.getParameter("start")).thenReturn("5");
        Mockito.when(request.getParameter("end")).thenReturn("6");
        Mockito.when(response.getWriter()).thenReturn(writer);

        new DataServlet().doGet(request, response);

        Mockito.verify(writer).write("[{\"metric\":\"c\",\"timestamp\":6,\"value\":0}]");
    }

}
