package com.github.terma.m.server;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

class JschUtils {

    @SuppressWarnings("WeakerAccess")
    public static void execute(final Session session, final String command)
            throws JSchException, IOException {
        final ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command + " 2>&1");
        InputStream inputStream = channelExec.getInputStream();
        try {
            channelExec.connect();
            final String output = IOUtils.toString(inputStream);
            final int exitCode = channelExec.getExitStatus();

            if (exitCode > 0) throw new IllegalArgumentException(
                    "Non zero exit code: " + exitCode + " for command: " + command + "\nOutput: " + output);
        } finally {
            channelExec.disconnect();
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static int executeAndExitCode(final Session session, final String command)
            throws JSchException, IOException {
        final ChannelExec channelExec = (ChannelExec) session.openChannel("exec");
        channelExec.setCommand(command + " 2>&1");

        InputStream inputStream = channelExec.getInputStream();
        try {
            channelExec.connect();
            // ignore result, just clean up output buffer
            //noinspection ResultOfMethodCallIgnored
            String o = IOUtils.toString(inputStream);
            System.out.println(o);
            return channelExec.getExitStatus();
        } finally {
            channelExec.disconnect();
        }
    }
}
