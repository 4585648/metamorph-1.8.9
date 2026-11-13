package com.example.metamorph.client;

import com.example.metamorph.ClientProxy;
import com.example.metamorph.MetamorphMod;
import com.example.metamorph.common.MorphApi;
import com.example.metamorph.common.MorphConfig;
import com.example.metamorph.common.MorphManager;
import com.example.metamorph.network.MorphRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientEventHandler {

    @SideOnly(Side.CLIENT)
    public static class KeyHandler {
        @SubscribeEvent
        public void onKeyInput(InputEvent.KeyInputEvent event) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.thePlayer == null) return;

            if (ClientProxy.openMenu.isPressed()) {
                if (!MorphConfig.allowMenu) return;
                if (MorphConfig.commandOnly && !mc.thePlayer.canCommandSenderUseCommand(2, "morph")) return;
                mc.displayGuiScreen(new MorphGui());
            }

            if (ClientProxy.skillKey.isPressed()) {
                MetamorphMod.network.sendToServer(new MorphRequestMessage(0, 1));
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class ClientTickHandler {
        // 记录玩家在客户端生成的“替身实体”
        private static final Map<UUID, EntityLivingBase> clientMobs = new HashMap<UUID, EntityLivingBase>();

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) {
                clientMobs.clear();
                return;
            }

            for (EntityPlayer player : mc.theWorld.playerEntities) {
                int morphId = MorphManager.getClient(player.getUniqueID());
                EntityLivingBase mob = clientMobs.get(player.getUniqueID());

                if (morphId > 0) {
                    // 如果替身不存在，或者变形 ID 改变了，则重新生成
                    if (mob == null || EntityList.getEntityID(mob) != morphId) {
                        if (mob != null) {
                            mob.setDead();
                            mc.theWorld.removeEntity(mob);
                        }
                        Entity entity = EntityList.createEntityByID(morphId, mc.theWorld);
                        if (entity instanceof EntityLivingBase) {
                            mob = (EntityLivingBase) entity;
                            // 使用负数 ID 防止与服务端真实实体冲突
                            mob.setEntityId(-player.getEntityId());
                            mob.noClip = true; // 穿透方块
                            mob.setEntityInvulnerable(true); // 无敌，防止被其他玩家/生物攻击
                            mob.setAlwaysRenderNameTag(false);
                            mc.theWorld.spawnEntityInWorld(mob);
                            clientMobs.put(player.getUniqueID(), mob);
                        }
                    }

                    if (mob != null) {
                        // 每一帧同步位置、旋转和动作
                        mob.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                        mob.prevPosX = player.prevPosX;
                        mob.prevPosY = player.prevPosY;
                        mob.prevPosZ = player.prevPosZ;
                        mob.lastTickPosX = player.lastTickPosX;
                        mob.lastTickPosY = player.lastTickPosY;
                        mob.lastTickPosZ = player.lastTickPosZ;
                        
                        mob.rotationYawHead = player.rotationYawHead;
                        mob.prevRotationYawHead = player.rotationYawHead;
                        mob.renderYawOffset = player.renderYawOffset;
                        mob.prevRenderYawOffset = player.renderYawOffset;
                        
                        mob.setSneaking(player.isSneaking());
                        mob.setSprinting(player.isSprinting());
                        
                        mob.limbSwing = player.limbSwing;
                        mob.limbSwingAmount = player.limbSwingAmount;
                        mob.prevLimbSwingAmount = player.limbSwingAmount;
                        mob.swingProgress = player.swingProgress;
                        mob.prevSwingProgress = player.swingProgress;
                        
                        mob.hurtTime = player.hurtTime;
                        mob.deathTime = player.deathTime;
                        
                        if (player.isBurning()) {
                            mob.setFire(2);
                        } else {
                            mob.extinguish();
                        }
                        
                        mob.ticksExisted = player.ticksExisted;
                        
                        // 同步玩家手上的物品（针对僵尸、骷髅等有手实体）
                        for (int slot = 0; slot < 5; slot++) {
                            mob.setCurrentItemOrArmor(slot, player.getEquipmentInSlot(slot));
                        }
                    }
                } else {
                    // 解除变形，清除替身
                    if (mob != null) {
                        mob.setDead();
                        mc.theWorld.removeEntity(mob);
                        clientMobs.remove(player.getUniqueID());
                    }
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class RenderHandler {
        @SubscribeEvent
        public void onRenderPlayer(RenderPlayerEvent.Pre event) {
            EntityPlayer player = event.entityPlayer;
            int morphId = MorphManager.getClient(player.getUniqueID());

            // 如果处于变形状态，直接取消原版玩家的渲染
            // 因为我们已经用 clientMobs 在世界里生成真正的怪物代替玩家了
            if (morphId > 0) {
                event.setCanceled(true);
            }
        }
    }
}
