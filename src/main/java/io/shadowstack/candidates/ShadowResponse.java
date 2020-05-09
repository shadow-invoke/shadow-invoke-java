package io.shadowstack.candidates;

import io.shadowstack.invocations.InvocationContext;
import io.shadowstack.invocations.InvocationKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShadowResponse {
    private InvocationKey invocationKey;
    private InvocationContext invocationContext;
    private Object result;
}
