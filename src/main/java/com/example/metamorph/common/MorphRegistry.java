// src/main/java/com/example/metamorph/common/MorphRegistry.java
package com.example.metamorph.common;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class MorphRegistry {

    private static final Map<Integer, MorphEntry> entries =
            new TreeMap<Integer, MorphEntry>();

    public static void init() {
        registerDefaults();
    }

    public static void register(MorphEntry entry) {
        if (entry != null) {
            entries.put(entry.id, entry);
        }
    }

    public static MorphEntry get(int id) {
        return entries.get(id);
    }

    public static Collection<MorphEntry> getAll() {
        return Collections.unmodifiableCollection(entries.values());
    }

    private static void registerDefaults() {
        register(new MorphEntry(50, "Creeper", 0.6F, 1.7F));
        register(new MorphEntry(51, "Skeleton", 0.6F, 1.95F));
        register(new MorphEntry(52, "Spider", 1.4F, 0.9F));
        register(new MorphEntry(54, "Zombie", 0.6F, 1.95F));
        register(new MorphEntry(90, "Pig", 0.9F, 0.9F));
        register(new MorphEntry(91, "Sheep", 0.9F, 1.3F));
        register(new MorphEntry(92, "Cow", 0.9F, 1.4F));
        register(new MorphEntry(93, "Chicken", 0.4F, 0.7F));
        register(new MorphEntry(95, "Wolf", 0.6F, 0.85F));
        register(new MorphEntry(120, "Villager", 0.6F, 1.95F));
    }
}