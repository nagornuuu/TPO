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
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatServer implements Runnable {

    private ServerSocketChannel serverSocketChannel = null;
    private Selector selector = null;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Lock pendingRequestLock = new ReentrantLock();

    private final Map<SocketChannel, String> cmap = new LinkedHashMap<>();
    StringBuffer serverLog = new StringBuffer();
    Charset charset = StandardCharsets.UTF_8;

    public ChatServer(String host, int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open();                       // create a server socket channel
            serverSocketChannel.configureBlocking(false);                           // set non-blocking mode
            serverSocketChannel.socket().bind(new InetSocketAddress(host, port));   // bind to a port and host address
            selector = Selector.open();                                             // create a selector
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);         // register the channel with the selector to accept connections
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void startServer() {
        Future<?> serverTask = executorService.submit(this);                 // start the server thread
        System.out.println("Server started\n");
    }

    @Override
    public void run() {
        while (selector.isOpen()) {                                            // if the selector is open
            try {
                selector.select();                                             // wait for an event
                Set<SelectionKey> keys = selector.selectedKeys();              // get the set of keys
                for (SelectionKey key : keys) {                                // for each key...
                    if (key.isAcceptable()) {                                  // if it is acceptable
                        SocketChannel socketChannel = serverSocketChannel.accept(); // accept the connection
                        socketChannel.configureBlocking(false);                     // set non-blocking mode
                        socketChannel.register(selector, SelectionKey.OP_READ);     // register the channel with the selector to read
                    } else if (key.isReadable()) {                             // if it is readable
                        SocketChannel socketChannel = (SocketChannel) key.channel(); // get the channel
                        try {                                                  // try to read
                            pendingRequestLock.lock();                         // lock the pending request
                            request(socketChannel);                            // process the request
                        } finally {
                            pendingRequestLock.unlock();                       // unlock the pending request
                        }
                    }
                }
                keys.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void request(SocketChannel socketChannel) throws IOException {
        if (!socketChannel.isOpen()) return;
        if (socketChannel.socket().isClosed()) return;
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        int n = socketChannel.read(byteBuffer);                     // read into buffer
        if (n <= 0) return;
        byteBuffer.flip();
        CharBuffer charBuffer = charset.decode(byteBuffer);         // decode buffer into char buffer
        String freq = charBuffer.toString();                        // convert char buffer to string
        String[] mreq = freq.split("@");                           // split the string
        for (String request : mreq) {                               // for each request...
            String response;
            if (request.startsWith("login")) {
                String[] split = request.split("\\s+");       // split the string
                cmap.put(socketChannel, split[1]);                  // add the user to the map
                String info = split[1] + " logged in\n";
                response(info);
            } else if (request.startsWith("bye")) {
                response = cmap.get(socketChannel) + " logged out\n";
                response(response);
                if (socketChannel.isOpen()) {
                    socketChannel.close();
                    socketChannel.socket().close();
                }
                cmap.remove(socketChannel);
            } else {
                String info = cmap.get(socketChannel) + ": " + request + "\n";          // get the user name
                response(info);
            }
        }
    }

    private void response(String response) throws IOException {
        serverLog.append(LocalTime.now() + " " + response);                 // append the response to the server log
        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(response));  // encode the response into a byte buffer
        for (SocketChannel socketChannel : cmap.keySet()) {                 // for each socket channel...
            if (socketChannel.isOpen() && !socketChannel.socket().isClosed()) { // if the socket channel is open and not closed...
                socketChannel.write(byteBuffer.duplicate());                // write the response to the socket channel
            }
        }
    }

    public void stopServer() {
        try {
            if (!cmap.isEmpty()) {
                response("server stopped");
                for (SocketChannel socketChannel : cmap.keySet()) {         // for each socket channel...
                    if (socketChannel.isOpen()) {
                        socketChannel.close();
                        socketChannel.socket().close();
                    }
                }
            }
            pendingRequestLock.lock();
            try {
                executorService.shutdownNow();
            } finally {
                pendingRequestLock.unlock();
            }
            if (selector != null) selector.close();
            if (serverSocketChannel != null) {
                serverSocketChannel.close();
                serverSocketChannel.socket().close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped");
    }

    public String getServerLog() {
        return serverLog.toString();
    }
}
