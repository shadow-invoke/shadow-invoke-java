package io.shadowstack.candidates.registrars;

import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;

/**
 * Registrar which is a REST API client for calling the registration endpoint on an oracle service.
 */
public interface RestCandidateRegistrar extends CandidateRegistrar {
        @RequestLine("POST /candidate/register")
        @Headers("Content-Type: application/json")
        RegistrationResponse register(RegistrationRequest request);

    /**
     * Create a new client for the given oracle host, conforming to the CandidateRegistrar interface, which
     * will forward candidate registration requests to the oracle's "register" endpoint.
     * @param host The host name of the oracle, including the protocol and port, e.g. "http://localhost:8080".
     * @return An instance of CandidateRegistrar that forwards to the oracle's "register" endpoint.
     */
    public static CandidateRegistrar create(String host) {
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(RestCandidateRegistrar.class))
                .logLevel(Logger.Level.BASIC)
                .target(RestCandidateRegistrar.class, host);
    }
}
