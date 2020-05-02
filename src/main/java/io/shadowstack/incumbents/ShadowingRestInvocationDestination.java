package io.shadowstack.incumbents;

import feign.Headers;
import feign.RequestLine;
import io.shadowstack.Invocation;

import java.util.List;

/**
 * A REST destination that stores and forwards invocations for shadowing.
 * Requests to all registered, relevant candidates will be made.
 *
 * ShadowingRestInvocationDestination destination = Feign.builder()
 *  .client(new OkHttpClient())
 *  .encoder(new JacksonEncoder())
 *  .decoder(new JacksonDecoder())
 *  .logger(new Slf4jLogger(CandidateRegistrar.class))
 *  .logLevel(Logger.Level.FULL)
 *  .target(ShadowingRestInvocationDestination.class, "http://localhost:8080");
 */
public interface ShadowingRestInvocationDestination extends InvocationDestination {
    @RequestLine("POST /record/shadowing")
    @Headers("Content-Type: application/json")
    List<Invocation> send(List<Invocation> invocations);
}
