/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Server extends Thread{


    String host;                                    // Variable that store the host as a string
    int port;                                       // Variable that store the port as an integer
    LinkedList<String> serverLog;                   // LinkedList that store server log messages as strings

    private final Map<SocketChannel, Queue<ByteBuffer>>  pendingData = new HashMap<>(); // Map object that store pending data for each SocketChannel as a queue of ByteBuffers
    private final Map<SocketChannel, String> name = new HashMap<>();                    // Map object that store the name of each SocketChannel as a string
    private final Map<SocketChannel, StringBuilder>  userLog = new HashMap<>();         // Map object that store user logs for each SocketChannel as a StringBuilder

    HashMap<SocketChannel,String> response = new HashMap<>(); // HashMap object that store server responses for each SocketChannel as a string

    // Constructor for the Server class that takes in a host and port parameter
    public Server(String host,int port){
        this.host = host;
        this.port = port;
        serverLog = new LinkedList<>();  // initialize the serverLog variable as a new instance of LinkedList
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();                                     // Selector object that manage the set of channels and open a new Selector
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();    // ServerSocketChannel object that handle incoming connections and open a new SocketChannel for each connection
            serverSocketChannel.bind(new InetSocketAddress(host, port));             // Binds the ServerSocketChannel to a specific address and port
            serverSocketChannel.configureBlocking(false);                            // Configures the ServerSocketChannel to be non-blocking
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);          // Registers the ServerSocketChannel with the Selector for accepting connections

            // Loops until the thread is interrupted
            while (!this.isInterrupted()) {
                if (selector.select() == 0) continue;                   // Waits until at least one channel is ready for I/O

                Set<SelectionKey> keys = selector.selectedKeys();       // Gets the set of keys with channels that are ready for I/O
                for (SelectionKey selectionKey : keys) {
                    if (selectionKey.isAcceptable()) {                                     // If the channel is ready to accept a new connection
                        SocketChannel socketChannel = serverSocketChannel.accept();        // Accepts the connection
                        socketChannel.configureBlocking(false);                            // Configures the SocketChannel to be non-blocking
                        socketChannel.register(selector, SelectionKey.OP_READ);            // Registers the SocketChannel with the Selector for reading
                        pendingData.put(socketChannel, new LinkedList<>());                // Adds the SocketChannel to the pendingData map
                    } else if (selectionKey.isReadable()) {                                // If the channel is ready to read
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();         // Gets the SocketChannel from the SelectionKey
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1000);                            // Creates a new ByteBuffer object
                        int read = socketChannel.read(byteBuffer);                                    // Reads the data from the SocketChannel into the ByteBuffer
                        if (read == -1) {                                                  // If the SocketChannel is closed
                            pendingData.remove(socketChannel);                             // Removes the SocketChannel from the pendingData map
                            socketChannel.close();                                         // Closes the SocketChannel
                        } else if (read > 0) {                                             // If the SocketChannel has data to read
                            byteBuffer.flip();                                             // Flips the ByteBuffer
                            String query = StandardCharsets.UTF_8.decode(byteBuffer).toString();       // Decodes the ByteBuffer into a string

                            String response = "";                                           // Variable that store the server response as a string

                            // If the query is a login request
                            if (query.startsWith("login")) {
                                // Adds a message to the server log indicating that the user logged in
                                serverLog.add(query.split(" ")[1] + " logged in at " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                                // Associates the SocketChannel with the user's name
                                name.put(socketChannel, query.split(" ")[1]);
                                // Adds a new StringBuilder to the userLog map with the SocketChannel as the key, and appends a message indicating that the user logged in
                                userLog.put(socketChannel, new StringBuilder());
                                userLog.get(socketChannel).append("=== " + query.split(" ")[1] + " log start ===\nlogged in\n");
                                response = "logged in";
                                // If the query is a logout request
                            } else if (query.equals("bye")) {
                                serverLog.add(name.get(socketChannel) + " logged out at " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                                userLog.get(socketChannel).append("logged out");
                                response = "logged out";
                                // If the query is a log transfer request
                            } else if (query.equals("bye and log transfer")) {
                                serverLog.add(name.get(socketChannel) + " logged out at " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                                userLog.get(socketChannel).append("logged out\n=== " + name.get(socketChannel) + " log end ===\n");
                                response = userLog.get(socketChannel).toString();
                                // If the query is a time request
                            } else if (query.split(" ").length == 2) {
                                serverLog.add(name.get(socketChannel) + " request at " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")) + ": \"" + query + "\"");
                                userLog.get(socketChannel).append("Request: " + query + "\nResult:\n" + Time.passed(query.split(" ")[0], query.split(" ")[1]) + "\n");
                                response = Time.passed(query.split(" ")[0], query.split(" ")[1]);
                            }

                            // Adds the response to the pendingData map
                            pendingData.get(socketChannel).add(StandardCharsets.UTF_8.encode(CharBuffer.wrap(response)));
                            // Switch the selection key's interest to read mode once all data has been written
                            selectionKey.interestOps(SelectionKey.OP_WRITE);
                        }
                    } else if (selectionKey.isWritable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        Queue<ByteBuffer> queue = pendingData.get(socketChannel);
                        while (!queue.isEmpty()) {                                     // While there is data waiting to be written
                            ByteBuffer byteBuffer = queue.peek();                      // Get the next buffer from the queue
                            int write = socketChannel.write(byteBuffer);               // Write the buffer to the channel
                            // If the write was unsuccessful, close the channel and remove it from the pending data map
                            if (write == -1) {
                                pendingData.remove(socketChannel);
                                socketChannel.close();
                                return;
                                // If there is still data remaining in the buffer, return and wait for the next write opportunity
                            } else if (byteBuffer.hasRemaining()) {
                                return;
                            }
                            queue.remove(); // Remove the buffer from the queue once it has been fully written
                        }
                        // Switch the selection key's interest to read mode once all data has been written
                        selectionKey.interestOps(SelectionKey.OP_READ);
                    }
                }
                keys.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    // Start the server and wait for one second
    public void startServer() throws InterruptedException {
        start();
        Thread.sleep(1000);
    }

    // Stop the server
    public void stopServer() {
        interrupt();
    }

    // Get the server log as a string with each message on a separate line
    public String getServerLog() {
        return String.join("\n", serverLog);
    }
}
