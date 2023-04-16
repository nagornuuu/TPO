/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class Client {
    final String host;
    private final int port;
    final String id;

    SocketChannel socketChannel;

    // Constructor with parameters host, port, and id
    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    // A method connect to establish the connection between server and client
    public void connect() {
        try {
            // Opening a new SocketChannel and configuring it
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            // Establishing the connection between the client and the server
            socketChannel.connect(new InetSocketAddress("localhost", port));

            // Waiting for the connection to establish
            while(!socketChannel.finishConnect()) {
                Thread.sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // A method send to send the query to the server and get the response
    public String send(String req) {
        try {
            // Encoding the query string to UTF-8 format and storing it in a ByteBuffer
            ByteBuffer buffer = StandardCharsets.UTF_8.encode(req);
            while(true) {
                socketChannel.write(buffer);                          // Writing the buffer content to the SocketChannel
                ByteBuffer byteBuffer = ByteBuffer.allocate(1000);            // Creating a ByteBuffer to receive the response from the server
                int read = socketChannel.read(byteBuffer);                    // Reading the response from the server
                if (read == -1) {                                     // Closing the SocketChannel if the response is null
                    socketChannel.close();
                } else if (read > 0) {                                // If the response is not null, then decoding the ByteBuffer
                                                                      // content to string format and returning it.
                    byteBuffer.flip();
                    return StandardCharsets.UTF_8.decode(byteBuffer).toString();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
