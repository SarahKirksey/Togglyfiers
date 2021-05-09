package com.sarahk.togglyfiers.items;

import com.sarahk.togglyfiers.ModConfig;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemTogglyfier extends ItemBlock
{
	public static final String[] togglyfierRefNames = new String[] {"tiny", "small", "mediumiron", "mediumgold", "largeiron", "largegold"};
	
	public ItemTogglyfier(Block var1)
	{
		super(var1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.maxStackSize = ModConfig.getBoolean(ModConfig.TogglyfierOptions.STACK_TOGGLE_BLOCKS) ? 16 : 1;
	}
	
	/**
	 * Returns the metadata of the block which this Item (ItemBlock) can place
	 */
	@Override
	public int getMetadata(int var1)
	{
		return var1;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack var1)
	{
		return "togglyfier." + togglyfierRefNames[var1.getItemDamage()%togglyfierRefNames.length];
	}
}
