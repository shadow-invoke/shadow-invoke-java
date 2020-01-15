package org.shadow.invocation;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.shadow.Bar;
import org.shadow.Baz;
import org.shadow.Foo;
import org.shadow.Task;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

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
    }

    @After
    public void tearDown() throws Exception {
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
    }
}