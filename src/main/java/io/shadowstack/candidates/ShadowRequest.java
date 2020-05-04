package io.shadowstack.candidates;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.shadowstack.InvocationContext;
import io.shadowstack.InvocationKey;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ShadowRequest {
    private InvocationKey invocationKey;
    private InvocationContext invocationContext;
    private Object[] arguments;

    @JsonIgnore
    public boolean isValid() {
        return this.invocationContext != null && this.invocationContext.isValid() &&
                this.invocationKey != null && this.invocationKey.isValid() && this.arguments != null;
    }
}
