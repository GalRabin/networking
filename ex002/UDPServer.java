/*
    from the course book:  Computer Networking: A Top Down Approach, by Kurose and Ross
 */
import java.net.*;
import java.util.Arrays;

class UDPServer
{
    public static void main(String[] args) throws Exception
    {
        byte[] receiveData = new byte[1024];
        DatagramSocket serverSocket = new DatagramSocket(9876);
        byte[] sendData;
        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            serverSocket.receive(receivePacket);

            String sentence = new String(Arrays.copyOfRange(receivePacket.getData(), 0, receivePacket.getLength()));
            System.out.println("RECEIVED: " + sentence);
            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();
            String capitalizedSentence = sentence.toUpperCase();

            sendData = capitalizedSentence.getBytes();

            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);
        }
    }
}
