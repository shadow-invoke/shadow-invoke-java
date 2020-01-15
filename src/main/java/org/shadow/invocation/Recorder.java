package org.shadow.invocation;

import com.rits.cloning.Cloner;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.shadow.DefaultValue;
import org.shadow.ReflectiveAccess;
import org.shadow.field.Filter;
import org.shadow.schedule.Schedule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

@Data
@Slf4j
public class Recorder implements MethodInterceptor {
    private static final Cloner CLONER = new Cloner();
    private final Object originalInstance;
    private Filter[] filters;
    private Schedule schedule = null;
    private int depth = 10;

    public Recorder(Object originalInstance) {
        this.originalInstance = originalInstance;
    }

    public Recorder filtering(Filter... filters) {
        this.filters = filters;
        return this;
    }

    public Recorder toDepth(int depth) {
        this.depth = depth;
        return this;
    }

    public Recorder capturing(Schedule schedule) {
        this.schedule = schedule;
        return this;
    }

    public <T> T build(Class<T> cls) {
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
            if(this.schedule == null || this.schedule.accept()) {
                Object[] referenceArguments = new Object[arguments.length];
                Object[] evaluatedArguments = new Object[arguments.length];
                for (int i = 0; i < arguments.length; ++i) {
                    referenceArguments[i] = CLONER.deepClone(arguments[i]);
                    this.filter(referenceArguments[i], 0, false);
                    evaluatedArguments[i] = CLONER.deepClone(arguments[i]);
                    this.filter(evaluatedArguments[i], 0, true);
                }
                Object referenceResult = CLONER.deepClone(result);
                this.filter(referenceResult, 0, false);
                Object evaluatedResult = CLONER.deepClone(result);
                this.filter(evaluatedResult, 0, true);
                Recording recording = new Recording(this.originalInstance,
                                                    method,
                                                    referenceArguments,
                                                    referenceResult,
                                                    evaluatedArguments,
                                                    evaluatedResult);
                Recording.QUEUE.add(recording);
            }
        } catch(Throwable t) {
            String message = "While intercepting recorded invocation. Method=%s, Args=%s, Object=%s.";
            String passed = Arrays.toString(arguments);
            String className = this.originalInstance.getClass().getSimpleName();
            log.error(String.format(message, method.getName(), passed, className), t);
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
            if(level < this.depth && areMembersFilterable(field)) {
                Object member = ReflectiveAccess.getMember(obj, field);
                this.filter(member, level + 1, isEvaluated);
            }
        }
    }

    private static boolean areMembersFilterable(Field field) {
        return (DefaultValue.of(field.getType()) == null) && !Modifier.isStatic(field.getModifiers());
    }
}
