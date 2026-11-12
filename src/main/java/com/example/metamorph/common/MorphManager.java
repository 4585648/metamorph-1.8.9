// src/main/java/com/example/metamorph/common/MorphManager.java
package com.example.metamorph.common;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MorphManager {

    public static final int NONE = 0;

    private static final Map<UUID, Integer> clientMorphs =
            new ConcurrentHashMap<UUID, Integer>();

    public static int getClient(UUID uuid) {
        Integer value = clientMorphs.get(uuid);
        return value == null ? NONE : value;
    }

    public static void setClient(UUID uuid, int morphId) {
        if (morphId == NONE) {
            clientMorphs.remove(uuid);
        } else {
            clientMorphs.put(uuid, morphId);
        }
    }
}