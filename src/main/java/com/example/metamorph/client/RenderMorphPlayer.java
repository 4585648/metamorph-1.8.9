package com.example.metamorph.client;

import com.example.metamorph.common.MorphEntry;
import com.example.metamorph.common.MorphManager;
import com.example.metamorph.common.MorphRegistry;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RenderMorphPlayer extends RenderPlayer {

    private final Map<UUID, EntityLivingBase> morphEntities = new HashMap<UUID, EntityLivingBase>();
    private final Map<UUID, Integer> morphEntityIds = new HashMap<UUID, Integer>();

    public RenderMorphPlayer(RenderManager renderManager) {
        super(renderManager);
    }

    // 修正：将参数类型改为 AbstractClientPlayer，以匹配 1.8.9 的 RenderPlayer 方法签名
    @Override
    public void doRender(AbstractClientPlayer player, double x, double y, double z, float entityYaw, float partialTicks) {
        int morphId = MorphManager.getClient(player.getUniqueID());

        if (morphId > 0) {
            MorphEntry entry = MorphRegistry.get(morphId);
            if (entry != null) {
                EntityLivingBase morph = getOrCreateMorphEntity(player, entry.id);
                if (morph != null) {
                    syncFromPlayer(player, morph);
                    
                    Render<Entity> render = this.renderManager.getEntityRenderObject(morph);
                    if (render != null) {
                        render.doRender(morph, x, y, z, entityYaw, partialTicks);
                        return; // 渲染完怪物直接返回，不渲染玩家本体
                    }
                }
            }
        }
        
        // 如果没有变形或渲染失败，正常渲染玩家
        super.doRender(player, x, y, z, entityYaw, partialTicks);
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

        for (int slot = 0; slot < 5; slot++) {
            entity.setCurrentItemOrArmor(slot, player.getEquipmentInSlot(slot));
        }
    }
}
