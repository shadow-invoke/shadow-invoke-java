package io.shadowstack.candidates;

import io.javalin.Javalin;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.InternalServerErrorResponse;
import io.shadowstack.filters.ObjectFilter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.post;

/**
 * Exposes the candidate class as a service for invocation shadowing.
 */
@Slf4j
@Builder
@AllArgsConstructor
public class CandidateService implements Runnable {
    private final String oracleHost;
    private final Set<Method> shadowedMethods;
    private final Object candidateInstance;
    private final ObjectFilter objectFilter;
    private final int servicePort;

    @Override
    public void run() {
        // Generate keys for registering with the oracle service
        String cls = this.candidateInstance.getClass().getCanonicalName();
        final Map<String, Method> map = this.shadowedMethods
                                                .stream()
                                                .collect(
                                                    Collectors.toMap(
                                                        m -> cls + "." + m.getName(),
                                                        Function.identity()
                                                    )
                                                );
        register(map.keySet());
        Javalin app = Javalin.create().start(servicePort);
        app.routes(() -> {
            post("/shadow", ctx -> {
                ShadowResponse response = this.shadow(ctx.bodyAsClass(ShadowRequest.class), map);
                ctx.json(Objects.requireNonNull(response));
            });
        });
    }

    private ShadowResponse shadow(ShadowRequest request, Map<String, Method> methods) {
        if(request == null || !request.isValid()) {
            throw new BadRequestResponse("Invocation passed is not valid");
        }
        String key = request.getInvocationKey().getTargetClassName() + "." + request.getInvocationKey().getTargetMethodName();
        Method toInvoke = methods.get(key);
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
                givenArguments[i] = methodArgumentClasses[i].cast(givenArguments[i]);
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
            result = this.objectFilter.filterAsEvaluatedCopy(result);
        } catch (IllegalAccessException | InvocationTargetException e) {
            String msg = String.format("While generating result for %s, got error %s", request, e.getMessage());
            log.error(msg, e);
            throw new InternalServerErrorResponse(msg);
        }
        return new ShadowResponse(request.getInvocationKey(), request.getInvocationContext(), result);
    }

    private void register(Set<String> keys) {
        // TODO: connect to oracle service, send keys and this service's IP + port
    }
}
