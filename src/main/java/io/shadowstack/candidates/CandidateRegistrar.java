package io.shadowstack.candidates;

import feign.*;

public interface CandidateRegistrar {
    @RequestLine("POST /register")
    @Headers("Content-Type: application/json")
    RegistrationResponse register(RegistrationRequest request);
}
/**
 Service service = Feign.builder()
 .client(new OkHttpClient())
 .encoder(new JacksonEncoder())
 .decoder(new JacksonDecoder())
 .logger(new Slf4jLogger(Service.class))
 .logLevel(Logger.Level.FULL)
 .target(Service.class, "http://localhost:8080");
 */