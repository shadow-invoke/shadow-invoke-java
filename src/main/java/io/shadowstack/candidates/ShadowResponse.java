package io.shadowstack.candidates;

import io.shadowstack.InvocationContext;
import io.shadowstack.InvocationKey;
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
