import jsch.SSHConnect;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Start {

    private static String serverIp;
    private static int serverPort;
    private static String[] readersIPs;
    private static int[] readersIDs;
    private static String[] writersIPs;
    private static int[] writersIDs;
    private static int noOfAccesses;
    private static final String PATH_TO_FILE = "system.properties";

    private static final String SERVER_USER_NAME = "";
    private static final String SERVER_PASSWORD = "";
    
    private static final String CLIENT_USER_NAME = "";
    private static final String CLIENT_PASSWORD = "";

    private static int readersCount = 0;

    public static void main(String[] args) {
        parseFile();

        createServer(new SSHConnect());
        createClient(new SSHConnect());

        System.out.println(serverIp);
        System.out.println(serverPort);

        for (String readerIp : readersIPs) {
            System.out.println(readerIp);
        }

        for (String writerIp : writersIPs) {
            System.out.println(writerIp);
        }

        System.out.println(noOfAccesses);
    }

    private static void parseFile() {

        /* access the file */
        File file = new File(PATH_TO_FILE);
        Scanner input = null;
        try {
            input = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }

        /* get the server ip */
        if (input.hasNext()) {
            String nextLine = input.nextLine();
            serverIp = nextLine.split("=")[1];
            serverIp = serverIp.charAt(0) == ' ' ? serverIp.substring(0) : serverIp;
        }

        /* get the server port */
        if (input.hasNext()) {
            String nextLine = input.nextLine();
            String serverPortAsString = nextLine.split("=")[1];
            serverPortAsString = serverPortAsString.charAt(0) == ' ' ? serverPortAsString.substring(0)
                    : serverPortAsString;
            serverPort = Integer.parseInt(serverPortAsString);
        }

        /* get readers IPs */
        if (input.hasNext()) {
            String nextLine = input.nextLine();
            int numOfReaders = Integer.parseInt(nextLine.split("=")[1].substring(0));
            readersIPs = new String[numOfReaders];
            readersIDs = new int[numOfReaders];

            for (int i = 0; i < numOfReaders; i++) {
                if (input.hasNext()) {
                    String line = input.nextLine();
                    readersIPs[i] = line.split("=")[1];
                    String firstHalf = line.split("=")[0];
                    readersIDs[i] = firstHalf.charAt(firstHalf.length() - 1) - '0' + 1;
                }
            }
        }

        /* get writers IPs */
        if (input.hasNext()) {
            String nextLine = input.nextLine();
            int numOfWriters = Integer.parseInt(nextLine.split("=")[1]);
            writersIPs = new String[numOfWriters];
            writersIDs = new int[numOfWriters];

            for (int i = 0; i < numOfWriters; i++) {
                if (input.hasNext()) {
                    String line = input.nextLine();
                    writersIPs[i] = line.split("=")[1];
                    String firstHalf = line.split("=")[0];
                    writersIDs[i] = firstHalf.charAt(firstHalf.length() - 1) - '0' + 1;
                }
            }
        }

        /* get the noOfAccesses */
        if (input.hasNext()) {
            String nextLine = input.nextLine();
            String secondHalf = nextLine.split("=")[1];
            noOfAccesses = Integer.parseInt(secondHalf.charAt(0) == ' ' ? secondHalf.substring(0) : secondHalf);
        }

        input.close();
    }

    private static void createServer(SSHConnect demo) {
        // use username and password of the lab machines.
        if (demo.openConnection(serverIp, 22, SERVER_USER_NAME, SERVER_PASSWORD, 120000)) {
            try {
                demo.sendCommand("cd \n");
                Thread.sleep(300);

                demo.sendCommand("javac Server.java \n");
                Thread.sleep(300);

                int noOfRequests = (readersIDs.length + writersIDs.length) * noOfAccesses;

                demo.sendCommand("java Server " + serverPort + " " + noOfRequests + " \n");
                Thread.sleep(300);

                System.out.println("after rcv");
                // demo.closeConnection();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Error");
        }
    }

    private static void createClient(SSHConnect demo) {
        try {
            for (int i = 0; i < readersIDs.length; i++) {
                // use for ip**, username and password for the lab machines.
                if (demo.openConnection(readersIPs[i], 22, CLIENT_USER_NAME, CLIENT_PASSWORD, 120000)) {
                    Thread.sleep(300);

                    demo.sendCommand("javac Client.java \n");
                    Thread.sleep(300);

                    demo.sendCommand("java Client " + serverIp + " " + serverPort + " READER " + readersIDs[i] + " "
                            + noOfAccesses + " " + readersIDs.length + "\n");
                    Thread.sleep(300);

                    System.out.println("reader " + readersIDs[i] + " has finished");
                }
            }

            for (int i = 0; i < writersIDs.length; i++) {
                // ip of the machines.
                if (demo.openConnection(writersIPs[i], 22, CLIENT_USER_NAME, CLIENT_PASSWORD, 120000)) {
                    Thread.sleep(300);

                    demo.sendCommand("javac Client.java \n");
                    Thread.sleep(300);

                    demo.sendCommand("java Client " + serverIp + " " + serverPort + " WRITER " + writersIDs[i] + " "
                            + noOfAccesses + " " + writersIDs.length + "\n");
                    Thread.sleep(300);

                    System.out.println("writer " + writersIDs[i] + " has finished");
                }
            }
            // demo.closeConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
