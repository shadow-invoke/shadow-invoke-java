package org.shadow.invocation;

import com.github.havarunner.HavaRunner;
import lombok.extern.slf4j.Slf4j;
import net.jodah.concurrentunit.Waiter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.shadow.*;
import org.shadow.field.Noise;
import org.shadow.field.Secret;
import org.shadow.invocation.transmission.Transmitter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.shadow.Fluently.*;

@Slf4j
@RunWith(HavaRunner.class)
public class RecorderTest {
    private static final Bar bar = new Bar();
    private static final Baz baz = new Baz("Pawn", 75000.00D, 69.5F, 1234L, new HashMap<>());
    private static final Foo foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);
    private static final String result = bar.doSomethingShadowed(foo);
    static {
        baz.getTaskTime().put(TimeUnit.MINUTES, Task.Clerical);
        baz.getTaskTime().put(TimeUnit.HOURS, Task.Management);
    }

    @Test
    public void testRecordNoiseSecretsNamed() throws InterruptedException, TimeoutException, ExecutionException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        CompletableFuture<Recording> future = new CompletableFuture<>();
        Bar proxy = record(bar)
                        .filteringOut(
                                noise().from(Foo.class).where(named("timestamp")),
                                secrets().from(Foo.class).where(named("lastName")),
                                noise().from(Baz.class).where(named("id")),
                                secrets().from(Baz.class).where(named("salary"))
                        )
                        .sendingTo(new Transmitter() {
                            @Override
                            public void transmit(List<Recording> recordings) {
                                assertEquals(recordings.size(), 1);
                                Recording recording = recordings.iterator().next();
                                assertNotNull(recording);
                                log.info(name + ": got recording " + recording.toString());
                                future.complete(recording);
                            }
                        })
                        .proxyingAs(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        Recording recording = future.get(2000L, TimeUnit.MILLISECONDS);

        assertNotNull(recording.getReferenceArguments());
        assertTrue(recording.getReferenceArguments().length > 0);
        assertTrue(recording.getReferenceArguments()[0] instanceof Foo);
        Foo referenceFoo = (Foo)recording.getReferenceArguments()[0];
        assertEquals(referenceFoo.getLastName(), DefaultValue.of(String.class));
        assertEquals(referenceFoo.getBaz().getSalary(), DefaultValue.of(Double.class));
        assertEquals(referenceFoo.getFirstName(), foo.getFirstName());
        assertEquals(referenceFoo.getTimestamp(), foo.getTimestamp());
        assertEquals(referenceFoo.getBaz().getTitle(), foo.getBaz().getTitle());
        assertEquals(referenceFoo.getBaz().getHeight(), foo.getBaz().getHeight());
        assertEquals(referenceFoo.getBaz().getId(), foo.getBaz().getId());
        assertEquals(recording.getReferenceResult(), result);

        assertNotNull(recording.getEvaluatedArguments());
        assertTrue(recording.getEvaluatedArguments().length > 0);
        assertTrue(recording.getEvaluatedArguments()[0] instanceof Foo);
        Foo evaluatedFoo = (Foo)recording.getEvaluatedArguments()[0];
        assertEquals(evaluatedFoo.getLastName(), DefaultValue.of(String.class));
        assertEquals(evaluatedFoo.getBaz().getSalary(), DefaultValue.of(Double.class));
        assertEquals(evaluatedFoo.getFirstName(), foo.getFirstName());
        assertEquals(evaluatedFoo.getTimestamp(), DefaultValue.of(LocalDateTime.class));
        assertEquals(evaluatedFoo.getBaz().getTitle(), foo.getBaz().getTitle());
        assertEquals(evaluatedFoo.getBaz().getHeight(), foo.getBaz().getHeight());
        assertEquals(evaluatedFoo.getBaz().getId(), DefaultValue.of(Long.class));
        assertEquals(recording.getEvaluatedResult(), result);
        log.info(name + " finishing.");
    }

    @Test
    public void testRecordNoiseSecretsAnnotated() throws InterruptedException, TimeoutException, ExecutionException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        CompletableFuture<Recording> future = new CompletableFuture<>();
        Bar proxy = record(bar)
                .filteringOut(
                        noise().from(Foo.class).where(annotated(Noise.class)),
                        secrets().from(Foo.class).where(annotated(Secret.class)),
                        noise().from(Baz.class), // annotated is default predicate
                        secrets().from(Baz.class) // annotated is default predicate
                )
                .sendingTo(new Transmitter() {
                    @Override
                    public void transmit(List<Recording> recordings) {
                        assertEquals(recordings.size(), 1);
                        Recording recording = recordings.iterator().next();
                        assertNotNull(recording);
                        log.info(name + ": got recording " + recording.toString());
                        future.complete(recording);
                    }
                })
                .proxyingAs(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        Recording recording = future.get(2000L, TimeUnit.MILLISECONDS);

        assertNotNull(recording.getReferenceArguments());
        assertTrue(recording.getReferenceArguments().length > 0);
        assertTrue(recording.getReferenceArguments()[0] instanceof Foo);
        Foo referenceFoo = (Foo)recording.getReferenceArguments()[0];
        assertEquals(referenceFoo.getLastName(), DefaultValue.of(String.class));
        assertEquals(referenceFoo.getBaz().getSalary(), foo.getBaz().getSalary(), 0.001D);
        assertEquals(referenceFoo.getFirstName(), foo.getFirstName());
        assertEquals(referenceFoo.getTimestamp(), foo.getTimestamp());
        assertEquals(referenceFoo.getBaz().getTitle(), foo.getBaz().getTitle());
        assertEquals(referenceFoo.getBaz().getHeight(), (float)DefaultValue.of(Float.class), 0.001D);
        assertEquals(referenceFoo.getBaz().getId(), foo.getBaz().getId());
        assertEquals(recording.getReferenceResult(), result);

        assertNotNull(recording.getEvaluatedArguments());
        assertTrue(recording.getEvaluatedArguments().length > 0);
        assertTrue(recording.getEvaluatedArguments()[0] instanceof Foo);
        Foo evaluatedFoo = (Foo)recording.getEvaluatedArguments()[0];
        assertEquals(evaluatedFoo.getLastName(), DefaultValue.of(String.class));
        assertEquals(evaluatedFoo.getBaz().getSalary(), foo.getBaz().getSalary(), 0.001D);
        assertEquals(evaluatedFoo.getFirstName(), foo.getFirstName());
        assertEquals(evaluatedFoo.getTimestamp(), DefaultValue.of(LocalDateTime.class));
        assertEquals(evaluatedFoo.getBaz().getTitle(), foo.getBaz().getTitle());
        assertEquals(evaluatedFoo.getBaz().getHeight(), (float)DefaultValue.of(Float.class), 0.001D);
        assertEquals(evaluatedFoo.getBaz().getId(), DefaultValue.of(Long.class));
        assertEquals(recording.getEvaluatedResult(), result);
        log.info(name + " finishing.");
    }

    @Test
    public void testZeroPercentThrottling() throws TimeoutException, InterruptedException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        final Waiter waiter = new Waiter();
        Bar proxy = record(bar)
                .filteringOut(
                        noise().from(Foo.class),
                        secrets().from(Foo.class),
                        noise().from(Baz.class),
                        secrets().from(Baz.class)
                )
                .throttlingTo(
                        percent(1.0)
                )
                .sendingTo(new Transmitter() {
                    @Override
                    public void transmit(List<Recording> recordings) {
                        log.info(name + ": got batch of size " + recordings.size());
                        waiter.assertEquals(20, recordings.size());
                        waiter.resume();
                    }
                }.withBatchSize(20))
                .proxyingAs(Bar.class);
        for(int i=0; i<100; ++i) {
            assertEquals(result, proxy.doSomethingShadowed(foo));
        }
        waiter.await(20, TimeUnit.SECONDS, 5);
        log.info(name + " finishing.");
    }

    @Test(expected = TimeoutException.class)
    public void testOneHundredPercentThrottling() throws TimeoutException, InterruptedException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        final Waiter waiter = new Waiter();
        Bar proxy = record(bar)
                .filteringOut(
                        noise().from(Foo.class),
                        secrets().from(Foo.class),
                        noise().from(Baz.class),
                        secrets().from(Baz.class)
                )
                .throttlingTo(
                        percent(0.0)
                )
                .sendingTo(new Transmitter() {
                    @Override
                    public void transmit(List<Recording> recordings) {
                        waiter.fail("Transmit should never have been called.");
                        waiter.resume();
                    }
                }.withBatchSize(20))
                .proxyingAs(Bar.class);
        for(int i=0; i<100; ++i) {
            assertEquals(result, proxy.doSomethingShadowed(foo));
        }
        waiter.await(5, TimeUnit.SECONDS);
        log.info(name + " finishing.");
    }

    @Test
    public void testRateThrottling() throws InterruptedException, ExecutionException, TimeoutException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        final Waiter waiter = new Waiter();
        Bar proxy = record(bar)
                .filteringOut(
                        noise().from(Foo.class),
                        secrets().from(Foo.class),
                        noise().from(Baz.class),
                        secrets().from(Baz.class)
                )
                .throttlingTo(
                        rate(2).per(1L, TimeUnit.SECONDS)
                )
                .sendingTo(new Transmitter() {
                    @Override
                    public void transmit(List<Recording> recordings) {
                        waiter.assertEquals(4, recordings.size());
                        waiter.resume();
                    }
                }.withBatchSize(4))
                .proxyingAs(Bar.class);
        for(int i=0; i<8; ++i) {
            assertEquals(result, proxy.doSomethingShadowed(foo));
            try {
                Thread.sleep(250L);
            } catch (InterruptedException ignored) { }
        }
        waiter.await(20, TimeUnit.SECONDS, 1);
        log.info(name + " finishing.");
    }
}