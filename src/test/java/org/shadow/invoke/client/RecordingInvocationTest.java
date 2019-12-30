package org.shadow.invoke.client;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.shadow.invoke.Bar;
import org.shadow.invoke.Baz;
import org.shadow.invoke.Foo;
import org.shadow.invoke.Task;
import org.shadow.invoke.core.InvocationRecord;
import org.shadow.invoke.core.Recordings;
import org.shadow.invoke.core.RedactedFields;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import static org.junit.Assert.*;
import static org.shadow.invoke.client.RecordingInvocation.record;
import static org.shadow.invoke.core.FieldFilter.from;

public class RecordingInvocationTest {
    private static final Bar bar = new Bar();
    private static final Baz baz = new Baz(
            "Pawn", 75000.00D, 69.5F, 1234L,
            ImmutableMap.of(TimeUnit.MINUTES, Task.Clerical, TimeUnit.HOURS, Task.Management)
    );
    private static final Foo foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);

    @Test
    public void testRecord() {
        record(bar)
                .redacting(
                        from(Foo.class).fields("lastName"),
                        from(Baz.class).fields("salary")
                )
                .toObjectGraphDepth(5)
                .invoke(Bar.class)
                .doSomethingShadowed(foo);
        InvocationRecord recording = Recordings.INSTANCE.getThreadLocalRecording();
        assertNotNull(recording);
        assertEquals(recording.getOutput(), bar.doSomethingShadowed(foo));
        assertNotNull(recording.getInputs());
        assertTrue(recording.getInputs().size() > 0);
        assertTrue(recording.getInputs().get(0) instanceof Foo);
        Foo redactedFoo = (Foo)recording.getInputs().get(0);
        assertEquals(redactedFoo.getLastName(), RedactedFields.redactedValueOf(String.class));
        assertEquals(redactedFoo.getBaz().getSalary(), RedactedFields.redactedValueOf(Double.class));
        assertEquals(redactedFoo.getFirstName(), foo.getFirstName());
        assertEquals(redactedFoo.getTimestamp(), foo.getTimestamp());
        assertEquals(redactedFoo.getBaz().getTitle(), foo.getBaz().getTitle());
        assertEquals(redactedFoo.getBaz().getHeight(), foo.getBaz().getHeight());
        assertEquals(redactedFoo.getBaz().getId(), foo.getBaz().getId());
    }

    @Test
    public void testRecordWithSingularRedaction() {
        record(bar)
                .redact(
                        from(Foo.class).fields("lastName")
                )
                .toObjectGraphDepth(5)
                .invoke(Bar.class)
                .doSomethingShadowed(foo);
        InvocationRecord recording = Recordings.INSTANCE.getThreadLocalRecording();
        assertNotNull(recording);
        assertEquals(recording.getOutput(), bar.doSomethingShadowed(foo));
        assertNotNull(recording.getInputs());
        assertTrue(recording.getInputs().size() > 0);
        assertTrue(recording.getInputs().get(0) instanceof Foo);
        Foo redactedFoo = (Foo)recording.getInputs().get(0);
        assertEquals(redactedFoo.getLastName(), RedactedFields.redactedValueOf(String.class));
        assertEquals(redactedFoo.getBaz().getSalary(), foo.getBaz().getSalary(), 0.001F);
        assertEquals(redactedFoo.getFirstName(), foo.getFirstName());
        assertEquals(redactedFoo.getTimestamp(), foo.getTimestamp());
        assertEquals(redactedFoo.getBaz().getTitle(), foo.getBaz().getTitle());
        assertEquals(redactedFoo.getBaz().getHeight(), foo.getBaz().getHeight());
        assertEquals(redactedFoo.getBaz().getId(), foo.getBaz().getId());
    }
}