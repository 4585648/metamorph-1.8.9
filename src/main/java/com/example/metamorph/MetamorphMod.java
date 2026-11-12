// src/main/java/com/example/metamorph/MetamorphMod.java
package com.example.metamorph;

import com.example.metamorph.command.CommandMorph;
import com.example.metamorph.common.MorphConfig;
import com.example.metamorph.common.MorphRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(
        modid = MetamorphMod.MODID,
        name = MetamorphMod.NAME,
        version = MetamorphMod.VERSION,
        acceptableRemoteVersions = "*"
)
public class MetamorphMod {

    public static final String MODID = "metamorph";
    public static final String NAME = "Metamorph";
    public static final String VERSION = "1.0";

    @Mod.Instance(MODID)
    public static MetamorphMod instance;

    @SidedProxy(
            clientSide = "com.example.metamorph.ClientProxy",
            serverSide = "com.example.metamorph.CommonProxy"
    )
    public static CommonProxy proxy;

    public static SimpleNetworkWrapper network;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MorphConfig.load(event.getSuggestedConfigurationFile());
        MorphRegistry.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        network = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);

        proxy.registerMessages();
        proxy.init();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandMorph());
    }
}