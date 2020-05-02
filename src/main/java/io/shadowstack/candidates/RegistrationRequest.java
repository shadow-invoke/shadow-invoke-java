package io.shadowstack.candidates;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class RegistrationRequest {
    private String targetClassName;          // fully qualified
    private String methodName;
    private List<String> argumentClassNames; // fully qualified
    private String candidateServiceHost;
    private int candidateServicePort;
}
