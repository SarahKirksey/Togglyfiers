package com.sarahk.togglyfiers.client;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static com.sarahk.togglyfiers.items.ModItems.exampleItem;
import static com.sarahk.togglyfiers.items.ModItems.togglificationCore;

public class ModelHandler
{
	
	private static void blockModels()
	{
		register(ModBlocks.exampleBlock);
		register(ModBlocks.togglyfier);
		register(ModBlocks.changeBlock);
	}
	
	@SubscribeEvent
	public static void handleModelRegistry(ModelRegistryEvent event)
	{
		itemModels();
		blockModels();
	}
	
	private static void itemModels()
	{
		register(exampleItem);
		register(togglificationCore);
	}
	
	private static void register(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(item), "inventory"));
	}
	
	private static void register(Item item, int meta, String modelResource)
	{
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation("exampleaddon:" + modelResource, "inventory"));
	}
	
	private static void register(Block block)
	{
		register(Item.getItemFromBlock(block));
	}
}

