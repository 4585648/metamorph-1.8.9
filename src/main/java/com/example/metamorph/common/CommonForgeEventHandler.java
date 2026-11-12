// src/main/java/com/example/metamorph/common/CommonForgeEventHandler.java
package com.example.metamorph.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CommonForgeEventHandler {

    @SubscribeEvent
    public void onEntityConstruct(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            MorphData.register((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.entity instanceof EntityPlayer)) {
            return;
        }

        EntityPlayer player = (EntityPlayer) event.entity;

        if (player.worldObj.isRemote) {
            return;
        }

        MorphData data = MorphData.get(player);

        if (data == null) {
            return;
        }

        MorphEntry entry = MorphRegistry.get(data.getMorphId());

        if (entry != null && entry.ability != null) {
            entry.ability.onUpdate(player);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.entityPlayer == null) {
            return;
        }

        MorphData oldData = MorphData.get((EntityPlayer) event.original);
        MorphData newData = MorphData.get(event.entityPlayer);

        if (oldData != null && newData != null) {
            newData.setMorphId(oldData.getMorphId());
        }

        if (!event.entityPlayer.worldObj.isRemote) {
            MorphApi.applySize(event.entityPlayer, MorphApi.getMorphId(event.entityPlayer));
        }
    }
}