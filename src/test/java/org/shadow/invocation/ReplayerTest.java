package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.shadow.Bar;
import org.shadow.Baz;
import org.shadow.Foo;
import org.shadow.exception.ReplayException;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.shadow.Fluently.*;

@Slf4j
public class ReplayerTest extends BaseTest {
    @Test
    public void testReplay() throws NoSuchMethodException, TimeoutException, InterruptedException, ReplayException {
        String name = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        Record record = new InMemoryRecord(recordings -> {
            resume();
            return true;
        }).withBatchSize(1);
        Bar proxy = record(bar)
                .filteringOut(
                    noise().from(Foo.class),
                    secrets().from(Foo.class),
                    noise().from(Baz.class),
                    secrets().from(Baz.class)
                )
                .savingTo(record)
                .proxyingAs(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        Instant timestamp = Instant.now();
        await(5L, TimeUnit.SECONDS);

        Method method = Bar.class.getMethod("doSomethingShadowed", Foo.class);
        Recording.InvocationKey key = new Recording.InvocationKey(method, Bar.class, new Object[]{foo}, timestamp);
        proxy = replay(Bar.class)
                    .filteringOut(
                            noise().from(Foo.class),
                            secrets().from(Foo.class),
                            noise().from(Baz.class),
                            secrets().from(Baz.class)
                    )
                    .retrievingFrom(record)
                    .atTimeBefore(timestamp)
                    .start();
        assertEquals(result, proxy.doSomethingShadowed(foo));
        log.info(name + " finishing.");
    }
}
