package com.example.metamorph.client;

import com.example.metamorph.ClientProxy;
import com.example.metamorph.MetamorphMod;
import com.example.metamorph.common.MorphApi;
import com.example.metamorph.common.MorphConfig;
import com.example.metamorph.common.MorphManager;
import com.example.metamorph.network.MorphRequestMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
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
        @SubscribeEvent
        public void onRenderPlayer(RenderPlayerEvent.Pre event) {
            EntityPlayer player = event.entityPlayer;
            int morphId = MorphManager.getClient(player.getUniqueID());

            if (morphId > 0) {
                // 不改模型，不取消事件，直接把当前渲染管线的纹理绑定替换为原版怪物贴图
                ResourceLocation texture = getMorphTexture(morphId);
                if (texture != null) {
                    Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
                }
            }
        }

        // 对应的原版怪物贴图路径
        private ResourceLocation getMorphTexture(int morphId) {
            switch (morphId) {
                case 54: return new ResourceLocation("textures/entity/zombie/zombie.png");
                case 51: return new ResourceLocation("textures/entity/skeleton/skeleton.png");
                case 50: return new ResourceLocation("textures/entity/creeper/creeper.png");
                case 58: return new ResourceLocation("textures/entity/enderman/enderman.png");
                case 90: return new ResourceLocation("textures/entity/pig/pig.png");
                case 91: return new ResourceLocation("textures/entity/sheep/sheep.png");
                case 92: return new ResourceLocation("textures/entity/cow/cow.png");
                case 93: return new ResourceLocation("textures/entity/chicken.png");
                default: return null;
            }
        }
    }
}
