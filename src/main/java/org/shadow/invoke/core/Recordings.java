package org.shadow.invoke.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Recordings {
    public static final Map<String, InvocationRecord> SAVED = new HashMap<>();

    private Recordings() {}

    public static InvocationRecord createAndSave() {
        InvocationRecord recording = new InvocationRecord();
        String guid = UUID.randomUUID().toString();
        recording.setGuid(guid);
        SAVED.put(guid, recording);
        return recording;
    }

}
