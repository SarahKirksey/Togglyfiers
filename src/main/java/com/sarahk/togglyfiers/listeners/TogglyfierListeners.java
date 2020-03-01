package com.sarahk.togglyfiers.listeners;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TogglyfierListeners extends ListenerBase
{
	
	public TogglyfierListeners(World worldIn)
	{
		super(worldIn, ModBlocks.togglyfier);
	}
	
	@Override
	public void LoadNBT(NBTTagCompound data)
	{
		// TODO Auto-generated method stub
	}
	
	@Override
	public void SaveNBT()
	{
		// TODO Auto-generated method stub
	}
	
	public void OnActivate(BlockPos pos)
	{
		System.out.println(ModBlocks.togglyfier + " Activated at " + pos);
	}
	
	public void OnDeactivate(BlockPos pos)
	{
		System.out.println(ModBlocks.togglyfier + " Deactivated at " + pos);
	}
	
	public void OnEnterEditMode(BlockPos pos)
	{
		System.out.println(ModBlocks.togglyfier + " Entered Edit Mode at " + pos);
	}
	
	public void OnExitEditMode(BlockPos pos)
	{
		System.out.println(ModBlocks.togglyfier + " Exited Edit Mode at " + pos);
	}
	
	
}
