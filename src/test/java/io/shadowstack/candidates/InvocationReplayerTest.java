package io.shadowstack.candidates;

import io.shadowstack.*;
import io.shadowstack.incumbents.InvocationRecorder;
import io.shadowstack.incumbents.InvocationSink;
import io.shadowstack.invocations.Invocation;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import io.shadowstack.exceptions.InvocationReplayerException;
import io.shadowstack.filters.ObjectFilter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.shadowstack.Fluently.*;
import static io.shadowstack.shoehorn.Fluently.reference;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class InvocationReplayerTest extends BaseTest {
    @Test
    public void testReplay() throws TimeoutException, InterruptedException, InvocationReplayerException {
        String name = new Object() {}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        final Queue<String> happyContextIds = new LinkedList<>();
        final Queue<String> sadContextIds = new LinkedList<>();
        final Method happyMethod = reference(Bar.class).from(b -> b.doSomethingShadowed(null));
        final Method sadMethod = reference(Bar.class).from(b -> b.doSomethingBad(null));

        InMemoryInvocationDestination invocationDestination = new InMemoryInvocationDestination(recordings -> {
            if(recordings != null && !recordings.isEmpty()) {
                Invocation invocation = recordings.get(0);
                String guid = invocation.getInvocationContext().getContextId();
                String methodName = invocation.getInvocationKey().getTargetMethodName();
                if(methodName.equals(happyMethod.getName())) {
                    happyContextIds.offer(guid);
                    log.info(name + ": got HAPPY context GUID " + guid + " for method " + methodName + " :D");
                } else if(methodName.equals(sadMethod.getName())) {
                    sadContextIds.offer(guid);
                    log.info(name + ": got SAD context GUID " + guid + " for method " + methodName + " :'(");
                }
            }
            resume();
            return true;
        });

        ObjectFilter filter = filter(noise().from(Foo.class),
                                              secrets().from(Foo.class),
                                              noise().from(Baz.class),
                                              secrets().from(Baz.class));

        Bar proxy = record(bar)
                        .filteringWith(filter)
                        .sendingTo(new InvocationSink(invocationDestination).withBatchSize(1))
                        .buildProxy(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        await(1L, TimeUnit.SECONDS); // TODO: Why? Does the Flux need to be explicitly flushed?

        proxy = replay(Bar.class)
                    .filteringWith(filter)
                    .retrievingFrom(invocationDestination)
                    .forContextId(happyContextIds.poll())
                    .buildProxy();
        assertEquals(result, proxy.doSomethingShadowed(foo));

        final Bar finalProxyRecord = record(bar)
                    .filteringWith(filter)
                    .sendingTo(new InvocationSink(invocationDestination).withBatchSize(1))
                    .buildProxy(Bar.class);
        assertThrows(NotImplementedException.class, () -> finalProxyRecord.doSomethingBad(foo));
        await(1L, TimeUnit.SECONDS); // TODO: Why? Does the Flux need to be explicitly flushed?

        final Bar finalProxyReplay = replay(Bar.class)
                .filteringWith(filter)
                .retrievingFrom(invocationDestination)
                .forContextId(sadContextIds.poll())
                .buildProxy();
        assertThrows(NotImplementedException.class, () -> finalProxyReplay.doSomethingBad(foo));

        // Test bad constructions
        InvocationReplayer replayer = replay(null)
                                        .filteringWith(filter)
                                        .retrievingFrom(invocationDestination)
                                        .forContextId("badf00d");
        final InvocationReplayer finalReplayer1 = replayer;
        assertThrows(InvocationReplayerException.class, () -> finalReplayer1.buildProxy());

        replayer = replay(Bar.class)
                    .filteringWith(null)
                    .retrievingFrom(invocationDestination)
                    .forContextId("badf00d");
        final InvocationReplayer finalReplayer2 = replayer;
        assertThrows(InvocationReplayerException.class, () -> finalReplayer2.buildProxy());

        replayer = replay(Bar.class)
                    .filteringWith(filter)
                    .retrievingFrom(null)
                    .forContextId("badf00d");
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
