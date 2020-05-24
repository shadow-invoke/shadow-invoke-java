package io.shadowstack.candidates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.javalin.Javalin;
import io.javalin.core.JavalinServer;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.InternalServerErrorResponse;
import io.shadowstack.candidates.registrars.CandidateRegistrar;
import io.shadowstack.candidates.registrars.RegistrationRequest;
import io.shadowstack.candidates.registrars.RegistrationResponse;
import io.shadowstack.filters.ObjectFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.post;

/**
 * Exposes the candidate class as a service for invocation shadowing.
 */
@Slf4j
@Builder(buildMethodName = "buildService")
@AllArgsConstructor
public class CandidateService implements Runnable, AutoCloseable {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.registerModule(new JavaTimeModule()); // replace deprecated time module
    }
    @NonNull private final CandidateRegistrar registeringWith;
    @NonNull private final Set<Method> shadowingMethods;
    @NonNull private final Object candidateInstance;
    private final ObjectFilter filteringWith;
    private final int onPort;
    private Map<String, Method> methodsServed;
    private Map<String, RegistrationResponse> methodsRegistered;
    private Javalin app;

    @Override
    public void run() {
        // Generate keys for registering with the oracle service
        String cls = this.candidateInstance.getClass().getCanonicalName();
        this.methodsServed = this.shadowingMethods
                                    .stream()
                                    .collect(
                                        Collectors.toMap(
                                            m -> cls + "." + m.getName(),
                                            Function.identity()
                                        )
                                    );
        this.app = Javalin.create().start(onPort);
        register(this.app.server()); // might not get called until app shuts down?
        this.app.routes(() -> {
            post("/shadow", ctx -> {
                ShadowResponse response = this.shadow(ctx.bodyAsClass(ShadowRequest.class));
                ctx.json(Objects.requireNonNull(response));
            });
        });
    }

    private ShadowResponse shadow(ShadowRequest request) {
        log.info("Got: " + request);
        if(request == null || !request.isValid()) {
            throw new BadRequestResponse("Invocation passed is not valid");
        }
        String key = request.getInvocationKey().getTargetClassName() + "." + request.getInvocationKey().getTargetMethodName();
        Method toInvoke = this.methodsServed.get(key);
        if(toInvoke == null) {
            throw new BadRequestResponse("Method not explicitly served by this candidate: " + key);
        }
        // Convert arguments with toInvoke's argument classes and use toInvoke on the candidate instance.
        Object[] givenArguments = (request.getArguments() == null)? new Object[0] : request.getArguments();
        Class<?>[] methodArgumentClasses = toInvoke.getParameterTypes();
        if(methodArgumentClasses.length != givenArguments.length) {
            String msg = "Mismatched request arguments size (%d) and expected method arguments size (%d).";
            throw new BadRequestResponse(String.format(msg, givenArguments.length, methodArgumentClasses.length));
        }
        for(int i=0;i<givenArguments.length;++i) {
            try {
                givenArguments[i] = MAPPER.convertValue(givenArguments[i], methodArgumentClasses[i]);
            } catch(ClassCastException e) {
                String msg = "Can't cast argument %d from given type %s to expected type %s.";
                String givenType = givenArguments[i].getClass().getCanonicalName();
                String expectedType = methodArgumentClasses[i].getCanonicalName();
                throw new BadRequestResponse(String.format(msg, i, givenType, expectedType));
            }
        }
        // Then filter the result and create a new response object to return.
        Object result = null;
        try {
            result = toInvoke.invoke(this.candidateInstance, givenArguments);
            if(filteringWith != null) {
                result = this.filteringWith.filterAsEvaluatedCopy(result);
            } else {
                log.warn(String.format("Object filter is null while serving %s", request));
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            String msg = String.format("While generating result for %s, got error %s", request, e.getMessage());
            log.error(msg, e);
            throw new InternalServerErrorResponse(msg);
        }
        log.info("Returning: " + result);
        return new ShadowResponse(request.getInvocationKey(), request.getInvocationContext(), result);
    }

    private void register(JavalinServer server) {
        this.methodsRegistered = new HashMap<>();
        this.methodsServed.entrySet().forEach(e -> {
            List<String> argumentClasses = Arrays.stream(e.getValue().getParameterTypes())
                                                    .map(Class::getCanonicalName)
                                                    .collect(Collectors.toList());
            String targetClass = this.candidateInstance.getClass().getCanonicalName();
            RegistrationRequest request = new RegistrationRequest(targetClass, e.getValue().getName(), argumentClasses,
                                                                  server.getServerHost(), server.getServerPort());
            RegistrationResponse response = this.registeringWith.register(request);
            this.methodsRegistered.put(e.getKey(), response);
        });
    }

    @Override
    public void close() throws Exception {
        if(this.app != null) {
            this.app.stop();
        }
    }
}
