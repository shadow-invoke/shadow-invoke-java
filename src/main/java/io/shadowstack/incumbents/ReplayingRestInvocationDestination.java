package io.shadowstack.incumbents;

import feign.Headers;
import feign.RequestLine;
import io.shadowstack.Invocation;

import java.util.List;

/**
 * A REST destination that stores invocations for replaying only. No request to a registered candidate will be made.
 *
 * ReplayingRestInvocationDestination destination = Feign.builder()
 *  .client(new OkHttpClient())
 *  .encoder(new JacksonEncoder())
 *  .decoder(new JacksonDecoder())
 *  .logger(new Slf4jLogger(CandidateRegistrar.class))
 *  .logLevel(Logger.Level.FULL)
 *  .target(ReplayingRestInvocationDestination.class, "http://localhost:8080");
 */
public interface ReplayingRestInvocationDestination extends InvocationDestination {
    @RequestLine("POST /record/replaying")
    @Headers("Content-Type: application/json")
    List<Invocation> send(List<Invocation> invocations);
}
