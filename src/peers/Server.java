
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {

    protected static List<List<Integer>> readersLog;
    protected static List<List<Integer>> writersLog;

    protected static int R_NUM = 0;
    protected static int S_SEQ = 0;
    protected static int O_VAL = -1;
    private static int R_SEQ = 0;
    private AtomicInteger mOval = new AtomicInteger(-1);
    
    private static final String logFolderName = "logs";

    public static void main(String args[]) {
        int port = Integer.parseInt(args[0]);
        int totalNumberOfAccesses = Integer.parseInt(args[1]);

        readersLog = new ArrayList<List<Integer>>();
        writersLog = new ArrayList<List<Integer>>();

        Socket s = null;
        ServerSocket ss2 = null;
        System.out.println("Server Listening......");
        try {
            ss2 = new ServerSocket(port); // can also use static final PORT_NUM , when defined
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Server error");
        }

        while (totalNumberOfAccesses > 0) {
            try {
                totalNumberOfAccesses--;
                s = ss2.accept();
                R_SEQ++;
                ServerThread st = new ServerThread(s, R_SEQ);
                st.start();
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Connection Error");
            }
        }

        /* Make sure the main thread closes the last thread */
        while (Thread.activeCount() > 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        printServerLog();
    }

    private static void printServerLog() {

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(logFolderName + "/" + "server_log.txt", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        /* Print readers records */
        writer.println("Readers:");
        writer.println("--------");

        String readersHeader = "sSeq\toVal\trID\t\trNum";
        writer.println(readersHeader);

        StringBuilder recordBody;
        for (int i = 0; i < readersLog.size(); i++) {
            recordBody = new StringBuilder();
            for (int token : readersLog.get(i)) {
                recordBody.append(token);
                recordBody.append("\t\t");
            }

            recordBody.deleteCharAt(recordBody.length() - 1);
            writer.println(new String(recordBody));
        }

        writer.println();

        /* Print writers records */
        writer.println("Writers:");
        writer.println("--------");

        String writersHeader = "sSeq\toVal\twID";
        writer.println(writersHeader);

        for (int i = 0; i < writersLog.size(); i++) {
            recordBody = new StringBuilder();
            for (int token : writersLog.get(i)) {
                recordBody.append(token);
                recordBody.append("\t\t");
            }
            recordBody.deleteCharAt(recordBody.length() - 1);
            writer.println(new String(recordBody));
        }
        /* Close writer */
        writer.close();
    }
}

class ServerThread extends Thread {

    String line = null;
    BufferedReader is = null;
    PrintWriter os = null;
    Socket s = null;

    int rSeq;

    final boolean READER = true;
    final boolean WRITER = false;
    Random rand;

    public ServerThread(Socket s, int rSeq) {
        this.s = s;
        this.rSeq = rSeq;
        rand = new Random();
    }

    public void run() {
        try {
            is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            os = new PrintWriter(s.getOutputStream());
        } catch (IOException e) {
            System.out.println("IO error in server thread");
        }
        try {
            line = is.readLine();
            boolean type = getType(line);
            int id = getClientId(line);

            /* A reader has entered the system, increase the rNum */
            if (type == READER) {
                Server.R_NUM++;
            }

            /* Sleep to give the illusion of a real system */
            int randomTime = rand.nextInt(10000);
            System.out.println(randomTime);
            Thread.sleep(randomTime);

            /* Operate if writer */
            if (type == WRITER) {
                Server.O_VAL = id;
            }

            /* When thread is about to finish */
            Server.S_SEQ++;
            String response = buildResponse(type, this.rSeq, Server.S_SEQ, Server.O_VAL);

            /* Write this thread record in its list */
            addToLogs(type, Server.S_SEQ, Server.O_VAL, id, Server.R_NUM);

            /* Respond to the client */
            os.println(response);
            os.flush();

            /* When a reader client is about to finish, decrement the rNum variable */
            if (type == READER) {
                Server.R_NUM--;
            }
        } catch (Exception e) {
            line = this.getName(); //reused String line for getting thread name
            System.out.println("IO Error/ Client " + line + " terminated abruptly");
        } finally {
            try {
                System.out.println("Connection Closing..");
                if (is != null) {
                    is.close();
                    System.out.println("Socket Input Stream Closed");
                }
                if (os != null) {
                    os.close();
                    System.out.println("Socket Out Closed");
                }
                if (s != null) {
                    s.close();
                    System.out.println("Socket Closed");
                }
            } catch (IOException ie) {
                System.out.println("Socket Close Error");
            }
        }
    }

    private void addToLogs(boolean type, int sSeq, int oVal, int id, int rNum) {
        ArrayList<Integer> record = new ArrayList<Integer>();
        record.add(sSeq);
        record.add(oVal);
        record.add(id);

        if (type == READER) {
            record.add(rNum);
            Server.readersLog.add(record);
        } else {
            Server.writersLog.add(record);
        }
    }

    private String buildResponse(boolean type, int rSeq, int sSeq, int O_VAL) {
        String response = rSeq + " " + sSeq;
        return type == WRITER ? response : response + " " + String.valueOf(O_VAL);
    }

    private int getClientId(String line) {
        return Integer.parseInt(line.split(" ")[1]);
    }

    private boolean getType(String line) {
        String type = line.split(" ")[0];
        return (type.equals("READER"));
    }
}
