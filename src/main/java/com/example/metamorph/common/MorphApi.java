package com.example.metamorph.common;

import com.example.metamorph.MetamorphMod;
import com.example.metamorph.network.MorphSyncMessage;
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
    }
    
    private static void setSizeReflect(EntityPlayer player, float width, float height) {
        try {
            java.lang.reflect.Method method = net.minecraft.entity.Entity.class.getDeclaredMethod("setSize", float.class, float.class);
            method.setAccessible(true);
            method.invoke(player, width, height);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
