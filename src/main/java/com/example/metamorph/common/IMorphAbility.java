// src/main/java/com/example/metamorph/common/IMorphAbility.java
package com.example.metamorph.common;

import net.minecraft.entity.player.EntityPlayer;

public interface IMorphAbility {

    void onMorphStart(EntityPlayer player);

    void onMorphEnd(EntityPlayer player);

    void onUpdate(EntityPlayer player);
}