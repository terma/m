package com.github.terma.m.node.jmx;

import java.io.IOException;

interface JmxConnectionFactory {

    JmxConnection connect(String value) throws IOException;

}
