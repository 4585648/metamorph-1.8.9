package com.example.metamorph.common;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CommonForgeEventHandler {

    // 记录每个玩家的技能冷却时间戳 (毫秒)
    private static final Map<UUID, Long> cooldowns = new HashMap<UUID, Long>();

    @SubscribeEvent
    public void onEntityConstruct(EntityEvent.EntityConstructing event) {
        if (event.entity instanceof EntityPlayer) {
            MorphData.register((EntityPlayer) event.entity);
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!(event.entity instanceof EntityPlayer)) return;

        EntityPlayer player = (EntityPlayer) event.entity;
        if (player.worldObj.isRemote) return;

        MorphData data = MorphData.get(player);
        if (data == null) return;

        int morphId = data.getMorphId();
        MorphEntry entry = MorphRegistry.get(morphId);

        if (entry != null && entry.ability != null) {
            entry.ability.onUpdate(player);
        }

        // 僵尸/骷髅白天燃烧
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

        // 飞行能力逻辑（蝙蝠 65、烈焰人 61、恶魂 56）
        if (morphId == 65 || morphId == 61 || morphId == 56) {
            if (!player.capabilities.isCreativeMode) {
                if (!player.capabilities.allowFlying) {
                    player.capabilities.allowFlying = true;
                    player.sendPlayerAbilities();
                }
                player.fallDistance = 0;
            }
        } else if (morphId == 0) {
            if (!player.capabilities.isCreativeMode) {
                if (player.capabilities.allowFlying) {
                    player.capabilities.allowFlying = false;
                    player.capabilities.isFlying = false;
                    player.sendPlayerAbilities();
                }
            }
        }
    }

    // 供网络包调用的技能触发方法
    public static void triggerSkill(EntityPlayerMP player) {
        MorphData data = MorphData.get(player);
        if (data == null || data.getMorphId() == 0) return;

        int morphId = data.getMorphId();
        UUID uuid = player.getUniqueID();
        long now = System.currentTimeMillis();
        long lastTime = cooldowns.containsKey(uuid) ? cooldowns.get(uuid) : 0L;

        // 判定冷却 (单位：秒)
        int cd = 0;
        if (morphId == 51) cd = 1;       // 骷髅 1秒
        else if (morphId == 50) cd = 10; // 苦力怕 10秒
        else if (morphId == 40) cd = 3;  // 末影人 3秒
        else if (morphId == 56) cd = 2;  // 恶魂 2秒
        else if (morphId == 61) cd = 1;  // 烈焰人 1秒

        if (cd > 0 && now - lastTime < cd * 1000L) {
            player.addChatMessage(new ChatComponentText("§c技能冷却中！"));
            return;
        }

        cooldowns.put(uuid, now);

        if (morphId == 51) {
            // 骷髅射箭
            EntityArrow arrow = new EntityArrow(player.worldObj, player, 1.5F);
            arrow.setDamage(2.0D);
            player.worldObj.playSoundAtEntity(player, "random.bow", 1.0F, 1.0F / (player.worldObj.rand.nextFloat() * 0.4F + 1.2F) + 0.5F);
            player.worldObj.spawnEntityInWorld(arrow);
            player.swingItem();
        } else if (morphId == 50) {
            // 苦力怕爆炸
            player.worldObj.createExplosion(player, player.posX, player.posY, player.posZ, 3.0F, true);
            player.setHealth(1.0F);
        } else if (morphId == 40) {
            // 末影人瞬移
            for (int i = 0; i < 16; ++i) {
                double d0 = player.posX + (player.worldObj.rand.nextDouble() - 0.5D) * 32.0D;
                double d1 = player.posY + (double)(player.worldObj.rand.nextInt(32) - 16);
                double d2 = player.posZ + (player.worldObj.rand.nextDouble() - 0.5D) * 32.0D;
                player.setPositionAndUpdate(d0, d1, d2);
                player.worldObj.playSoundEffect(player.posX, player.posY, player.posZ, "mob.endermen.portal", 1.0F, 1.0F);
                break;
            }
            player.swingItem();
        } else if (morphId == 56) {
            // 恶魂火球
            Vec3 vec3 = player.getLookVec();
            EntityLargeFireball fireball = new EntityLargeFireball(player.worldObj, player, 1.0D, 1.0D, 1.0D);
            fireball.posX = player.posX + vec3.xCoord * 2.0D;
            fireball.posY = player.posY + (double)player.getEyeHeight() + vec3.yCoord * 2.0D;
            fireball.posZ = player.posZ + vec3.zCoord * 2.0D;
            player.worldObj.spawnEntityInWorld(fireball);
            player.worldObj.playSoundAtEntity(player, "mob.ghast.fireball", 1.0F, 1.0F);
            player.swingItem();
        } else if (morphId == 61) {
            // 烈焰人小火球
            Vec3 vec3 = player.getLookVec();
            EntitySmallFireball fireball = new EntitySmallFireball(player.worldObj, player, 1.0D, 1.0D, 1.0D);
            fireball.posX = player.posX + vec3.xCoord * 2.0D;
            fireball.posY = player.posY + (double)player.getEyeHeight() + vec3.yCoord * 2.0D;
            fireball.posZ = player.posZ + vec3.zCoord * 2.0D;
            player.worldObj.spawnEntityInWorld(fireball);
            player.worldObj.playSoundAtEntity(player, "mob.blaze.breathe", 1.0F, 1.0F);
            player.swingItem();
        }
    }

    @SubscribeEvent
    public void onSetAttackTarget(LivingSetAttackTargetEvent event) {
        if (event.target instanceof EntityPlayer && event.entityLiving instanceof EntityLiving && event.entityLiving instanceof IMob) {
            EntityPlayer player = (EntityPlayer) event.target;
            MorphData data = MorphData.get(player);

            if (data != null && data.getMorphId() != 0) {
                if (data.getMorphId() == 120 && event.entityLiving instanceof EntityZombie) {
                    return;
                }

                EntityLiving mob = (EntityLiving) event.entityLiving;
                if (mob.getAITarget() != player) {
                    mob.setAttackTarget(null);
                }
            }
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        if (event.entityLiving instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) event.entityLiving;
            MorphData data = MorphData.get(player);

            if (data != null && data.getMorphId() != 0) {
                if (data.getMorphId() == 120 && event.source.getEntity() instanceof EntityZombie) {
                    return;
                }

                if (event.source.getEntity() instanceof EntityLiving && event.source.getEntity() instanceof IMob) {
                    EntityLiving mob = (EntityLiving) event.source.getEntity();
                    if (mob.getAITarget() != player) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        if (event.entityPlayer == null) return;

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
