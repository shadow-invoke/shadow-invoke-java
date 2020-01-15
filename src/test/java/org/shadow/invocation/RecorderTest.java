package org.shadow.invocation;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.shadow.*;

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
    public void setUp() throws Exception {
        Recording.QUEUE.clear();
    }

    @Test
    public void testRecordNoiseSecrets() {
        Bar proxy = record(bar)
                        .filtering(
                                noise()
                                    .from(Foo.class)
                                    .where(named("timestampd"))
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

        Recording recording = Recording.QUEUE.poll();
        assertNotNull(recording);
        assertEquals(recording.getReferenceResult(), bar.doSomethingShadowed(foo));
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
        assertEquals(recording.getReferenceResult(), bar.doSomethingShadowed(foo));

        assertNotNull(recording.getEvaluatedArguments());
        assertTrue(recording.getEvaluatedArguments().length > 0);
        assertTrue(recording.getEvaluatedArguments()[0] instanceof Foo);
        Foo evaluatedFoo = (Foo)recording.getEvaluatedArguments()[0];
        assertEquals(evaluatedFoo.getLastName(), DefaultValue.of(String.class));
        assertEquals(evaluatedFoo.getBaz().getSalary(), DefaultValue.of(Double.class));
        assertEquals(evaluatedFoo.getFirstName(), foo.getFirstName());
        assertEquals(evaluatedFoo.getTimestamp(), foo.getTimestamp());
        assertEquals(evaluatedFoo.getBaz().getTitle(), foo.getBaz().getTitle());
        assertEquals(evaluatedFoo.getBaz().getHeight(), foo.getBaz().getHeight());
        assertEquals(evaluatedFoo.getBaz().getId(), foo.getBaz().getId());
    }
}