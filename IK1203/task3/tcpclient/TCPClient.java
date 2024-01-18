package tcpclient;
import java.net.*;
import java.time.*;
import java.time.temporal.ChronoField;
import java.io.*;

public class TCPClient {

    public boolean shutdown;             // True if client should shutdown connection
	public Integer timeout;			     // Max time to wait for data from server (null if no limit)
	public Integer limit;			     // Max no. of bytes to receive from server (null if no limit)

	
    
    /**
     * 
     * @param shutdown Will shutdown the connection in the outgoing direction after having sent the optional data to 
     * the server, it will otherwise not shut down the connection.
     * @param timeout Is the maximum time in milliseconds that the client should wait for data from the server before returning, 
     * if there is no upper limit, then timeout=null
     * @param limit Is the max amount of data in bytes that the client should recieve before returning, if there is no upper limit, then limit=null
     */
    public TCPClient(boolean shutdown, Integer timeout, Integer limit) {
        this.shutdown = shutdown;
        this.timeout = timeout;
        this.limit = limit;

    }

    /**
     * We set the timeout with setSoTimeout(timeout), if we don't get any data from the server before setSoTimeout(timeout) we get a 
     * SocketTimeoutException we need to catch that error, close the connection and return. We use socket.setReceiveBufferSize(limit) if there is 
     * a buffer size limit, and we need to set the limit before connecting it to the remote host. We use shutdown to shutdown the outgoing 
     * connection after having sent the optional data to the server using socket.shutdownOutput().
     * @param hostname
     * @param port
     * @param toServerBytes
     * @return
     * @throws IOException
     */
    public byte[] askServer(String hostname, int port, byte [] toServerBytes) throws IOException {


        /*Intialize variables and arrays */
        Socket clientSocket = new Socket(hostname, port);
        byte[] localBuffer = new byte[1];
        ByteArrayOutputStream fromServer = new ByteArrayOutputStream();
        int nextValue = 0;
        byte[] serverReply = new byte[0];

        String output = new String(toServerBytes);

        System.out.print("the Value of the string we send to the server is: ");
        System.out.println(output);

        
        try {

            /*Checks shutdown and closes the outputstream of the socket after sending the optional data */
            if (shutdown != false) {
                /*sends the user input(toServerBytes) to the server*/

                clientSocket.getOutputStream().write(toServerBytes);
                /*The we close the outgoing socket connection */
                clientSocket.shutdownOutput();
                
            }
            else{
                /*sends the user input(toServerBytes) to the server*/
                clientSocket.getOutputStream().write(toServerBytes);
            }
            /*We need to first initiate the socket, send the data and then check for timeout, if we dont get data
             * within the timeout limit we will get a an exception which we will catch wo the if statement needs 
             * to be just before the while loop
             */
            if (timeout != null) {
                clientSocket.setSoTimeout(timeout); 
            };

            
            if (limit != null) {
                while (true) {
                    
                    nextValue = clientSocket.getInputStream().read(localBuffer);
                    if (fromServer.size() >= limit || nextValue == -1) {
                        clientSocket.shutdownInput();
                        break;
                    }
                    fromServer.write(localBuffer, 0, nextValue);

                }
            }
            else {
                while (true) {
                    
                    nextValue = clientSocket.getInputStream().read(localBuffer);
                    if (nextValue == -1) {
                        break;
                    }
                    fromServer.write(localBuffer, 0, nextValue);
                }
            }
            serverReply = fromServer.toByteArray();
            clientSocket.close();
            return serverReply;
        } catch (SocketTimeoutException e) {
            serverReply = fromServer.toByteArray();
            clientSocket.close();
            return serverReply;
        }

    }
}
