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
package com.github.terma.m.node.gigaspace;

import com.github.terma.m.node.HostAwareChecker;
import com.github.terma.m.node.StringUtils;
import com.github.terma.m.node.SystemTime;
import com.github.terma.m.shared.Event;
import com.j_spaces.core.admin.JSpaceAdminProxy;
import com.j_spaces.core.admin.SpaceRuntimeInfo;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.GigaSpaceConfigurer;
import org.openspaces.core.space.UrlSpaceConfigurer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

abstract class GigaSpaceType extends HostAwareChecker {

    private static final String METRIC_PARAM = "metric";
    private static final String GIGASPACE_URL_PARAM = "gigaSpaceUrl";
    private static final String GIGASPACE_USER_PARAM = "gigaSpaceUser";
    private static final String GIGASPACE_PASSWORD_PARAM = "gigaSpacePassword";
    private static final String TYPE_NAME_PATTERN = "typeNamePattern";

    protected final SystemTime systemTime;

    private final GigaSpace gigaSpace;
    private final String metric;
    private final Pattern typeNamePattern;

    public GigaSpaceType(String host, Map<String, String> params) {
        this(host, params, new SystemTime());
    }

    public GigaSpaceType(String host, Map<String, String> params, SystemTime systemTime) {
        super(host);

        this.systemTime = systemTime;
        this.metric = params.get(METRIC_PARAM);
        this.typeNamePattern = params.containsKey(TYPE_NAME_PATTERN) ?
                Pattern.compile(params.get(TYPE_NAME_PATTERN)) : null;

        UrlSpaceConfigurer urlSpaceConfigurer = new UrlSpaceConfigurer(params.get(GIGASPACE_URL_PARAM));
        if (params.containsKey(GIGASPACE_USER_PARAM)) {
            urlSpaceConfigurer.userDetails(params.get(GIGASPACE_USER_PARAM), params.get(GIGASPACE_PASSWORD_PARAM));
        }
        this.gigaSpace = new GigaSpaceConfigurer(urlSpaceConfigurer.create()).create();
    }

    @Override
    public List<Event> get() throws Exception {
        final JSpaceAdminProxy admin = (JSpaceAdminProxy) gigaSpace.getSpace().getAdmin();
        final SpaceRuntimeInfo runtimeInfo = admin.getRuntimeInfo();

        List<Event> events = new ArrayList<>();
        final HashMap<String, String> context = new HashMap<>();
        for (int i = 0; i < runtimeInfo.m_ClassNames.size(); i++) {
            final String typeName = runtimeInfo.m_ClassNames.get(i);
            if (typeNamePattern != null && !typeNamePattern.matcher(typeName).find()) continue;

            final long value = calculateValue(typeName, runtimeInfo.m_NumOFEntries.get(i));

            context.put("typeName", typeName);
            events.add(new Event(StringUtils.enrich(metric, context), systemTime.getMillis(), value));
        }
        return events;
    }

    protected abstract long calculateValue(String typeName, long value);

}
