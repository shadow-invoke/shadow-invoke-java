package org.shadow.invocation;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.shadow.*;
import org.shadow.field.Noise;
import org.shadow.field.Secret;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.shadow.Fluently.*;

public class RecorderTest {
    private static final Bar bar = new Bar();
    private static final Baz baz = new Baz(
            "Pawn", 75000.00D, 69.5F, 1234L,
            ImmutableMap.of(TimeUnit.MINUTES, Task.Clerical, TimeUnit.HOURS, Task.Management)
    );
    private static final Foo foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);

    @Before
    public void setUp() {
        Recording.QUEUE.clear();
    }

    @Test
    public void testRecordNoiseSecretsNamed() {
        Bar proxy = record(bar)
                        .filtering(
                                noise()
                                    .from(Foo.class)
                                    .where(named("timestamp"))
                                    .build(),
                                secrets()
                                    .from(Foo.class)
                                    .where(named("lastName"))
                                    .build(),
                                noise()
                                    .from(Baz.class)
                                    .where(named("id"))
                                    .build(),
                                secrets()
                                    .from(Baz.class)
                                    .where(named("salary"))
                                    .build()
                        )
                        .build(Bar.class);
        String result = proxy.doSomethingShadowed(foo);
        assertEquals(result, bar.doSomethingShadowed(foo));

        Recording recording = Recording.QUEUE.poll();
        assertNotNull(recording);
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
    }

    @Test
    public void testRecordNoiseSecretsAnnotated() {
        Bar proxy = record(bar)
                .filtering(
                        noise()
                                .from(Foo.class)
                                .where(annotated(Noise.class))
                                .build(),
                        secrets()
                                .from(Foo.class)
                                .where(annotated(Secret.class))
                                .build(),
                        noise()
                                .from(Baz.class) // annotated is default predicate
                                .build(),
                        secrets()
                                .from(Baz.class) // annotated is default predicate
                                .build()
                )
                .build(Bar.class);
        String result = proxy.doSomethingShadowed(foo);
        assertEquals(result, bar.doSomethingShadowed(foo));

        Recording recording = Recording.QUEUE.poll();
        assertNotNull(recording);
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
    }

    @Test
    public void testRecordPercentSchedule() {
        Bar proxy = record(bar)
                .filtering(
                        noise().from(Foo.class).build(),
                        secrets().from(Foo.class).build(),
                        noise().from(Baz.class).build(),
                        secrets().from(Baz.class).build()
                )
                .capturing(
                        percent(0.5)
                )
                .build(Bar.class);
        String expected = bar.doSomethingShadowed(foo);
        for(int i=0; i<100; ++i) {
            assertEquals(expected, proxy.doSomethingShadowed(foo));
        }
        // TODO: This test is going to be flaky; how to fix?
        assertTrue(Recording.QUEUE.size() > 25 && Recording.QUEUE.size() < 75);
    }
}