package org.shadow.invoke.client;

import org.junit.Test;
import org.shadow.invoke.Bar;
import org.shadow.invoke.Baz;
import org.shadow.invoke.Foo;
import org.shadow.invoke.core.InvocationRecord;
import org.shadow.invoke.core.Recordings;

import java.time.LocalDateTime;

import static org.junit.Assert.*;
import static org.shadow.invoke.client.ShadowingInvocation.shadow;
import static org.shadow.invoke.core.FieldFilter.from;

public class ShadowingInvocationTest {
    private static final Bar bar = new Bar();
    private static final Baz baz = new Baz("Pawn", 75000.00D, 1234);
    private static final Foo foo = new Foo("Bob", "Smith", 35, LocalDateTime.now(), baz);

    @Test
    public void testShadow() {
        shadow(bar)
                .ignoring(
                        from(Foo.class).fields("timestamp"),
                        from(Baz.class).fields("id")
                )
                .redacting(
                        from(Foo.class).fields("lastName"),
                        from(Baz.class).fields("salary")
                )
                .invoke(Bar.class)
                .doSomethingShadowed(foo);
        InvocationRecord recording = Recordings.INSTANCE.getThreadLocalRecording();
        assertNotNull(recording);
        assertTrue(Foo.class.isAssignableFrom(recording.getOutput().getClass()));
    }
}