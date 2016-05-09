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
import java.util.Set;

@SuppressWarnings("WeakerAccess")
class JmxConnectionImpl implements JmxConnection {

    private final JMXConnector mConnector;
    private final MBeanServerConnection mMBSC;

    public JmxConnectionImpl(String url) throws IOException {
        final JMXServiceURL jmxServiceUrl = new JMXServiceURL(url);
        mConnector = JMXConnectorFactory.connect(jmxServiceUrl);
        mMBSC = mConnector.getMBeanServerConnection();
    }

    @Override
    public Object getAttribute(String objectName, String attribute) throws JMException, IOException {
        final Set<ObjectName> objectNames = mMBSC.queryNames(JmxUtils.createObjectName(objectName), null);
        if (objectNames.size() > 0) {
            final ObjectName objectName1 = objectNames.iterator().next();
            try {
                return mMBSC.getAttribute(objectName1, attribute);
            } catch (JMException e) {
                throw new IllegalArgumentException("Can't get attribute: " + attribute + " on " + objectName1, e);
            }
        } else {
            return null;
        }
    }

    @Override
    public void close() throws IOException {
        mConnector.close();
    }
}
