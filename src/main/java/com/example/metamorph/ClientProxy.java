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

    public static KeyBinding skillKey = new KeyBinding(
            "Trigger Morph Skill",
            Keyboard.KEY_G,
            "key.categories.misc"
    );

    @Override
    public void init() {
        // 调用父类（CommonProxy）的初始化，注册服务端相关的事件
        super.init();

        // 注册按键绑定
        ClientRegistry.registerKeyBinding(openMenu);
        ClientRegistry.registerKeyBinding(skillKey);

        // 注册 FML 事件（按键输入、客户端 Tick）
        FMLCommonHandler.instance().bus().register(new ClientEventHandler.KeyHandler());
        FMLCommonHandler.instance().bus().register(new ClientEventHandler.ClientTickHandler());
        
        // 注册 Forge 事件（渲染事件，用于替换贴图）
        MinecraftForge.EVENT_BUS.register(new ClientEventHandler.RenderHandler());
    }
}
