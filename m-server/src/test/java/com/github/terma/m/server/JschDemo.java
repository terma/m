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

import com.github.terma.m.shared.Config;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.util.Properties;

import static com.github.terma.m.server.JschUtils.execute;


public class JschDemo {

    public static void main(String[] args) throws IOException, JSchException {
        Config server = new Config();
        server.host = "localhost";
        server.port = 8080;

        final JSch jsch = new JSch();

        jsch.addIdentity("~/.ssh/id_rsa");
        Session session = jsch.getSession(null, "localhost");
        final Properties config = new Properties();
        config.setProperty("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setConfig("PreferredAuthentications", "publickey");
        session.connect();

        try {
            execute(session, "ls");
            execute(session, "ls");
            execute(session, "ls");
            execute(session, "ls");
        } finally {
            session.disconnect();
        }
    }

}
