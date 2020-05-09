package io.shadowstack.candidates;

import io.shadowstack.*;
import io.shadowstack.incumbents.InvocationSink;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import io.shadowstack.exceptions.InvocationReplayerException;
import io.shadowstack.filters.ObjectFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.shadowstack.Fluently.*;
import static org.junit.Assert.assertEquals;

@Slf4j
public class InvocationReplayerTest extends BaseTest {
    @Test
    public void testReplay() throws TimeoutException, InterruptedException, InvocationReplayerException {
        String name = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        final List<String> contextIds = new ArrayList<>(1);

        InMemoryInvocationDestination invocationDestination = new InMemoryInvocationDestination(recordings -> {
            if(recordings != null && !recordings.isEmpty()) {
                String guid = recordings.get(0).getInvocationContext().getContextId();
                log.info(name + ": got context GUID " + guid);
                contextIds.add(guid);
            }
            resume();
            return true;
        });

        ObjectFilter filter = Fluently.filter(
                                        noise().from(Foo.class),
                                        secrets().from(Foo.class),
                                        noise().from(Baz.class),
                                        secrets().from(Baz.class)
        );

        Bar proxy = record(bar)
                            .filteringWith(filter)
                            .sendingTo(new InvocationSink(invocationDestination).withBatchSize(1))
                            .proxyingAs(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        await(5L, TimeUnit.SECONDS);

        proxy = replay(Bar.class)
                    .filteringWith(filter)
                    .retrievingFrom(invocationDestination)
                    .forContextId(contextIds.get(0));
        // TODO: Where does context ID come from in a replay? Candidate service receives ReplayRequest instead of usual input?
        assertEquals(result, proxy.doSomethingShadowed(foo));
        log.info(name + " finishing.");
    }
}
