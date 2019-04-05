package com.example.exampleaddon.proxy;

import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	
	@Override
	public void preInit() {
		MinecraftForge.EVENT_BUS.register(ModelManager.class);
	}
	
	@Override
	public void init() {
	
	}
	
	
}

