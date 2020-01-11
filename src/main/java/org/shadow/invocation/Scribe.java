package org.shadow.invocation;

import com.rits.cloning.Cloner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.field.Filter;
import org.shadow.schedule.Schedule;

import java.lang.reflect.Method;
import java.util.Arrays;

@Data
@Slf4j
public class Scribe implements MethodInterceptor {
    private static final Cloner CLONER = new Cloner();
    private final Object originalInstance;
    private Filter[] filters;
    private Schedule schedule = null;

    public Scribe(Object originalInstance) {
        this.originalInstance = originalInstance;
    }

    public Scribe filtering(Filter... filters) {
        this.filters = filters;
        return this;
    }

    public Scribe capturing(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    // capturing(percent(5))
    // capturing(hourly(60))

    public <T> T as(Class<T> cls) {
        if(cls == null || !cls.isInstance(this.originalInstance)) {
            String message = "Invalid combination of class %s and original instance %s. Returning null.";
            String className = (cls != null)? cls.getSimpleName() : "null";
            log.warn(String.format(message, className, this.originalInstance.getClass().getSimpleName()));
            return null;
        }
        return (T)Enhancer.create(cls, this);
    }

    @Override
    public Object intercept(Object o, Method method, Object[] arguments, MethodProxy proxy) throws Throwable {
        Object result = method.invoke(this.originalInstance, arguments);
        try {
            // TODO: Filter here, then submit to queue for delivery
            Transcript transcript = new Transcript(this.originalInstance, method, arguments, result);
        } catch(Throwable t) {
            String message = "While intercepting recorded invocation. Method=%s, Args=%s, Object=%s.";
            String passed = Arrays.toString(arguments);
            String className = this.originalInstance.getClass().getSimpleName();
            log.error(String.format(message, method.getName(), passed, className), t);
        }
        return result;
    }
}
