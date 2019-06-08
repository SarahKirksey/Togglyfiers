package com.sarahk.togglyfiers.listeners;

import com.sarahk.togglyfiers.blocks.ModBlocks;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ToglyfierListeners extends ListenerBase {

	public ToglyfierListeners(World worldIn) {
		super(worldIn, ModBlocks.togglifyer);
	}
	public void OnActivate(BlockPos pos){
		System.out.println(ModBlocks.togglifyer+" Activated at "+pos);
	}
	public void OnDeactivate(BlockPos pos){
		System.out.println(ModBlocks.togglifyer+" Deactivated at "+pos);
	}
	public void OnEnterEditMode(BlockPos pos){
		System.out.println(ModBlocks.togglifyer+" Entered Edit Mode at "+pos);
	}
	public void OnExitEditMode(BlockPos pos) {
		System.out.println(ModBlocks.togglifyer+" Exited Edit Mode at "+pos);
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
