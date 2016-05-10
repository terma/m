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
package com.github.terma.m.node.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;

public class JmxConnectionFactoryImpl implements JmxConnectionFactory {

    @Override
    public JmxConnection connect(String url) throws IOException {
        if (url == null) return new JmxConnectionImpl(ManagementFactory.getPlatformMBeanServer());
        else return new JmxConnectionImpl(url);
    }

    @Override
    public JmxConnection connect(String host, String port) throws IOException {
        return new JmxConnectionImpl(JmxUtils.buildJmxUrl(host, port));
    }

}
