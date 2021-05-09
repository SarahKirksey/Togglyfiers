package com.sarahk.togglyfiers.proxy;

import com.sarahk.togglyfiers.ModConfig;
import com.sarahk.togglyfiers.client.ModelHandler;
import com.sarahk.togglyfiers.client.RenderTogglyfier;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
	
	@Override
	public void preInit()
	{
		super.preInit();
		MinecraftForge.EVENT_BUS.register(ModelHandler.class);
	}
	
	@Override
	public void init()
	{
		super.init();
		MinecraftForge.EVENT_BUS.register(new ModConfig());
	}
	
	public static EntityPlayer getClientPlayer()	//Note: can't get the client player directly from FMLClientHandler either, as the server side will still crash because of the return type
	{
		return FMLClientHandler.instance().getClientPlayerEntity();
	}
	
	public static void addScheduledTask(Runnable runnable)
	{
		Minecraft.getMinecraft().addScheduledTask(runnable);
	}
	
	public static void registerRenderers()
	{
		Minecraft mc = Minecraft.getMinecraft();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityTogglyfier.class, new RenderTogglyfier());
	}
}

