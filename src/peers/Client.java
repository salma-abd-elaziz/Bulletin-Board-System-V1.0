
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class Client {
    private final static String READER = "READER";
    private final static String WRITER = "WRITER";
    private final static String logFolderName = "logs";

    public static void main(String args[]) throws IOException {
        InetAddress address = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        String type = args[2];
        String id = args[3];
        int noOfAccesses = Integer.parseInt(args[4]);
        int readersCount = Integer.parseInt(args[5]);

        Socket s1 = null;
        String line = null;
        BufferedReader br = null;
        BufferedReader is = null;
        PrintWriter os = null;

        if(type.equals(READER)) {
            readersCount++;
        }
	
        int writerId = Integer.parseInt(id) + readersCount;
        String logFileName = type.equals(READER)? String.valueOf(id) : String.valueOf(writerId);
        PrintWriter writer = new PrintWriter(   logFolderName + "/" + "log" + logFileName + ".txt", "UTF-8");

        writer.println("Client type: " + type);
        String header = (type.equals(READER)? "rSeq\tsSeq\tOVal" : "rSeq\tsSeq");
        writer.println(header);

        for (int i = 0; i < noOfAccesses; i++) {
            try {
                s1 = new Socket(address, port); // You can use static final constant PORT_NUM
                br = new BufferedReader(new InputStreamReader(System.in));
                is = new BufferedReader(new InputStreamReader(s1.getInputStream()));
                os = new PrintWriter(s1.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                System.err.print("IO Exception");
            }

            String response = "";

            /* this is where client talks to server */
            try {
                line = type + " " + (type.equals(READER) ? id : writerId);
                os.println(line);
                os.flush();
                response = is.readLine();
                System.out.println("rSeq: " + response.split(" ")[0]);
                System.out.println("sSeq: " + response.split(" ")[1]);
                if(type.equals(READER)) {
                    System.out.println("o_val: " + response.split(" ")[2]);
                }

                writer.println(formatResponse(response));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Socket read Error");
            } finally {
                is.close();
                os.close();
                br.close();
                s1.close();
                System.out.println("Connection Closed");
            }
        }
        writer.close();
    }

    private static String formatResponse(String response) {
        String[] tokens = response.split(" ");
        StringBuilder sb = new StringBuilder();

        for(String token : tokens) {
            sb.append(token);
            sb.append("\t\t");
        }

        sb.deleteCharAt(sb.length()-1);
        return new String(sb);
    }
}
