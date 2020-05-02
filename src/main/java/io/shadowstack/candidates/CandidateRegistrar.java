package io.shadowstack.candidates;

import feign.*;

/**
 * CandidateRegistrar registrar = Feign.builder()
 *  .client(new OkHttpClient())
 *  .encoder(new JacksonEncoder())
 *  .decoder(new JacksonDecoder())
 *  .logger(new Slf4jLogger(CandidateRegistrar.class))
 *  .logLevel(Logger.Level.FULL)
 *  .target(CandidateRegistrar.class, "http://localhost:8080");
 */
public interface CandidateRegistrar {
    @RequestLine("POST /register")
    @Headers("Content-Type: application/json")
    RegistrationResponse register(RegistrationRequest request);
}