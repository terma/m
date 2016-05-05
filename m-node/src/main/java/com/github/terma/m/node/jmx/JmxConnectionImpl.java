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
