package com.sarahk.togglyfiers.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import javax.annotation.Nonnull;

public class ModBlocks
{
	
	@SubscribeEvent
	public static void registerBlocks(RegistryEvent.Register<Block> event)
	{
		IForgeRegistry<Block> registry = event.getRegistry();
		//blocks
		registry.register(exampleBlock);
		registry.register(togglyfier);
		registry.register(changeBlock);
	}
	
	@Nonnull public static final Block exampleBlock =
		new Block(Material.CLAY).setRegistryName("example_block").setUnlocalizedName("exampleBlock").setCreativeTab(CreativeTabs.MISC);
	@Nonnull public static final BlockTogglyfier togglyfier = new BlockTogglyfier();
	@Nonnull public static final BlockChangeBlock changeBlock = new BlockChangeBlock();
	
}
