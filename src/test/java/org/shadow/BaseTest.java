package org.shadow;

import net.jodah.concurrentunit.ConcurrentTestCase;
import org.junit.Before;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class BaseTest extends ConcurrentTestCase {
    protected Bar bar = new Bar();
    protected Baz baz = new Baz("Pawn", 75000.00D, 69.5F, 1234L, new HashMap<>());
    protected Foo foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);
    protected String result = bar.doSomethingShadowed(foo);

    @Before
    public void before() {
        bar = new Bar();
        baz = new Baz("Pawn", 75000.00D, 69.5F, 1234L, new HashMap<>());
        foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);
        result = bar.doSomethingShadowed(foo);
        baz.getTaskTime().put(TimeUnit.MINUTES, Task.Clerical);
        baz.getTaskTime().put(TimeUnit.HOURS, Task.Management);
    }
}
