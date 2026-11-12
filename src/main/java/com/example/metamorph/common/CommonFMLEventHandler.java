// src/main/java/com/example/metamorph/common/CommonFMLEventHandler.java
package com.example.metamorph.common;

import com.example.metamorph.MetamorphMod;
import com.example.metamorph.network.MorphSyncMessage;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

public class CommonFMLEventHandler {

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.player instanceof EntityPlayerMP)) {
            return;
        }

        EntityPlayerMP player = (EntityPlayerMP) event.player;

        MorphData data = MorphData.get(player);
        int morphId = data == null ? 0 : data.getMorphId();

        if (morphId != 0 && MorphRegistry.get(morphId) == null) {
            morphId = 0;

            if (data != null) {
                data.setMorphId(0);
            }
        }

        MorphApi.applySize(player, morphId);

        MetamorphMod.network.sendToAll(new MorphSyncMessage(player.getUniqueID(), morphId));

        List<EntityPlayerMP> online =
                new ArrayList<EntityPlayerMP>(player.mcServer.getConfigurationManager().playerEntityList);

        for (EntityPlayerMP other : online) {
            if (other != player) {
                int otherId = MorphApi.getMorphId(other);
                MetamorphMod.network.sendTo(new MorphSyncMessage(other.getUniqueID(), otherId), player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.player instanceof EntityPlayerMP) {
            MetamorphMod.network.sendToAll(new MorphSyncMessage(event.player.getUniqueID(), 0));
        }
    }
}