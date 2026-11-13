package com.example.metamorph.common;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MorphManager {

    public static final int NONE = 0;

    private static final Map<UUID, Integer> clientMorphs = new ConcurrentHashMap<UUID, Integer>();
    private static final Set<Integer> unlockedClient = new HashSet<Integer>();

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

    public static void setUnlockedClient(Set<Integer> list) {
        unlockedClient.clear();
        unlockedClient.addAll(list);
        unlockedClient.add(0); // 永远解锁恢复原状
    }

    public static boolean isUnlockedClient(int id) {
        return id == 0 || unlockedClient.contains(id);
    }
}
