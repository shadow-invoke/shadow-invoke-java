package org.shadow.invocation;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.shadow.*;
import org.shadow.filtering.Noise;
import org.shadow.filtering.ObjectFilter;
import org.shadow.filtering.Secret;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;
import static org.shadow.Fluently.*;

@Slf4j
public class RecorderTest extends BaseTest {
    @Test
    public void testRecordNoiseSecretsNamed() throws InterruptedException, TimeoutException, ExecutionException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        CompletableFuture<Recording> future = new CompletableFuture<>();
        ObjectFilter filter = filter(
                noise().from(Foo.class).where(named("timestamp")),
                secrets().from(Foo.class).where(named("lastName")),
                noise().from(Baz.class).where(named("id")),
                secrets().from(Baz.class).where(named("salary"))
        );
        Bar proxy = record(bar)
                        .filteringWith(filter)
                        .savingTo(new ObserveOnlyRecord() {
                            @Override
                            public void put(List<Recording> recordings) {
                                Recording recording = recordings.get(0);
                                log.info(name + ": got recording " + recording);
                                future.complete(recording);
                            }
                        }.withBatchSize(1))
                        .proxyingAs(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        Recording recording = future.get(5L, TimeUnit.SECONDS);

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
        ObjectFilter filter = filter(
                noise().from(Foo.class).where(annotated(Noise.class)),
                secrets().from(Foo.class).where(annotated(Secret.class)),
                noise().from(Baz.class), // annotated is default predicate
                secrets().from(Baz.class) // annotated is default predicate
        );
        Bar proxy = record(bar)
                .filteringWith(filter)
                .savingTo(new ObserveOnlyRecord() {
                    @Override
                    public void put(List<Recording> recordings) {
                        Recording recording = recordings.get(0);
                        log.info(name + ": got recording " + recording.toString());
                        future.complete(recording);
                    }
                }.withBatchSize(1))
                .proxyingAs(Bar.class);
        assertEquals(result, proxy.doSomethingShadowed(foo));
        Recording recording = future.get(5L, TimeUnit.SECONDS);

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
        ObjectFilter filter = filter(
                noise().from(Foo.class),
                secrets().from(Foo.class),
                noise().from(Baz.class),
                secrets().from(Baz.class)
        );
        Bar proxy = record(bar)
                .filteringWith(filter)
                .throttlingTo(
                        percent(1.0)
                )
                .savingTo(new ObserveOnlyRecord() {
                    @Override
                    public void put(List<Recording> recordings) {
                        log.info(name + ": got batch of size " + recordings.size());
                        threadAssertEquals(20, recordings.size());
                        resume();
                    }
                }.withBatchSize(20))
                .proxyingAs(Bar.class);
        for(int i=0; i<100; ++i) {
            assertEquals(result, proxy.doSomethingShadowed(foo));
        }
        await(5, TimeUnit.SECONDS, 5);
        log.info(name + " finishing.");
    }

    @Test(expected = TimeoutException.class)
    public void testOneHundredPercentThrottling() throws TimeoutException, InterruptedException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        ObjectFilter filter = filter(
                noise().from(Foo.class),
                secrets().from(Foo.class),
                noise().from(Baz.class),
                secrets().from(Baz.class)
        );
        Bar proxy = record(bar)
                .filteringWith(filter)
                .throttlingTo(
                        percent(0.0)
                )
                .savingTo(new ObserveOnlyRecord() {
                    @Override
                    public void put(List<Recording> recordings) {
                        fail("Transmit should never have been called.");
                        resume();
                    }
                }.withBatchSize(1))
                .proxyingAs(Bar.class);
        for(int i=0; i<100; ++i) {
            assertEquals(result, proxy.doSomethingShadowed(foo));
        }
        await(5, TimeUnit.SECONDS);
    }

    @Test
    public void testRateThrottling() throws InterruptedException, TimeoutException {
        String name = new Object(){}.getClass().getEnclosingMethod().getName();
        log.info(name + " starting.");
        ObjectFilter filter = filter(
                noise().from(Foo.class),
                secrets().from(Foo.class),
                noise().from(Baz.class),
                secrets().from(Baz.class)
        );
        Bar proxy = record(bar)
                .filteringWith(filter)
                .throttlingTo(
                        rate(2).per(1L, TimeUnit.SECONDS)
                )
                .savingTo(new ObserveOnlyRecord() {
                    @Override
                    public void put(List<Recording> recordings) {
                        log.info(name + ": got batch of size " + recordings.size());
                        threadAssertEquals(4, recordings.size());
                        resume();
                    }
                }.withBatchSize(4))
                .proxyingAs(Bar.class);
        for(int i=0; i<8; ++i) {
            assertEquals(result, proxy.doSomethingShadowed(foo));
            try {
                Thread.sleep(250L);
            } catch (InterruptedException ignored) { }
        }
        await(5, TimeUnit.SECONDS, 1);
        log.info(name + " finishing.");
    }
}