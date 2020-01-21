/*
    from the course book:  Computer Networking: A Top Down Approach, by Kurose and Ross
 */
package ex002;

import java.io.*;
import java.net.*;

class TCPClient
{
    public static void main(String[] argv) throws Exception
    {
        String sentence;
        String modifiedSentence;

        BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));

        Socket clientSocket = new Socket("localhost", 6789); // connect

        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        sentence = inFromUser.readLine();
        outToServer.writeBytes(sentence + '\n'); // write
        modifiedSentence = inFromServer.readLine(); // read
        System.out.println("FROM SERVER: " + modifiedSentence);

        clientSocket.close(); // close
    }
}