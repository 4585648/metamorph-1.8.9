// src/main/java/com/example/metamorph/common/MorphEntry.java
package com.example.metamorph.common;

public class MorphEntry {

    public final int id;
    public final String name;
    public final float width;
    public final float height;
    public final IMorphAbility ability;

    public MorphEntry(int id, String name, float width, float height) {
        this(id, name, width, height, EmptyAbility.INSTANCE);
    }

    public MorphEntry(int id, String name, float width, float height, IMorphAbility ability) {
        this.id = id;
        this.name = name;
        this.width = width;
        this.height = height;
        this.ability = ability;
    }
}