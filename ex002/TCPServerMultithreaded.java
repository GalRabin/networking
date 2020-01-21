import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class EchoRunnable implements Runnable {
    private Socket clientSocket = null;

    EchoRunnable(Socket clientSocket) {
        this.clientSocket = clientSocket;
//        try {
//            this.clientSocket.setSoTimeout(500);
//        } catch (SocketException e) {
//        }

    }

    @Override
    public void run() {
        String clientSentence;
        String capitalizedSentence;
        try (
                BufferedReader inFromClient =
                        new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                DataOutputStream outToClient = new DataOutputStream(clientSocket.getOutputStream())
        ) {
            clientSentence = inFromClient.readLine();  //read
            // System.out.println("Received: " + clientSentence);
            if (clientSentence != null) {
                capitalizedSentence = clientSentence.toUpperCase() + '\n';
                outToClient.writeBytes(capitalizedSentence);  // write
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            try {
                this.clientSocket.close();
            } catch (IOException e) {
                System.err.println(e.getMessage());
            }
        }
    }
}

class TCPServerMultithreaded {
    public static void main(String[] argv) throws Exception {
        ServerSocket welcomeSocket = new ServerSocket(6789);  // bind + listen
        ExecutorService executor = Executors.newFixedThreadPool(20);

        while (true) {
            Socket clientSocket = welcomeSocket.accept();
            Runnable worker = new EchoRunnable(clientSocket);

            executor.execute(worker);
        }
        // executor.shutdown();
    }
}