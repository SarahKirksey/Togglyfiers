package com.example.exampleaddon.proxy;

import com.example.exampleaddon.blocks.ModBlocks;
import com.example.exampleaddon.items.ModItems;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

	public void preInit()
	{
		MinecraftForge.EVENT_BUS.register(ModBlocks.class);
		MinecraftForge.EVENT_BUS.register(ModItems.class);
	}
	
	public void init()
	{
	
	}
	
	public void postInit()
	{
		
	}
}
