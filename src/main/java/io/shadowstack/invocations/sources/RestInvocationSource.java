package io.shadowstack.invocations.sources;

import feign.*;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.shadowstack.invocations.Invocation;

/**
 * Invocation source which is a REST API client for calling the recording endpoint on an oracle service.
 */
public interface RestInvocationSource extends InvocationSource {
    @RequestLine("GET /recording")
    @Headers("Content-Type: application/json")
    Invocation retrieve(@QueryMap InvocationParameters parameters);

    /**
     * Create a new client for the given oracle host, conforming to the InvocationSource interface, which
     * will retrieve recorded invocations via the oracle's "recording" endpoint.
     * @param host The host name of the oracle, including the protocol and port, e.g. "http://localhost:8080".
     * @return An instance of InvocationSource that retrieves from the oracle's "recording" endpoint.
     */
    static InvocationSource createClient(String host) {
        return Feign.builder()
                .client(new OkHttpClient())
                .encoder(new JacksonEncoder())
                .decoder(new JacksonDecoder())
                .logger(new Slf4jLogger(RestInvocationSource.class))
                .logLevel(Logger.Level.BASIC)
                .target(RestInvocationSource.class, host);
    }
}