package org.shadow.invoke.core;

import lombok.Data;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class InvocationCache {
    private final Map<String, Invocation> savedRecordings = new HashMap<>();
    // TODO: Mapped by class+method
    private final ThreadLocal<String> threadLocalRecordingGuid = new ThreadLocal<>();
    public static final InvocationCache INSTANCE = new InvocationCache();

    private InvocationCache() {}

    public Invocation createAndSave(Object[] inputs, Object output, Method method, int maxLevels,
                                    List<FieldFilter> redacted, List<FieldFilter> ignored) {
        List<Object> args = Arrays.asList(inputs);
        Invocation recording = new Invocation(redacted, ignored, args, output, method, maxLevels);
        String guid = UUID.randomUUID().toString();
        this.threadLocalRecordingGuid.set(guid);
        this.savedRecordings.put(guid, recording);
        return recording;
    }

    public Invocation getThreadLocalRecording() {
        if(this.threadLocalRecordingGuid.get() != null) {
            return this.savedRecordings.get(this.threadLocalRecordingGuid.get());
        }
        return null;
    }
}
