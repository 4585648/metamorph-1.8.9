package com.example.metamorph.common;

import com.example.metamorph.MetamorphMod;
import com.example.metamorph.network.MorphSyncMessage;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class MorphApi {

    public static boolean setMorph(EntityPlayerMP player, int morphId, boolean bypassPermission) {
        if (morphId < 0) morphId = 0;

        if (morphId != 0 && MorphRegistry.get(morphId) == null) return false;
        if (!bypassPermission && !canMorph(player, morphId)) return false;

        MorphData data = MorphData.get(player);
        if (data == null) return false;

        // 拦截未解锁的生物（除非是 0 或强制绕过）
        if (!bypassPermission && morphId != 0 && !data.isUnlocked(morphId)) {
            player.addChatMessage(new net.minecraft.util.ChatComponentText("§c你还没有解锁这个生物！"));
            return false;
        }

        int old = data.getMorphId();
        if (old == morphId) {
            MetamorphMod.network.sendToAll(new MorphSyncMessage(player.getUniqueID(), morphId));
            return true;
        }

        MorphEntry oldEntry = MorphRegistry.get(old);
        if (oldEntry != null && oldEntry.ability != null) oldEntry.ability.onMorphEnd(player);

        data.setMorphId(morphId);

        MorphEntry newEntry = MorphRegistry.get(morphId);
        if (newEntry != null && newEntry.ability != null) newEntry.ability.onMorphStart(player);

        applySize(player, morphId);
        MetamorphMod.network.sendToAll(new MorphSyncMessage(player.getUniqueID(), morphId));
        return true;
    }

    public static boolean canMorph(EntityPlayer player, int morphId) {
        if (morphId == 0) return true;
        if (!MorphConfig.isAllowed(morphId)) return false;
        if (MorphConfig.commandOnly && !player.canCommandSenderUseCommand(2, "morph")) return false;
        return true;
    }

    public static int getMorphId(EntityPlayer player) {
        MorphData data = MorphData.get(player);
        return data == null ? 0 : data.getMorphId();
    }

    public static void applySize(EntityPlayer player, int morphId) {
        MorphEntry entry = MorphRegistry.get(morphId);
        if (morphId == 0 || entry == null) {
            setSizeReflect(player, 0.6F, 1.8F);
        } else {
            setSizeReflect(player, entry.width, entry.height);
        }
        applyHealth(player, morphId);
    }

    public static void applyHealth(EntityPlayer player, int morphId) {
        float maxHealth = 20.0F;
        if (morphId != 0) maxHealth = getMaxHealthById(morphId);

        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        if (attribute != null) {
            attribute.setBaseValue(maxHealth);
            if (player.getHealth() > maxHealth) player.setHealth(maxHealth);
        }
    }

    private static float getMaxHealthById(int morphId) {
        switch (morphId) {
            case 58: return 40.0F;
            case 54: case 51: case 50: case 95: case 120: case 61: return 20.0F;
            case 52: return 16.0F;
            case 90: case 92: case 94: case 56: return 10.0F;
            case 91: return 8.0F;
            case 65: return 6.0F;
            case 93: return 4.0F;
            default: return 20.0F;
        }
    }
    
    private static void setSizeReflect(EntityPlayer player, float width, float height) {
        try {
            java.lang.reflect.Method method;
            try {
                method = net.minecraft.entity.Entity.class.getDeclaredMethod("func_70105_a", float.class, float.class);
            } catch (NoSuchMethodException e) {
                method = net.minecraft.entity.Entity.class.getDeclaredMethod("setSize", float.class, float.class);
            }
            method.setAccessible(true);
            method.invoke(player, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
