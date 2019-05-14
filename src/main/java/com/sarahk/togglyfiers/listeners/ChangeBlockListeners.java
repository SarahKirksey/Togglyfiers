package com.sarahk.togglyfiers.listeners;

import com.sarahk.togglyfiers.blocks.BlockTogglyfier;
import com.sarahk.togglyfiers.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ChangeBlockListeners extends ListenerBase {

	public ChangeBlockListeners(World worldIn) {
		super(worldIn, ModBlocks.changeBlock);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void LoadNBT(NBTTagCompound data) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void SaveNBT() {
		// TODO Auto-generated method stub
		
	}

}
