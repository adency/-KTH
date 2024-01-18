import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import tcpclient.TCPClient;




/**
 * What needs to be done:
 * 1. Define a class called MyRunnable that implements the interface Runnable
 * 2. Create a MyRunnable object
 * 3. Create a Thread Object, using the MyRunnable object from step 2 as a parameter
 * 4. Take the Thread Object from step 3, and call its start method
 * 5. This will, in turn, call the run method in the class that you defined in step 1
 *    - The run method is where the thread will do its work
 *    - This is where you should implement the code that correscond to the HTTPAsk server from task 3
 * 
 * 
 * 
 */


public class ConcHTTPAsk {

   /*Create a constant for port */
   public static int port;





   private static void parseArgs(String[] args){
      String argsString = String.join("", args);
      port = Integer.parseInt(argsString);
   }

   public void startServer() throws IOException{
      //Establish the server and which port it has
      ServerSocket serverSocket = new ServerSocket(port);
      System.out.println("The server has started\n");
      while (true) {
         Socket newClient = serverSocket.accept();
         MyRunnable handleClient = new MyRunnable(newClient);
         Thread newClientThread = new Thread(handleClient);
         newClientThread.start();
      }
   }







    public static void main( String[] args) throws IOException{
      parseArgs(args);

      ConcHTTPAsk server = new ConcHTTPAsk();
      server.startServer();
    }
}

