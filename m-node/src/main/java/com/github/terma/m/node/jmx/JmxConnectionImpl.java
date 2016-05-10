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

import javax.management.JMException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
class JmxConnectionImpl implements JmxConnection {

    private final JMXConnector jmxConnector;
    private final MBeanServerConnection mBeanServerConnection;

    public JmxConnectionImpl(String url) throws IOException {
        final JMXServiceURL jmxServiceUrl = new JMXServiceURL(url);
        jmxConnector = JMXConnectorFactory.connect(jmxServiceUrl);
        mBeanServerConnection = jmxConnector.getMBeanServerConnection();
    }

    public JmxConnectionImpl(JMXConnector jmxConnector) throws IOException {
        this.jmxConnector = jmxConnector;
        mBeanServerConnection = this.jmxConnector.getMBeanServerConnection();
    }

    public JmxConnectionImpl(MBeanServerConnection mBeanServerConnection) throws IOException {
        this.jmxConnector = null;
        this.mBeanServerConnection = mBeanServerConnection;
    }

    @Override
    public List<String> findObjectNames(String query) throws IOException {
        final Set<ObjectName> objectNames = mBeanServerConnection.queryNames(JmxUtils.createObjectName(query), null);
        final List<String> result = new ArrayList<>();
        for (ObjectName objectName : objectNames) {
            result.add(objectName.toString());
        }
        return result;
    }

    @Override
    public Object getAttribute(String objectName, String attribute) throws JMException, IOException {
        try {
            return mBeanServerConnection.getAttribute(new ObjectName(objectName), attribute);
        } catch (JMException e) {
            throw new IllegalArgumentException("Can't get attribute: " + attribute + " on " + objectName, e);
        }
    }

    @Override
    public void close() throws IOException {
        if (jmxConnector != null) jmxConnector.close();
    }
}
