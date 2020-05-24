package io.shadowstack.incumbents;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InvocationSinkTest {
    @Test
    public void testBadBatchSizeDefault() {
        InvocationSink sink = new InvocationSink(invocations -> {
            return null;
        }).withBatchSize(-1);
        assertEquals(1, sink.getBatchSize());
    }
}
