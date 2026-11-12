// src/main/java/com/example/metamorph/CommonProxy.java
package com.example.metamorph;

import com.example.metamorph.common.CommonFMLEventHandler;
import com.example.metamorph.common.CommonForgeEventHandler;
import com.example.metamorph.network.MorphRequestMessage;
import com.example.metamorph.network.MorphSyncMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

    public void init() {
        MinecraftForge.EVENT_BUS.register(new CommonForgeEventHandler());
        FMLCommonHandler.instance().bus().register(new CommonFMLEventHandler());
    }

    public void registerMessages() {
        MetamorphMod.network.registerMessage(
                MorphRequestMessage.Handler.class,
                MorphRequestMessage.class,
                0,
                Side.SERVER
        );

        MetamorphMod.network.registerMessage(
                MorphSyncMessage.Handler.class,
                MorphSyncMessage.class,
                1,
                Side.CLIENT
        );
    }
}