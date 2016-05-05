package com.github.terma.m.node.jmx;

import javax.management.JMException;
import java.io.Closeable;
import java.io.IOException;

public interface JmxConnection extends Closeable {

    Object getAttribute(String objectName, String attribute) throws IOException, JMException;

}
