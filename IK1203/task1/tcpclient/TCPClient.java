package tcpclient;
import java.net.*;
import java.io.*;

public class TCPClient {
    
    public TCPClient() {
    }

    /**
     * The client needs to first allocate the buffers
     * @param hostname is the hostname of the server
     * @param port is the port of the server that we are connecting to
     * @param toServerBytes Is the user input that we want to forward to the server
     * @return 
     * @throws IOException
     */
    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {
        /* Establish a link to the server using the supplied hostname and port*/
        Socket clientSocket = new Socket(hostname, port); 
        /*sends the user input(toServerBytes) to the server*/
        clientSocket.getOutputStream().write(toServerBytes); 

        /*Create a byte array of 1 byte, intiate the ByteArrayOutputStream, i used a while loop that is always true until so that we get an endless
        * loop. we output the value of the clientsocket 24 byte at a time which is then written to the ByteArrayOutputStream fromServer. If we reach
        * the Value -1 we break out of the loop. Write the data from fromServer ByteArrayOutputStream to a byte array that we return.
        */
        byte[] localBuffer = new byte[24];
        ByteArrayOutputStream fromServer = new ByteArrayOutputStream();
        int nextValue = 0;
        while (true){
            nextValue = clientSocket.getInputStream().read(localBuffer);
            if (nextValue == -1) {
                break;
            }
            fromServer.write(localBuffer, 0, nextValue);
        }
        byte serverReply[] = fromServer.toByteArray();
        clientSocket.close();
        return serverReply;
    }

    public byte[] askServer(String hostname, int port) throws IOException {
        /* Establish a link to the server using the supplied hostname and port*/
        Socket clientSocket = new Socket(hostname, port); 

        /*Create a byte array of 1 byte, intiate the ByteArrayOutputStream, i used a while loop that is always true until so that we get an endless
        * loop. we output the value of the clientsocket 24 byte at a time which is then written to the ByteArrayOutputStream fromServer. If we reach
        * the Value -1 we break out of the loop. Write the data from fromServer ByteArrayOutputStream to a byte array that we return.
        */
        byte[] localBuffer = new byte[24];
        ByteArrayOutputStream fromServer = new ByteArrayOutputStream();
        int nextValue = 0;
        while (true){
            nextValue = clientSocket.getInputStream().read(localBuffer);
            if (nextValue == -1) {
                break;
            }
            fromServer.write(localBuffer, 0, nextValue);
        }
        byte serverReply[] = fromServer.toByteArray();
        clientSocket.close();
        return serverReply;
    }
}
