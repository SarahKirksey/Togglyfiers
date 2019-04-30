package com.example.exampleaddon;

import com.example.exampleaddon.blocks.ModBlocks;
import com.example.exampleaddon.items.ModItems;
import com.example.exampleaddon.proxy.CommonProxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Main.MODID, name = Main.MODNAME, version = Main.VERSION, useMetadata = true,acceptedMinecraftVersions = "[1.12,1.12.2]")
public class Main {

    public static final String MODID = "exampleaddon";
    public static final String MODNAME = "Example Addon";
    public static final String VERSION = "0.0.1";

    @SidedProxy(clientSide = "com.example.exampleaddon.proxy.ClientProxy", serverSide = "com.example.exampleaddon.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MODID)
    public static Main instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	
    	ModConfig.loadConfigFile(event.getSuggestedConfigurationFile(), event.getSide());
		

    	proxy.preInit();
    	
    }
    

    @EventHandler
    public void init(FMLInitializationEvent e) {
    	
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    	
        proxy.postInit();
    
    }
    
    
}


