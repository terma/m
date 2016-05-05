package com.github.terma.m.node.jmx;

import java.io.IOException;

class JmxConnectionFactoryImpl implements JmxConnectionFactory {

    @Override
    public JmxConnection connect(String url) throws IOException {
        return new JmxConnectionImpl(url);
    }

}
