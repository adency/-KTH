import java.net.*;
import java.nio.charset.StandardCharsets;
import java.io.*;
import tcpclient.TCPClient;



/**
 * What needs to be done:
 * 1. We need to connect to the server using the ServerSocket
 * 2. We then create a socket and start listening on it for incoming connection using accept()
 * 3. We read the data from the client using getInputStream()
 * 4. We send data to the client using getOutputStream()
 * 5. Close the client connection
 * 6. Terminate the server after the we are done
 * 
 * The server needs to process the HTTP request from TCPAsk
 * So that TCPAsk sends it a hostname, port and string and it then forwards that request and sends
 * it to the hostname, 
 * 
 * Methods:
 * 1. Starts the server in an infinite loop
 * 2. Stores HTTP reqeuest from client and other server
 * 3. Figures out what to do with the request
 * 4. Figures out what to do with the response
 * 
 * Tests
 * HTTP request error
 * 1. Check if it has GET- Check
 *   - If GET is misspelled - Check
 *    - Does it start with GET?
 * 2. HTTP
 *   - Does it end with HTTP/1.1
 *   -What happens if its misspelled? - Check
 *   -Whay happens if its wrong version - Check
 * 3. Do we have a port?
 * 4. Do we have a hostname?
 *   - What happens if its empty
 * Structural errors
 * 5. ASK
 *   -Is there an ASK?
 *   -Check if its in another place
 *   -Is Ask? the first thing after GET?
 * 6. Limit & Timeout & shutdown
 *   - are they empty?
 * 
 */



public class HTTPAsk {


   /*Create a constant for port */
   public static int port;



   private static void parseArgs(String[] args){
      String argsString = String.join("", args);
      port = Integer.parseInt(argsString);
   }

