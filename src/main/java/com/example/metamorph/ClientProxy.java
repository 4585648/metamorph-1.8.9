// src/main/java/com/example/metamorph/ClientProxy.java
package com.example.metamorph;

import com.example.metamorph.client.ClientEventHandler;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;

public class ClientProxy extends CommonProxy {

    public static KeyBinding openMenu = new KeyBinding(
            "Open Morph Menu",
            Keyboard.KEY_V,
            "key.categories.misc"
    );

    @Override
    public void init() {
        super.init();

        ClientRegistry.registerKeyBinding(openMenu);

        FMLCommonHandler.instance().bus().register(new ClientEventHandler.KeyHandler());
        FMLCommonHandler.instance().bus().register(new ClientEventHandler.ClientTickHandler());
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler.RenderHandler());
    }
}