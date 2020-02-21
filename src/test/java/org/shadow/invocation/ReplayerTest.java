package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.shadow.Bar;
import org.shadow.BaseTest;
import org.shadow.Baz;
import org.shadow.Foo;
import org.shadow.exception.ReplayException;
import org.shadow.filtering.ObjectFilter;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.shadow.Fluently.*;

@Slf4j
public class ReplayerTest extends BaseTest {
    @Test
    public void testReplay() throws TimeoutException, InterruptedException, ReplayException {
        String name = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");

        Record record = new InMemoryRecord(recordings -> {
            resume();
            return true;
        }).withBatchSize(1);

        ObjectFilter filter = filter(
                noise().from(Foo.class),
                secrets().from(Foo.class),
                noise().from(Baz.class),
                secrets().from(Baz.class)
        );

        Bar proxy = record(bar)
                .filteringWith(filter)
                .savingTo(record)
                .proxyingAs(Bar.class);
        // TODO: How does client signial to service that this recording is a shadowed call to be evaluated against a candidate?
        Instant timestamp = Instant.now();
        assertEquals(result, proxy.doSomethingShadowed(foo));
        await(5L, TimeUnit.SECONDS);

        proxy = replay(Bar.class)
                    .filteringWith(filter)
                    .retrievingFrom(record)
                    .startingAt(timestamp);
        // TODO: Where does timestamp come from in the context of a replay? Candidate service receives ReplayRequest instead of usual input?
        assertEquals(result, proxy.doSomethingShadowed(foo));
        log.info(name + " finishing.");
    }
}