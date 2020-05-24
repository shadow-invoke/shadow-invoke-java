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

import java.util.List;

/**
 * Destination which is a REST API client for calling the shadowing endpoint on an oracle service.
 * This stores and forwards invocations for shadowing and requests to all registered, relevant candidates will be made.
 */
public interface ShadowingRestInvocationDestination extends InvocationDestination {
    @RequestLine("POST /record/shadowing")
    @Headers("Content-Type: application/json")
    List<Invocation> send(List<Invocation> invocations);

    /**
     * Create a new client for the given oracle host, conforming to the InvocationDestination interface, which
     * will forward invocations for shadowing (i.e. they will be both recorded and forwarded to shadowing candidates).
     * @param host The host name of the oracle, including the protocol and port, e.g. "http://localhost:8080".
     * @return An instance of InvocationDestination that forwards to the oracle's "shadowing" endpoint.
     */
    static InvocationDestination createClient(String host) {
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(ShadowingRestInvocationDestination.class))
                .logLevel(Logger.Level.BASIC)
                .target(ShadowingRestInvocationDestination.class, host);
    }
}
