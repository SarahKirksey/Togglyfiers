package com.sarahk.togglyfiers.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModBlocks {
	
	public static Block exampleBlock = new Block(Material.CLAY).setRegistryName("example_block").setUnlocalizedName("exampleBlock").setCreativeTab(CreativeTabs.MISC);
	public static Block togglifyer = new BlockTogglyfier();
	public static Block changeBlock = new BlockChangeBlock();

	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();
		//blocks
		registry.register(exampleBlock);
		registry.register(togglifyer);
		registry.register(changeBlock);
	}

}
