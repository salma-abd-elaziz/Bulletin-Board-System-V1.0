package jsch;

import java.io.OutputStream;
import java.util.Properties;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHConnect {
    private JSch mSShJSch = null;
    private Session mSSHSession = null;
    private Channel mSHHChannel = null;
    private OutputStream outputStream = null;

    public boolean openConnection(String host, int port, String userName, String password, int timeout) {
        System.out.println("Start the connection!");
        boolean res = false;

        /* Init JSch obj */
        mSShJSch = new JSch();

        /* Set JSch obj properties, check no key to login */
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        this.mSShJSch.setConfig(config);

        try {
            this.mSSHSession = this.mSShJSch.getSession(userName, host, port);

            /* Set password and connect */
            this.mSSHSession.setPassword(password);
            this.mSSHSession.connect(timeout);

            /* Open the channel */
            this.mSHHChannel = this.mSSHSession.openChannel("shell");
            this.mSHHChannel.connect();

            this.mSHHChannel.setInputStream(System.in);
//            this.outputStream = this.mSHHChannel.getOutputStream();
            this.outputStream = this.mSHHChannel.getOutputStream();
            res = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public boolean sendCommand(String command) {
        boolean res = false;
        try {
            if (this.outputStream != null) {
                // Send data.
                this.outputStream.write(command.getBytes());
                this.outputStream.flush();
                res = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}