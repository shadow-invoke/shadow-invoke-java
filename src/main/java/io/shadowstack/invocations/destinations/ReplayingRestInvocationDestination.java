package io.shadowstack.invocations.destinations;

import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.shadowstack.invocations.Invocation;
import io.shadowstack.invocations.destinations.InvocationDestination;

import java.util.List;

/**
 * Destination which is a REST API client for calling the recording endpoint on an oracle service.
 * This stores invocations for replaying only. No request to a registered candidate will be made.
 */
public interface ReplayingRestInvocationDestination extends InvocationDestination {
    @RequestLine("POST /record/replaying")
    @Headers("Content-Type: application/json")
    List<Invocation> send(List<Invocation> invocations);

    /**
     * Create a new client for the given oracle host, conforming to the InvocationDestination interface, which
     * will forward invocations for replay only (i.e. they will recorded but not be forwarded to shadowing candidates).
     * @param host The host name of the oracle, including the protocol and port, e.g. "http://localhost:8080".
     * @return An instance of InvocationDestination that forwards to the oracle's "replaying" endpoint.
     */
    static InvocationDestination createClient(String host) {
        return Feign.builder()
                    .client(new OkHttpClient())
                    .encoder(new JacksonEncoder())
                    .decoder(new JacksonDecoder())
                    .logger(new Slf4jLogger(ReplayingRestInvocationDestination.class))
                    .logLevel(Logger.Level.BASIC)
                    .target(ReplayingRestInvocationDestination.class, host);
    }
}
