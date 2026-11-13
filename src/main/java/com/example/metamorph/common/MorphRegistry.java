package com.example.metamorph.common;

import com.example.metamorph.common.ability.MorphAbilities;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class MorphRegistry {

    private static final Map<Integer, MorphEntry> entries = new TreeMap<Integer, MorphEntry>();

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
        register(new MorphEntry(40, "Enderman", 0.6F, 2.9F, new MorphAbilities.EndermanAbility()));
        register(new MorphEntry(50, "Creeper", 0.6F, 1.7F, new MorphAbilities.CreeperAbility()));
        register(new MorphEntry(51, "Skeleton", 0.6F, 1.95F, new MorphAbilities.SkeletonAbility()));
        register(new MorphEntry(52, "Spider", 1.4F, 0.9F, new MorphAbilities.SpiderAbility()));
        register(new MorphEntry(54, "Zombie", 0.6F, 1.95F, new MorphAbilities.ZombieAbility()));
        register(new MorphEntry(55, "Slime", 2.04F, 2.04F, new MorphAbilities.SlimeAbility()));
        register(new MorphEntry(56, "Ghast", 4.0F, 4.0F, new MorphAbilities.GhastAbility()));
        register(new MorphEntry(61, "Blaze", 0.6F, 1.8F, new MorphAbilities.BlazeAbility()));
        register(new MorphEntry(65, "Bat", 0.5F, 0.9F, new MorphAbilities.BatAbility()));
        register(new MorphEntry(90, "Pig", 0.9F, 0.9F));
        register(new MorphEntry(91, "Sheep", 0.9F, 1.3F));
        register(new MorphEntry(92, "Cow", 0.9F, 1.4F));
        register(new MorphEntry(93, "Chicken", 0.4F, 0.7F, new MorphAbilities.ChickenAbility()));
        register(new MorphEntry(94, "Squid", 0.8F, 0.8F, new MorphAbilities.SquidAbility()));
        register(new MorphEntry(95, "Wolf", 0.6F, 0.85F));
        register(new MorphEntry(120, "Villager", 0.6F, 1.95F));
    }
}
