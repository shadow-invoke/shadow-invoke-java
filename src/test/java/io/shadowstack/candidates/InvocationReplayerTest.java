package io.shadowstack.candidates;

import io.shadowstack.*;
import io.shadowstack.incumbents.InvocationRecorder;
import io.shadowstack.incumbents.InvocationSink;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import io.shadowstack.exceptions.InvocationReplayerException;
import io.shadowstack.filters.ObjectFilter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.shadowstack.Fluently.*;
import static org.junit.jupiter.api.Assertions.*;

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

        ObjectFilter filter = Fluently.filter(noise().from(Foo.class),
                                              secrets().from(Foo.class),
                                              noise().from(Baz.class),
                                              secrets().from(Baz.class));

        Bar proxy = record(bar)
                        .filteringWith(filter)
                        .sendingTo(new InvocationSink(invocationDestination).withBatchSize(1))
                        .buildProxy(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        await(5L, TimeUnit.SECONDS);

        proxy = replay(Bar.class)
                    .filteringWith(filter)
                    .retrievingFrom(invocationDestination)
                    .forContextId(contextIds.get(0))
                    .buildProxy();
        assertEquals(result, proxy.doSomethingShadowed(foo));
        final Bar finalProxy = proxy;
        // TODO: Verify exception is propagated. Invocation returned by source is null somehow.
        //assertThrows(NotImplementedException.class, () -> finalProxy.doSomethingBad(foo));

        // Test bad constructions
        InvocationReplayer replayer = replay(null)
                                        .filteringWith(filter)
                                        .retrievingFrom(invocationDestination)
                                        .forContextId(contextIds.get(0));
        final InvocationReplayer finalReplayer1 = replayer;
        assertThrows(InvocationReplayerException.class, () -> finalReplayer1.buildProxy());

        replayer = replay(Bar.class)
                    .filteringWith(null)
                    .retrievingFrom(invocationDestination)
                    .forContextId(contextIds.get(0));
        final InvocationReplayer finalReplayer2 = replayer;
        assertThrows(InvocationReplayerException.class, () -> finalReplayer2.buildProxy());

        replayer = replay(Bar.class)
                    .filteringWith(filter)
                    .retrievingFrom(null)
                    .forContextId(contextIds.get(0));
        final InvocationReplayer finalReplayer3 = replayer;
        assertThrows(InvocationReplayerException.class, () -> finalReplayer3.buildProxy());

        replayer = replay(Bar.class)
                    .filteringWith(filter)
                    .retrievingFrom(invocationDestination)
                    .forContextId(null);
        final InvocationReplayer finalReplayer4 = replayer;
        assertThrows(InvocationReplayerException.class, () -> finalReplayer4.buildProxy());

        log.info(name + " finishing.");
    }
}