   /**
    * This method starts the server and does the following:
    * 1. 
    * @throws IOException
    */
   public void start() throws IOException{
      OutputStream outputStream = new ByteArrayOutputStream();


      String toServer = new String();
      String toBrowser = new String();
      //Establish the server and which port it has
      ServerSocket serverSocket = new ServerSocket(port);
      System.out.println("The server has started\n");
      while (true) {
         Socket client = serverSocket.accept();
         System.out.println("A client has connected\n");
         try {


            //client is the browser and it has now connected to our server
            outputStream = client.getOutputStream();
            //We need to get all the info from the client/browser and store it in a string
            //System.out.println("Step 1. We call HTTPInput ");
            toServer = HTTPInput(client);
            //System.out.println("The output of HTTPInput is: ");
            //System.out.println(toServer);
            //System.out.println("-----------------");



            //We need to check if it request contains all the information that we need
            //System.out.println("Step 2. We call checkInput");
            String checkToServer = checkInput(toServer);
            //System.out.println(checkToServer);
            //System.out.println("-----------------");


            //If the check is not ok, then we need to write the error to the client browser
            if (!checkToServer.contentEquals("ok")) {
               //System.out.println("The value of the check is not equal to ok");
               //System.out.println(checkToServer);
               //System.out.println("-----------------");
               outputStream.write(checkToServer.getBytes(StandardCharsets.UTF_8));
               outputStream.flush();
               client.close();
               System.out.println("The client is closed: " + client.isClosed());
            }
            //If the check is ok, then we proceed to send the HTTP request to the server
            // and send the response to the browser
            else{
               //System.out.println("Step 3. We call the Send() function");
               toBrowser = send(toServer);

               //System.out.println("The value of got back from the send function is: ");
               //System.out.println(toBrowser);
               //System.out.println("-----------------");

               //System.out.println("toBrowser is empty: ");
               //System.out.println(toBrowser.isEmpty());

               if (toBrowser.isEmpty()) {
                  toBrowser = "HTTP/1.1 404 Not Found \r\n\r\n";
               }
               else{
                  toBrowser = "HTTP/1.1 200 \r\n\r\n" + toBrowser;
               }
               //System.out.println("The content of toBrowser is: ");
               //System.out.println(toBrowser);

               outputStream.write(toBrowser.getBytes(StandardCharsets.UTF_8));
               outputStream.flush();
               client.close();
               System.out.println("The client is closed: " + client.isClosed());
            }
         } 
         catch (UnknownHostException e){
            toBrowser = "HTTP/1.1 404 \r\n\r\n";
            outputStream.write(toBrowser.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            client.close();
         }
         catch (Exception e) {
            // TODO: handle exception
         }
         
      }

   }

   /**
    * The method is gonna use the StringToSend method to get the hostname, limit
    * shutdown, timeout, port and data from the HTTP query and use that to call teh tcpclient.
    * @param input
    */
   public String send(String input) throws IOException {

      boolean shutdown = false;             // True if client should shutdown connection
      Integer timeout = null;			     // Max time to wait for data from server (null if no limit)
      Integer limit = null;			     // Max no. of bytes to receive from server (null if no limit)
      String hostname = null;			     // Domain name of server

      byte[] userInputBytes = new byte[0];  // Data to send to server
      String outputToServer = new String();;
      Integer porten = null;

      if(input.contains("shutdown=")){
         shutdown = Boolean.parseBoolean(extractString(input, "shutdown"));
      } 

      if(input.contains("timeout=")){
         timeout = Integer.parseInt(extractString(input, "timeout"));
      }

      if(input.contains("limit=")){
         limit = Integer.parseInt(extractString(input, "limit"));
      }


      if (input.contains("string=")) {
         outputToServer = extractString(input, "string");
      }
      hostname = extractString(input, "hostname");
      porten = Integer.parseInt(extractString(input, "port"));
      userInputBytes = outputToServer.getBytes();

      TCPClient tcpClient = new tcpclient.TCPClient(shutdown, timeout, limit);
      byte[] serverBytes  = tcpClient.askServer(hostname, porten, userInputBytes);
      String serverOutput = new String(serverBytes);
      return serverOutput;
      
   }

   
   /**
   * Takes the client stream and translates that to a string. It ends the while loop
   * if the string contains "\r\n\r".
   * @param clientSocket The lient socket 
   * @return A string that contains the complete HTTP request from the browser
   * @throws IOException
   */
   public String HTTPInput(Socket clientSocket) throws IOException {
      byte[] HTTPBuffer = new byte[1];
      ByteArrayOutputStream fromCLient = new ByteArrayOutputStream();
      int nextValue = 0;
      String nextString = new String();
      while (true) {
         nextValue = clientSocket.getInputStream().read(HTTPBuffer);
         fromCLient.write(HTTPBuffer, 0, nextValue);
         nextString = fromCLient.toString("UTF-8");
         if (nextString.endsWith("\r\n\r\n")) {
            break;
         }
      }
      return nextString;
   }




   /**
    * The Method gets a String as an input and a target string, the method checks for the target in
    * HTTP request from the browser and extracts the relevant info.
    * @param input The HTTP request from the browser
    * @param target relevant string that we want to extract
    * @return A string containing our target info
    */
   public String extractString(String input, String target){

      int start=0;
      int end =0;
      String toSend = new String();

      //The hostname is the first part and always ends with & because we need a port
      if (target.equals("hostname")) {
         start = input.indexOf("hostname=")+9;
         end = input.indexOf("&", start);
      }

      //The port can either be last or there can be a string before it.
      if (target.equals("port")) {
         start = input.lastIndexOf("port=")+5;
         if (input.contains("string=")) {
            end = input.indexOf("&", start);
         }
         else{
            end = input.indexOf(" ", start);
         }
      }
      if (target.equals("limit")) {
         start = input.lastIndexOf("limit=")+6;
         end = input.indexOf("&", start);
      }
      if (target.equals("timeout")) {
         start = input.lastIndexOf("timeout=")+8;
         end = input.indexOf("&", start);
      }
      if (target.equals("shutdown")) {
         start = input.lastIndexOf("shutdown=")+9;
         end = input.indexOf("&", start);
      }
      if (target.equals("string")) {
         start = input.lastIndexOf("string=")+7;
         end = input.indexOf("HTTP", start);
         toSend += input.substring(start, end);
         toSend = toSend + "\n";
         return toSend;
      }

      if (target.equals("data")) {
         int newline = input.indexOf("\r\n");
         toSend = input.substring(0, newline);
         toSend = toSend + "\r\n";
         toSend = toSend + "Host:" + extractString(input, "hostname")+ "\r\n\r\n";
      }

      toSend += input.substring(start, end);


      return toSend;
   }

   boolean isInt(String request, String target){
      String value = extractString(request, target);
      //System.out.println(value);
      try {
         Integer.parseInt(value);
         return true;
      } catch (NumberFormatException e) {
         return false;
      }

   }


   public String checkInput(String input) {


      String request = extractString(input, "data");
      //System.out.println(request);
      
      boolean checkHostname = request.contains("hostname=");
      boolean checkPort = request.contains("port=");
      boolean checkLimit = request.contains("limit=");
      boolean checkTimeout = request.contains("timeout=");
      boolean checkShutdown = request.contains("shutdown=");
      boolean favicon = request.contains("favicon");
      boolean containsHTTP = request.contains("HTTP/");
      //System.out.println(containsHTTP);
      boolean limit = false;
      boolean port = false;
      boolean timeout = false;
      boolean hostname = false;
      boolean shutdown = false;
      boolean GET = !request.contains("GET");
      boolean HTTP = !request.contains("HTTP/1.1");
      boolean ask = !request.contains("ask?"); 


      if (checkPort) {
         port = !isInt(request, "port");
      }

      if (checkTimeout) {
         timeout = !isInt(request, "timeout");
      }

      if (checkLimit) {
         limit = !isInt(request, "limit");
      }

      if (checkShutdown) {
         String shut = extractString(request, "shutdown");
         shutdown = shut.isBlank();
      }


      if (checkHostname) {
         hostname = extractString(request, "hostname").isBlank();
      }

      String output = new String();
      output = "ok";
      //If requst does not contain GET return 501 Not Implemented
      if (GET) {
         System.out.println("There is no GET or its misspelled");
         output = "HTTP/1.1 501 Not Implemented \r\n\r\n"; 
         return output;
      }
      //If the request contains HTTP but the version is anything but HTTP/1.1 we return
      // 505 HTTP Version Not Supported
      if (containsHTTP) {
         if (HTTP) {
            System.out.println("The HTTP version is not");
            output = "HTTP/1.1 505 HTTP Version Not Supported \r\n\r\n";
            return output;
         }
      }

      if (!checkHostname || !checkPort) {
         System.out.println("There is no host or port nr");
         output = "HTTP/1.1 400 Bad Request\r\n\r\n";
         return output;
      }

      //If request contains Limit, timeout or port, we check if they are empty or not an int
      if (!containsHTTP || hostname || port || limit || timeout || shutdown) {
         output = "HTTP/1.1 400 Bad Request\r\n\r\n";
         return output;
      }

      if (ask  | favicon)  {
         System.out.println("There is no ASK or asks for a favicon");
         output = "HTTP/1.1 404 Not Found\r\n\r\n";
         return output;
      }

      return output;
   }


    public static void main( String[] args) throws IOException{
      parseArgs(args);

      HTTPAsk server = new HTTPAsk();
      server.start();
    }


}

