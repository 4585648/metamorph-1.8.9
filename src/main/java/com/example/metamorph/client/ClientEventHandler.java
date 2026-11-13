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
            event.setCanceled(true);

            RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
            
            // 使用原生的 Render 接口（不加泛型），避免编译报错
            Render render = renderManager.getEntityRenderObject(morph);
            
            if (render != null) {
                try {
                    // 直接调用 doRender，传递正确的相对坐标，可渲染末影人、蜘蛛等特殊模型的附加层
                    render.doRender(morph, 
                            player.posX - renderManager.viewerPosX, 
                            player.posY - renderManager.viewerPosY, 
                            player.posZ - renderManager.viewerPosZ, 
                            player.rotationYaw, 
                            event.partialRenderTick);
                } catch (Exception e) {
                    // 兜底防崩
                    renderManager.renderEntitySimple(morph, event.partialRenderTick);
                }
            } else {
                renderManager.renderEntitySimple(morph, event.partialRenderTick);
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
            morphEntities.put(uuid, living);
            morphEntityIds.put(uuid, morphId);
            return living;
        }

        private void syncFromPlayer(EntityPlayer player, EntityLivingBase entity) {
            // 极其重要：必须给实体分配世界实例，否则末影人等实体无法渲染
            entity.worldObj = player.worldObj;
            
            entity.setPosition(player.posX, player.posY, player.posZ);

            entity.prevPosX = player.prevPosX;
            entity.prevPosY = player.prevPosY;
            entity.prevPosZ = player.prevPosZ;
            entity.lastTickPosX = player.lastTickPosX;
            entity.lastTickPosY = player.lastTickPosY;
            entity.lastTickPosZ = player.lastTickPosZ;

            entity.rotationYaw = player.rotationYaw;
            entity.prevRotationYaw = player.prevRotationYaw;
            entity.rotationPitch = player.rotationPitch;
            entity.prevRotationPitch = player.prevRotationPitch;
            entity.rotationYawHead = player.rotationYawHead;
            entity.prevRotationYawHead = player.prevRotationYawHead;
            entity.renderYawOffset = player.renderYawOffset;
            entity.prevRenderYawOffset = player.prevRenderYawOffset;

            entity.setSneaking(player.isSneaking());
            entity.setSprinting(player.isSprinting());
            entity.setInvisible(player.isInvisible());

            entity.limbSwing = player.limbSwing;
            entity.limbSwingAmount = player.limbSwingAmount;
            entity.prevLimbSwingAmount = player.prevLimbSwingAmount;
            entity.swingProgress = player.swingProgress;
            entity.prevSwingProgress = player.prevSwingProgress;

            entity.hurtTime = player.hurtTime;
            entity.deathTime = player.deathTime;

            if (player.isBurning()) entity.setFire(2);
            else entity.extinguish();
            
            entity.ticksExisted = player.ticksExisted;

            // 针对无手部模型的特殊实体，跳过装备同步
            if (entity.getClass().getName().contains("Enderman") || 
                entity.getClass().getName().contains("Creeper") || 
                entity.getClass().getName().contains("Slime") || 
                entity.getClass().getName().contains("Squid") ||
                entity.getClass().getName().contains("Ghast")) {
                return;
            }

            try {
                for (int slot = 0; slot < 5; slot++) {
                    entity.setCurrentItemOrArmor(slot, player.getEquipmentInSlot(slot));
                }
            } catch (Exception ignored) {}
        }
    }
}
