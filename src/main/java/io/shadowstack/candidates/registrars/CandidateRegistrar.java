package io.shadowstack.candidates.registrars;

public interface CandidateRegistrar {
    RegistrationResponse register(RegistrationRequest request);
}