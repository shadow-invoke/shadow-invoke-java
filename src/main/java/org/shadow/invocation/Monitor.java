package org.shadow.invocation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;

@Data
@Slf4j
@AllArgsConstructor
public class Monitor implements MethodInterceptor {
    private final Object originalInstance;

    public <T> T invoke(Class<T> cls) {
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
            Record record = new Record(this.originalInstance, method, arguments, result);
        } catch(Throwable t) {
            String message = "While intercepting recorded invocation. Method=%s, Args=%s, Object=%s.";
            String passed = Arrays.toString(arguments);
            String className = this.originalInstance.getClass().getSimpleName();
            log.error(String.format(message, method.getName(), passed, className), t);
        }
        return result;
    }
}
