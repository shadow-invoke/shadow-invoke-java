package io.shadowstack.candidates;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import feign.Feign;
import feign.Headers;
import feign.Logger;
import feign.RequestLine;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import io.shadowstack.*;
import io.shadowstack.candidates.registrars.CandidateRegistrar;
import io.shadowstack.candidates.registrars.RegistrationRequest;
import io.shadowstack.candidates.registrars.RegistrationResponse;
import io.shadowstack.filters.ObjectFilter;
import io.shadowstack.invocations.InvocationContext;
import io.shadowstack.invocations.InvocationKey;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static io.shadowstack.Fluently.*;
import static org.junit.Assert.assertTrue;

public class CandidateServiceTest extends BaseTest {
    public static interface TestService {
        @RequestLine("POST /shadow")
        @Headers("Content-Type: application/json")
        ShadowResponse shadow(ShadowRequest request);
    }

    @Test
    public void testShadowSimple() throws Exception {
        final int port = this.findFreePort();
        CandidateRegistrar testRegistrar = new CandidateRegistrar() {
            @Override
            public RegistrationResponse register(RegistrationRequest request) {
                return null;
            }
        };

        Method testMethod = Bar.class.getMethod("doSomethingShadowed", Foo.class);
        Set<Method> methods = new HashSet<>();
        methods.add(testMethod);

        ObjectFilter filter = filter(
                noise().from(Foo.class),
                secrets().from(Foo.class),
                noise().from(Baz.class),
                secrets().from(Baz.class)
        ).toObjectDepth(1);

        try(CandidateService service = candidate(this.bar)
                                            .registeringWith(testRegistrar)
                                            .onPort(port)
                                            .shadowingMethods(methods)
                                            .filteringWith(filter)
                                            .buildService()
        ) {
            service.run();
            TestService client = Feign
                    .builder()
                    .client(new OkHttpClient())
                    // Encoder/decoder outfitted with JavaTimeModule to handle Foo's LocalDateTime member
                    .encoder(new JacksonEncoder(Collections.singleton(new JavaTimeModule())))
                    .decoder(new JacksonDecoder(Collections.singleton(new JavaTimeModule())))
                    .logger(new Slf4jLogger(TestService.class))
                    .logLevel(Logger.Level.FULL)
                    .target(TestService.class, "http://localhost:" + port);
            ShadowRequest request = new ShadowRequest();
            Object[] args = new Object[]{this.foo};
            request.setArguments(args);
            request.setInvocationContext(new InvocationContext(UUID.randomUUID().toString()));
            request.setInvocationKey(new InvocationKey(testMethod, args));
            ShadowResponse response = client.shadow(request);
            assertTrue(response.getResult().toString().equals(this.result));
        }
    }
}
