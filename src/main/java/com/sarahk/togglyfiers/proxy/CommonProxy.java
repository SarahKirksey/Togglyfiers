package com.sarahk.togglyfiers.proxy;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.items.ModItems;
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
