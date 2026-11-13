package com.example.metamorph.common;

import com.example.metamorph.MetamorphMod;
import com.example.metamorph.network.MorphSyncMessage;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class MorphApi {

    public static boolean setMorph(EntityPlayerMP player, int morphId, boolean bypassPermission) {
        if (morphId < 0) {
            morphId = 0;
        }

        if (morphId != 0 && MorphRegistry.get(morphId) == null) {
            return false;
        }

        if (!bypassPermission && !canMorph(player, morphId)) {
            return false;
        }

        MorphData data = MorphData.get(player);

        if (data == null) {
            return false;
        }

        int old = data.getMorphId();

        if (old == morphId) {
            MetamorphMod.network.sendToAll(new MorphSyncMessage(player.getUniqueID(), morphId));
            return true;
        }

        MorphEntry oldEntry = MorphRegistry.get(old);
        if (oldEntry != null && oldEntry.ability != null) {
            oldEntry.ability.onMorphEnd(player);
        }

        data.setMorphId(morphId);

        MorphEntry newEntry = MorphRegistry.get(morphId);
        if (newEntry != null && newEntry.ability != null) {
            newEntry.ability.onMorphStart(player);
        }

        applySize(player, morphId);

        MetamorphMod.network.sendToAll(new MorphSyncMessage(player.getUniqueID(), morphId));

        return true;
    }

    public static boolean canMorph(EntityPlayer player, int morphId) {
        if (morphId == 0) {
            return true;
        }

        if (!MorphConfig.isAllowed(morphId)) {
            return false;
        }

        if (MorphConfig.commandOnly && !player.canCommandSenderUseCommand(2, "morph")) {
            return false;
        }

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
        
        // 同时应用血量上限和当前血量
        applyHealth(player, morphId);
    }

    // 新增：根据变形ID应用对应的生命值
    public static void applyHealth(EntityPlayer player, int morphId) {
        float maxHealth = 20.0F; // 默认玩家血量
        if (morphId != 0) {
            maxHealth = getMaxHealthById(morphId);
        }

        IAttributeInstance attribute = player.getEntityAttribute(SharedMonsterAttributes.maxHealth);
        if (attribute != null) {
            attribute.setBaseValue(maxHealth);
            // 如果当前血量大于最大血量，将当前血量设置为最大血量
            if (player.getHealth() > maxHealth) {
                player.setHealth(maxHealth);
            }
        }
    }

    // 根据变形ID获取生物对应的最大血量
    private static float getMaxHealthById(int morphId) {
        switch (morphId) {
            case 54: // Zombie (僵尸)
            case 51: // Skeleton (骷髅)
            case 50: // Creeper (苦力怕)
            case 95: // Wolf (狼)
            case 120: // Villager (村民)
                return 20.0F;
            case 52: // Spider (蜘蛛)
                return 16.0F;
            case 90: // Pig (猪)
            case 92: // Cow (牛)
                return 10.0F;
            case 91: // Sheep (羊)
                return 8.0F;
            case 93: // Chicken (鸡)
                return 4.0F;
            default:
                return 20.0F;
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
