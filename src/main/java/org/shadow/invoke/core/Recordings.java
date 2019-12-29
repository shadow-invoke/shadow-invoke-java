package org.shadow.invoke.core;

import lombok.Data;
import java.lang.reflect.Method;
import java.util.*;

@Data
public class Recordings {
    private final Map<String, InvocationRecord> savedRecordings = new HashMap<>();
    private final ThreadLocal<String> threadLocalRecordingGuid = new ThreadLocal<>();
    public static final Recordings INSTANCE = new Recordings();

    private Recordings() {}

    public InvocationRecord createAndSave(Object[] inputs, Object output, Method method,
                                          List<FieldFilter> redacted, List<FieldFilter> ignored) {
        InvocationRecord recording = new InvocationRecord(redacted, ignored, Arrays.asList(inputs), output, method);
        String guid = UUID.randomUUID().toString();
        this.threadLocalRecordingGuid.set(guid);
        this.savedRecordings.put(guid, recording);
        return recording;
    }

    public InvocationRecord getThreadLocalRecording() {
        if(this.threadLocalRecordingGuid.get() != null) {
            return this.savedRecordings.get(this.threadLocalRecordingGuid.get());
        }
        return null;
    }
}
