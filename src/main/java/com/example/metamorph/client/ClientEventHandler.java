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
        // 记录生成的替身实体
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
                    // 1. 利用原版机制让玩家隐形（光影会正常渲染这个隐形状态）
                    if (!player.isInvisible()) {
                        player.setInvisible(true);
                    }

                    // 2. 生成并同步替身怪物
                    if (mob == null || EntityList.getEntityID(mob) != morphId) {
                        if (mob != null) {
                            mob.setDead();
                            mc.theWorld.removeEntity(mob);
                        }
                        Entity entity = EntityList.createEntityByID(morphId, mc.theWorld);
                        if (entity instanceof EntityLivingBase) {
                            mob = (EntityLivingBase) entity;
                            mob.setEntityId(-player.getEntityId()); // 负数 ID 防冲突
                            mob.noClip = true; // 穿墙
                            mob.setAlwaysRenderNameTag(false);
                            mc.theWorld.spawnEntityInWorld(mob);
                            clientMobs.put(player.getUniqueID(), mob);
                        }
                    }

                    if (mob != null) {
                        // 同步位置和动作
                        mob.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
                        mob.prevPosX = player.prevPosX;
                        mob.prevPosY = player.prevPosY;
                        mob.prevPosZ = player.prevPosZ;
                        mob.lastTickPosX = player.lastTickPosX;
                        mob.lastTickPosY = player.lastTickPosY;
                        mob.lastTickPosZ = player.lastTickPosZ;
                        
                        mob.rotationYawHead = player.rotationYawHead;
                        mob.renderYawOffset = player.renderYawOffset;
                        mob.setSneaking(player.isSneaking());
                        
                        mob.limbSwing = player.limbSwing;
                        mob.limbSwingAmount = player.limbSwingAmount;
                        
                        if (player.isBurning()) mob.setFire(2);
                        else mob.extinguish();
                        
                        // 同步装备
                        for (int slot = 0; slot < 5; slot++) {
                            mob.setCurrentItemOrArmor(slot, player.getEquipmentInSlot(slot));
                        }
                    }
                } else {
                    // 恢复原状时，取消隐形并清理怪物
                    if (player.isInvisible()) {
                        player.setInvisible(false);
                    }
                    if (mob != null) {
                        mob.setDead();
                        mc.theWorld.removeEntity(mob);
                        clientMobs.remove(player.getUniqueID());
                    }
                }
            }
        }
    }
}
