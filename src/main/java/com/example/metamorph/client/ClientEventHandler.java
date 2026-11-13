package com.example.metamorph.client;

import com.example.metamorph.ClientProxy;
import com.example.metamorph.MetamorphMod;
import com.example.metamorph.common.MorphApi;
import com.example.metamorph.common.MorphConfig;
import com.example.metamorph.common.MorphManager;
import com.example.metamorph.network.MorphRequestMessage;
import net.minecraft.client.Minecraft;
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
}
