package com.example.metamorph.client;

import com.example.metamorph.ClientProxy;
import com.example.metamorph.MetamorphMod;
import com.example.metamorph.common.MorphApi;
import com.example.metamorph.common.MorphConfig;
import com.example.metamorph.common.MorphEntry;
import com.example.metamorph.common.MorphManager;
import com.example.metamorph.common.MorphRegistry;
import com.example.metamorph.network.MorphRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
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
        private final Map<UUID, Integer> lastMorph = new HashMap<UUID, Integer>();

        @SubscribeEvent
        public void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) return;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.theWorld == null) {
                lastMorph.clear();
                return;
            }
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                int morphId = MorphManager.getClient(player.getUniqueID());
                Integer last = lastMorph.get(player.getUniqueID());
                if (last == null || last.intValue() != morphId) {
                    MorphApi.applySize(player, morphId);
                    lastMorph.put(player.getUniqueID(), morphId);
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class RenderHandler {
        private final Map<UUID, EntityLivingBase> morphEntities = new HashMap<UUID, EntityLivingBase>();
        private final Map<UUID, Integer> morphEntityIds = new HashMap<UUID, Integer>();

        @SubscribeEvent
        public void onRenderPlayer(RenderPlayerEvent.Pre event) {
            EntityPlayer player = event.entityPlayer;
            int morphId = MorphManager.getClient(player.getUniqueID());

            if (morphId <= 0) {
                clearCache(player.getUniqueID());
                return;
            }

            MorphEntry entry = MorphRegistry.get(morphId);
            if (entry == null) {
                clearCache(player.getUniqueID());
                return;
            }

            EntityLivingBase morph = getOrCreateMorphEntity(player, entry.id);
            if (morph == null) return;

            syncFromPlayer(player, morph);
            
            // 核心：取消玩家渲染，直接借用原版的 Render 画怪物
            event.setCanceled(true);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            Render<Entity> render = renderManager.getEntityRenderObject(morph);
            
            if (render != null) {
                try {
                    render.doRender(morph, 
                        player.posX - renderManager.viewerPosX, 
                        player.posY - renderManager.viewerPosY, 
                        player.posZ - renderManager.viewerPosZ, 
                        player.rotationYaw, 
                        event.partialRenderTick);
                } catch (Exception e) {
                    // 如果光影报错，什么都不做，直接跳过这一帧
                }
            }
        }

        private void clearCache(UUID uuid) {
            EntityLivingBase old = morphEntities.remove(uuid);
            morphEntityIds.remove(uuid);
            if (old != null) old.setDead();
        }

        private EntityLivingBase getOrCreateMorphEntity(EntityPlayer player, int morphId) {
            UUID uuid = player.getUniqueID();
            Integer oldId = morphEntityIds.get(uuid);
            EntityLivingBase cached = morphEntities.get(uuid);

            if (cached != null && oldId != null && oldId == morphId) return cached;
            if (cached != null) cached.setDead();

            Entity entity = EntityList.createEntityByID(morphId, player.worldObj);
            if (!(entity instanceof EntityLivingBase)) return null;

            EntityLivingBase living = (EntityLivingBase) entity;
            living.setEntityId(-100 - Math.abs(uuid.hashCode() % 10000));
            
            morphEntities.put(uuid, living);
            morphEntityIds.put(uuid, morphId);
            return living;
        }

        private void syncFromPlayer(EntityPlayer player, EntityLivingBase entity) {
            entity.worldObj = player.worldObj;
            entity.setPositionAndRotation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);

            entity.prevPosX = player.prevPosX;
            entity.prevPosY = player.prevPosY;
            entity.prevPosZ = player.prevPosZ;
            entity.lastTickPosX = player.lastTickPosX;
            entity.lastTickPosY = player.lastTickPosY;
            entity.lastTickPosZ = player.lastTickPosZ;

            entity.rotationYaw = player.rotationYaw;
            entity.prevRotationYaw = player.rotationYaw;
            entity.rotationPitch = player.rotationPitch;
            entity.prevRotationPitch = player.rotationPitch;
            entity.rotationYawHead = player.rotationYawHead;
            entity.prevRotationYawHead = player.rotationYawHead;
            entity.renderYawOffset = player.renderYawOffset;
            entity.prevRenderYawOffset = player.renderYawOffset;

            entity.setSneaking(player.isSneaking());
            entity.setSprinting(player.isSprinting());
            entity.setInvisible(player.isInvisible());

            entity.limbSwing = player.limbSwing;
            entity.limbSwingAmount = player.limbSwingAmount;
            entity.prevLimbSwingAmount = player.limbSwingAmount;
            entity.swingProgress = player.swingProgress;
            entity.prevSwingProgress = player.swingProgress;

            entity.hurtTime = player.hurtTime;
            entity.deathTime = player.deathTime;

            if (player.isBurning()) entity.setFire(2);
            else entity.extinguish();
            
            entity.ticksExisted = player.ticksExisted;

            if (entity.getClass().getName().contains("Enderman") || 
                entity.getClass().getName().contains("Creeper") || 
                entity.getClass().getName().contains("Slime") || 
                entity.getClass().getName().contains("Squid") ||
                entity.getClass().getName().contains("Ghast")) {
                return;
            }

            for (int slot = 0; slot < 5; slot++) {
                entity.setCurrentItemOrArmor(slot, player.getEquipmentInSlot(slot));
            }
        }
    }
            }
