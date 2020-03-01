package com.sarahk.togglyfiers.items;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

public class ModItems
{
	
	private static Item registerItemBlock(IForgeRegistry<Item> registry, ItemBlock item)
	{
		registry.register(item.setRegistryName(item.getBlock().getRegistryName()));
		return item;
	}

	private static Item registerItemBlock(IForgeRegistry<Item> registry, Block block) {return registerItemBlock(registry, new ItemBlock(block));}

	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event)
	{
		IForgeRegistry<Item> registry = event.getRegistry();
		
		//ItemBlocks
		registerItemBlock(registry, new ItemBlock(ModBlocks.exampleBlock));
		registerItemBlock(registry, ModBlocks.togglyfier);
		registerItemBlock(registry, ModBlocks.changeBlock);
		
		registry.register(exampleItem);
		registry.register(togglificationCore);
		
	}
	public static final Item exampleItem = new Item().setRegistryName("example_item").setUnlocalizedName("exampleItem").setCreativeTab(CreativeTabs.MISC);
	public static final Item togglificationCore = new Item().setRegistryName("togglification_core").setUnlocalizedName("togglificationCore").setCreativeTab(CreativeTabs.MATERIALS);
	public static final Item togglyfierAssistant = new Item().setRegistryName("togglifier_assistant").setUnlocalizedName("togglifierAssistant");
}
