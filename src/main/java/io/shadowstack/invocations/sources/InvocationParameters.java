package io.shadowstack.invocations.sources;

import io.shadowstack.invocations.InvocationContext;
import io.shadowstack.invocations.InvocationKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InvocationParameters {
    private String hash;
    private String context;

    public InvocationParameters(InvocationKey key, InvocationContext ctx) {
        this.hash = key.getInvocationHash();
        this.context = ctx.getContextId();
    }
}
