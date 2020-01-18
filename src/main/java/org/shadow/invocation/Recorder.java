package org.shadow.invocation;

import com.rits.cloning.Cloner;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.DefaultValue;
import org.shadow.ReflectiveAccess;
import org.shadow.field.Filter;
import org.shadow.invocation.transmission.Transmitter;
import org.shadow.throttling.Throttle;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@Slf4j
public class Recorder implements MethodInterceptor {
    private static final Cloner CLONER = new Cloner();
    private final Object originalInstance;
    private Filter[] filters;
    @Getter private Throttle throttle = null;
    @Getter private Transmitter transmitter = null;
    private int objectDepth = 10;

    public Recorder(Object originalInstance) {
        this.originalInstance = originalInstance;
    }

    public Recorder filteringOut(Filter.Builder... filters) {
        this.filters = Arrays.stream(filters).map(Filter.Builder::build).toArray(Filter[]::new);
        return this;
    }

    public Recorder toDepth(int objectDepth) {
        this.objectDepth = objectDepth;
        return this;
    }

    public Recorder throttlingTo(Throttle throttle) {
        this.throttle = throttle;
        return this;
    }

    public Recorder sendingTo(Transmitter transmitter) {
        this.transmitter = transmitter;
        return this;
    }

    public <T> T proxyingAs(Class<T> cls) {
        if(cls == null || !cls.isInstance(this.originalInstance)) {
            String message = "Invalid combination of class %s and original instance %s. Returning null.";
            String className = (cls != null)? cls.getSimpleName() : "null";
            log.warn(String.format(message, className, this.originalInstance.getClass().getSimpleName()));
            return null;
        }
        return (T)Enhancer.create(cls, this);
    }

    private Object[] cloneArguments(Object[] arguments, boolean isEvaluated) {
        Object[] copy = new Object[arguments.length];
        for (int i = 0; i < arguments.length; ++i) {
            copy[i] = CLONER.deepClone(arguments[i]);
            this.filter(copy[i], 0, isEvaluated);
        }
        return copy;
    }

    private Object cloneResult(Object result, boolean isEvaluated) {
        Object copy = CLONER.deepClone(result);
        this.filter(copy, 0, false);
        return copy;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] arguments, MethodProxy proxy) throws Throwable {
        Object result = method.invoke(this.originalInstance, arguments);
        try {
            Recording recording = new Recording(this.originalInstance, method,
                                                cloneArguments(arguments, false),
                                                cloneResult(result, false),
                                                cloneArguments(arguments, true),
                                                cloneResult(result, true));
            Record.INSTANCE.submit(recording, this);
        } catch(Throwable t) {
            String message = "While intercepting recorded invocation. Method=%s, Args=%d, Object=%s.";
            String className = this.originalInstance.getClass().getSimpleName();
            log.error(String.format(message, method.getName(), arguments.length, className), t);
        }
        return result;
    }

    private void filter(Object obj, int level, boolean isEvaluated) {
        if(obj == null) return;
        Class<?> cls = obj.getClass();
        for(Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            for(Filter filter : this.filters) {
                if(isEvaluated) {
                    filter.filterAsEvaluatedCopy(obj, field);
                } else {
                    filter.filterAsReferenceCopy(obj, field);
                }
            }
            boolean filterable = (DefaultValue.of(field.getType()) == null) && !Modifier.isStatic(field.getModifiers());
            if(level < this.objectDepth && filterable) {
                Object member = ReflectiveAccess.getMember(obj, field);
                this.filter(member, level + 1, isEvaluated);
            }
        }
    }
}
