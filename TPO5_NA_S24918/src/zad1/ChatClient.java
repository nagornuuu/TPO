/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatClient {

    private final ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
    private final StringBuffer chatView = new StringBuffer();
    private final int port;
    private final String id, host;
    private SocketChannel socketChannel;
    private final Charset charset = StandardCharsets.UTF_8;
    private final Lock readThreadLock = new ReentrantLock();
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private Thread readThread;

    // constructor with host, port and id parameters and initializes the chat view
    public ChatClient(String host, int port, String id) {
        this.id = id;
        this.host = host;
        this.port = port;
        chatView.append("===").append(id).append(" chat view\n");
    }

    public String getId() {
        return id;
    }

    public String getChatView() {
        return chatView.toString();
    }

    public void signalNewMsg(String msg) {
        propertyChangeSupport.firePropertyChange("newMsg", "", msg);                 // fire a new message event
    }

    // adds a message to the chat view
    public void login() {
        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(host, port));      // connect to the server
            socketChannel.configureBlocking(false);                                     // set non-blocking mode
            while (!socketChannel.finishConnect()) Thread.sleep(50);               // wait for connection
        } catch (Exception e) {
            throw new RuntimeException("connection error: " + e.getMessage(), e);       // throw exception if connection failed
        }
        send("login " + id);                                                     // send login message
        startReadThread();
    }

    public void logout() {
        if (socketChannel != null) {                                                  // if the socket channel is not null
            send("bye");
            readThreadLock.lock();
            try {
                readThread.interrupt();
            } finally {
                readThreadLock.unlock();
            }
        }
    }

    public void startReadThread() {
        readThread = new Thread(() -> {                                              // create a new thread
            while (true) {
                readThreadLock.lock();
                try {
                    if (!socketChannel.isOpen()) break;
                    byteBuffer.clear();
                    if (Thread.interrupted() && !socketChannel.isOpen()) return;
                    int n;
                    int i = 0;                                                       // set the counter to 0
                    while ((n = socketChannel.read(byteBuffer)) == 0) {              // while there is no data to read
                        Thread.sleep(50);
                        if (Thread.interrupted() && !socketChannel.isOpen()) return;
                        i++;                                                         // increment the counter
                        if (i >= 10) {
                            addToView("*** no response on time");
                            break;
                        }
                    }
                    if (n < 0) break;
                    byteBuffer.flip();
                    CharBuffer charBuffer = charset.decode(byteBuffer);
                    String response = charBuffer.toString();
                    signalNewMsg(response);                                          // signal new message
                    addToView(response);
                } catch (InterruptedException e) {
                    return;
                } catch (Exception e) {
                    try {
                        if (socketChannel != null && socketChannel.isOpen()) {       // if the socket channel is not null and is open
                            socketChannel.close();                                   // close the socket channel
                            socketChannel.socket().close();                          // close the socket
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println(id + " exc " + e);
                    e.printStackTrace();
                    break;
                } finally {
                    readThreadLock.unlock();
                }
            }
        });
        readThread.start();                                                          // start the read thread
    }

    public void send(String request) {
        ByteBuffer byteBuffer = charset.encode(CharBuffer.wrap(request + "@"));
        try {
            socketChannel.write(byteBuffer);                                         // write to the socket channel
        } catch (Exception e) {
            addToView("send error: " + e);                                      // add a message to the chat view
            e.printStackTrace();
        }
    }

    public void addToView(String msg) {
        chatView.append(msg);                                                       // append the message to the chat view
    }

}