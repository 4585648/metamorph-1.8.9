package com.example.metamorph.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
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

        int morphId = data.getMorphId();
        MorphEntry entry = MorphRegistry.get(morphId);

        if (entry != null && entry.ability != null) {
            entry.ability.onUpdate(player);
        }

        // 新增：变形为僵尸(54)或骷髅(51)时的白天燃烧逻辑
        if (morphId == 54 || morphId == 51) {
            if (player.worldObj.isDaytime() && !player.isImmuneToFire()) {
                float brightness = player.getBrightness(1.0F);
                BlockPos pos = new BlockPos(player.posX, player.posY + player.getEyeHeight(), player.posZ);
                
                if (brightness > 0.5F && player.worldObj.canBlockSeeSky(pos)) {
                    if (player.worldObj.rand.nextFloat() * 30.0F < (brightness - 0.4F) * 2.0F) {
                        player.setFire(8);
                    }
                }
            }
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
