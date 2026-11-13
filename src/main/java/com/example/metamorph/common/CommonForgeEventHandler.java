package com.example.metamorph.common;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
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

        // 变形为僵尸(54)或骷髅(51)时的白天燃烧逻辑
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

    // 拦截：当怪物试图锁定变形玩家时，如果是主动锁定（非被激怒），直接清空目标
    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.target instanceof EntityPlayer && event.entityLiving instanceof EntityLiving && event.entityLiving instanceof IMob) {
            EntityPlayer player = (EntityPlayer) event.target;
            MorphData data = MorphData.get(player);

            if (data != null && data.getMorphId() != 0) {
                EntityLiving mob = (EntityLiving) event.entityLiving;
                // 如果怪物的AITarget不是该玩家，说明玩家没打它，直接强制清空它的攻击目标
                if (mob.getAITarget() != player) {
                    mob.setAttackTarget(null);
                }
            }
        }
    }

    // 兜底拦截：如果怪物依然对你造成了伤害（如史莱姆碰撞），如果是主动攻击则直接取消伤害
    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            MorphData data = MorphData.get(player);

            if (data != null && data.getMorphId() != 0) {
                if (event.source.getEntity() instanceof EntityLiving && event.source.getEntity() instanceof IMob) {
                    EntityLiving mob = (EntityLiving) event.source.getEntity();
                    // 如果玩家没有打它，取消怪物造成的伤害
                    if (mob.getAITarget() != player) {
                        event.setCanceled(true);
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
