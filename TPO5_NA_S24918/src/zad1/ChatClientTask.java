/**
 *
 *  @author Nahornyi Andrii S24918
 *
 */

package zad1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class ChatClientTask extends FutureTask<String> {
    private final ChatClient client;


    private ChatClientTask(ChatClient c, Callable<String> code) {
        super(code);            // wywołanie konstruktora klasy FutureTask
        client = c;             // przypisanie obiektu ChatClient do pola client
    }


    public static ChatClientTask create(ChatClient c, List<String> msgs, int wait) {
        return new ChatClientTask(c, () -> {
            String id = c.getId();                                  // get id of client
            c.login();
            if (wait != 0) Thread.sleep(wait);                      // wait if necessary
            for (String request : msgs) {
                if (Thread.interrupted()) throw new InterruptedException(id + " task interrupted");
                c.send(request);                                    // send request
                if (wait != 0) Thread.sleep(wait);                  // wait if necessary
            }
            c.logout();                                             // logout
            if (wait != 0) Thread.sleep(wait);                      // wait if necessary
            return id + " task completed";                          // return message
        });
    }

    public ChatClient getClient() {
        return client;
    }
}