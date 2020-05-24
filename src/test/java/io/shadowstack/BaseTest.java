package io.shadowstack;

import net.jodah.concurrentunit.ConcurrentTestCase;
import org.junit.jupiter.api.BeforeAll;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BaseTest extends ConcurrentTestCase {
    protected static Bar bar = new Bar();
    protected static Baz baz = new Baz("Pawn", 75000.00D, 69.5F, 1234L, new HashMap<>());
    protected static Foo foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);
    protected static String result = bar.doSomethingShadowed(foo);

    @BeforeAll
    static void before() {
        bar = new Bar();
        baz = new Baz("Pawn", 75000.00D, 69.5F, 1234L, new HashMap<>());
        foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);
        result = bar.doSomethingShadowed(foo);
        baz.getTaskTime().put(TimeUnit.MINUTES, Title.Clerical);
        baz.getTaskTime().put(TimeUnit.HOURS, Title.Management);
    }

    protected int findFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("IOException in findFreePort ignored.");
            }
            return port;
        } catch (IOException e) {
            System.out.println("IOException in findFreePort ignored.");
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
    }
}
