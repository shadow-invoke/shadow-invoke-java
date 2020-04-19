package io.shadowstack.incumbents;

import io.shadowstack.Fluently;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import io.shadowstack.Bar;
import io.shadowstack.BaseTest;
import io.shadowstack.Baz;
import io.shadowstack.Foo;
import io.shadowstack.exceptions.InvocationReplayerException;
import io.shadowstack.filters.ObjectFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

@Slf4j
public class InvocationReplayerTest extends BaseTest {
    @Test
    public void testReplay() throws TimeoutException, InterruptedException, InvocationReplayerException {
        String name = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        final List<String> contextIds = new ArrayList<>(1);

        InvocationSink invocationSink = new InMemoryInvocationSink(recordings -> {
            if(recordings != null && !recordings.isEmpty()) {
                String guid = recordings.get(0).getInvocationContext().getContextId();
                log.info(name + ": got context GUID " + guid);
                contextIds.add(guid);
            }
            resume();
            return true;
        }).withBatchSize(1);

        ObjectFilter filter = Fluently.filter(
                Fluently.noise().from(Foo.class),
                Fluently.secrets().from(Foo.class),
                Fluently.noise().from(Baz.class),
                Fluently.secrets().from(Baz.class)
        );

        Bar proxy = Fluently.record(bar)
                            .filteringWith(filter)
                            .savingTo(invocationSink)
                            .proxyingAs(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        await(5L, TimeUnit.SECONDS);

        proxy = Fluently.replay(Bar.class)
                        .filteringWith(filter)
                        .retrievingFrom(invocationSink)
                        .forContextId(contextIds.get(0));
        // TODO: Where does context ID come from in a replay? Candidate service receives ReplayRequest instead of usual input?
        assertEquals(result, proxy.doSomethingShadowed(foo));
        log.info(name + " finishing.");
    }
}
