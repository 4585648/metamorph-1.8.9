package com.example.metamorph.common;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MorphManager {

    public static final int NONE = 0;

    private static final Map<UUID, Integer> clientMorphs = new ConcurrentHashMap<UUID, Integer>();
    
    // 使用线程安全的集合
    public static final Set<Integer> unlockedClient = Collections.synchronizedSet(new HashSet<Integer>());

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
        synchronized (unlockedClient) {
            unlockedClient.clear();
            unlockedClient.addAll(list);
            unlockedClient.add(0); // 永远解锁恢复原状
        }
    }

    public static boolean isUnlockedClient(int id) {
        // 兜底：如果服务端没发数据，或者列表为空，默认全部可见，防止UI彻底卡死
        if (unlockedClient.size() <= 1) {
            return true; 
        }
        return id == 0 || unlockedClient.contains(id);
    }
}
