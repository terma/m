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

import com.github.terma.m.shared.CheckConfig;
import com.github.terma.m.shared.Config;
import com.github.terma.m.shared.NodeConfig;
import com.google.gson.Gson;
import com.jcraft.jsch.*;

import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;


public class NodeRunner {

    private final static Logger LOGGER = Logger.getLogger(NodeRunner.class.getName());

    public static void main(String[] args) throws FileNotFoundException {
        Config server = new Config();
        server.host = "localhost";
        server.port = 8080;

        NodeConfig nodeConfig = new NodeConfig();
        nodeConfig.host = "localhost";
        nodeConfig.checks.add(new CheckConfig("host.mem"));
        nodeConfig.checks.add(new CheckConfig("jvm", "-Dname=(SERVE.+)"));

        safeStart(server, nodeConfig);

        LOGGER.info("yspeh!");
    }

    public static void safeStart(final Config server, NodeConfig nodeConfig) {
        nodeConfig.serverHost = server.host;
        nodeConfig.serverPort = server.port;
        nodeConfig.serverContext = server.context;

        try {
            start(server, nodeConfig);
        } catch (IOException | JSchException | SftpException e) {
            throw new RuntimeException("Can't start node: " + server, e);
        }
    }

    private static String getPrivateKeyFile(final Config server) {
        return server.privateKeyFile == null ? "~/.ssh/id_rsa" : server.privateKeyFile;
    }

    private static void start(final Config server, NodeConfig nodeConfig)
            throws JSchException, SftpException, IOException {
        final JSch jsch = new JSch();

        jsch.addIdentity(getPrivateKeyFile(server));
        Session session = jsch.getSession(server.user, nodeConfig.host);
        final Properties config = new Properties();
        config.setProperty("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setConfig("PreferredAuthentications", "publickey");
        session.connect();

        try {
            LOGGER.info("Create host folder...");
            final String remoteDir = "m-node-" + nodeConfig.host;
            executeAndLastOut(session, "rm -Rf " + remoteDir + " && mkdir " + remoteDir);

            InputStream zip = NodeRunner.class.getResourceAsStream("/m-node.zip");

            LOGGER.info("Copying node data...");
            final ChannelSftp channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.put(NodeRunner.class.getResourceAsStream("/m-node.sh"), remoteDir + "/m-node.sh");
            channelSftp.put(new ByteArrayInputStream(new Gson().toJson(nodeConfig).getBytes()), remoteDir + "/config.json");
            channelSftp.put(zip, remoteDir + "/m-node.zip");
            channelSftp.chmod(500, remoteDir + "/m-node.sh");
            channelSftp.disconnect();

            LOGGER.info("Executing start node script...");
            executeAndLastOut(session, "cd " + remoteDir + " && ./m-node.sh");
        } finally {
            session.disconnect();
        }
    }

    private static String executeAndLastOut(final Session session, final String command)
            throws JSchException, IOException {
        final ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command + " 2>&1");

        BufferedReader outReader = new BufferedReader(new InputStreamReader(channelExec.getInputStream()));

        channelExec.connect();

        String line;
        String lastLine = "";
        while ((line = outReader.readLine()) != null) {
            lastLine = line;
            LOGGER.info(line);
        }
        int exitCode = channelExec.getExitStatus();
        channelExec.disconnect();

        if (exitCode != 0) {
            throw new RuntimeException("Non zero exit code: " + exitCode + " for command: " + command);
        }

        return lastLine;
    }

}
