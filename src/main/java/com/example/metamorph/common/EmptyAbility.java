// src/main/java/com/example/metamorph/common/EmptyAbility.java
package com.example.metamorph.common;

import net.minecraft.entity.player.EntityPlayer;

public class EmptyAbility implements IMorphAbility {

    public static final EmptyAbility INSTANCE = new EmptyAbility();

    @Override
    public void onMorphStart(EntityPlayer player) {

    }

    @Override
    public void onMorphEnd(EntityPlayer player) {

    }

    @Override
    public void onUpdate(EntityPlayer player) {

    }
}