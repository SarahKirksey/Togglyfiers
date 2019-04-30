package com.example.exampleaddon.client;

import jdk.nashorn.internal.objects.NativeJava;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static com.example.exampleaddon.blocks.ModBlocks.*;
import static com.example.exampleaddon.items.ModItems.*;

public class ModelHandler {

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
	
	private static void blockModels()
	{
		register(exampleBlock);
		register(togglifyer);
	}
	
	private static void register(Item item)
	{
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(Item.REGISTRY.getNameForObject(item), "inventory"));
	}
	
	private static void register(Item item, int meta, String modelResource)
	{
		ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation("exampleaddon:"+modelResource, "inventory"));
	}
	
	private static void register(Block block)
	{
		register(Item.getItemFromBlock(block));
	}
}

