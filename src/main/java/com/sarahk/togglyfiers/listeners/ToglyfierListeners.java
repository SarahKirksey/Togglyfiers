package com.sarahk.togglyfiers.listeners;

import com.sarahk.togglyfiers.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ToglyfierListeners extends ListenerBase {

	public ToglyfierListeners(World worldIn, Block blockIn) {
		super(worldIn, ModBlocks.togglifyer);
	}
	public void OnActivate(){}
	public void OnDeactivate(){}
	public void OnEnterEditMode(){}
	public void OnExitEditMode() {}
	
	@Override
	public void LoadNBT(NBTTagCompound data) {
		// TODO Auto-generated method stub
	}

	@Override
	public void SaveNBT() {
		// TODO Auto-generated method stub
	}
	
	
	
	
}
