package com.sarahk.togglyfiers;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.items.ModItems;
import com.sarahk.togglyfiers.proxy.CommonProxy;
import com.sarahk.togglyfiers.util.MutableItemStack;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.Random;

@Mod(modid = Main.MOD_ID, name = Main.MOD_NAME, version = Main.VERSION, useMetadata = true, acceptedMinecraftVersions = "[1.12,1.12.2]")
public class Main {
	
	public static final Item togglyfierAssistant = ModItems.togglyfierAssistant;
	public static final Block togglyfier = ModBlocks.togglyfier;
	public static final Block changeBlock = ModBlocks.changeBlock;

    public static final String MOD_ID = "togglyfiers";
    public static final String MOD_NAME = "Togglyfiers";
    public static final String VERSION = "0.0.1";
	
	public static boolean doesServerHaveMod()
	{
		return true;
	}
	
	public static boolean isPlayerOp(final EntityPlayer player)
	{
	    return false;
	}
	
	public static void setWorldManagerSoundStatus(final World world, final boolean b)
	{
	}
	
	@SidedProxy(clientSide = "com.sarahk."+ MOD_ID +".proxy.ClientProxy", serverSide = "com.sarahk."+ MOD_ID +".proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(MOD_ID)
    public static Main instance;

    public static ItemStack dropItemStack(World var0, ItemStack var1, Vec3i pos, final EnumFacing var5)
    {
		if (var1 != null && var1.getCount() > 0 && !var0.isRemote)
		{
			float var2 = pos.getX();
			float var3 = pos.getY();
			float var4 = pos.getZ();
			
			Random var6 = var0.rand;
			float var7 = var6.nextFloat() * 0.8F + 0.1F;
			float var8 = var6.nextFloat() * 0.8F + 0.1F;
			float var9 = var6.nextFloat() * 0.8F + 0.1F;
			float var10 = 0.0F;
			float var11 = 0.0F;
			float var12 = 0.0F;
		
			switch (var5.ordinal())
			{
			case 0:
				var11 = 0.2F;
				break;
		
			case 1:
				var11 = -0.2F;
				break;
		
			case 2:
				var12 = 0.2F;
				break;
		
			case 3:
				var12 = -0.2F;
				break;
		
			case 4:
				var10 = 0.2F;
				break;
		
			case 5:
				var10 = -0.2F;
			}
			
			
		
			EntityItem var13 = new EntityItem(var0, (double)((float)var2 + var7), (double)((float)var3 + var8), (double)((float)var4 + var9), var1.copy());
			float var14 = 0.05F;
			var13.motionX = (double)((float)var6.nextGaussian() * var14 + var10);
			var13.motionY = (double)((float)var6.nextGaussian() * var14 + var11);
			var13.motionZ = (double)((float)var6.nextGaussian() * var14 + var12);
			var0.spawnEntity(var13);
			var1.setCount(0);
		}
		return var1;
    }
	
	public static void dropItemStack(World world, ItemStack stackInSlot, BlockPos pos)
	{
		dropItemStack(world, stackInSlot, pos, EnumFacing.DOWN);
	}
	
	public static void dropItemStack(World world, MutableItemStack stackInSlot, BlockPos pos)
	{
		stackInSlot.set(dropItemStack(world, stackInSlot.toItemStack(), pos, EnumFacing.DOWN));
	}

    public static void notifyPlayer(String s) {
    }

    public static void notifyPlayer(String s, EntityPlayer player) {
    }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
    	
    	ModConfig.loadConfigFile(event.getSuggestedConfigurationFile(), event.getSide());
		

    	proxy.preInit();
    	
    }
    

    @EventHandler
    public void init(FMLInitializationEvent e)
    {
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent e) {
    	
        proxy.postInit();
    
    }
    
    
}


