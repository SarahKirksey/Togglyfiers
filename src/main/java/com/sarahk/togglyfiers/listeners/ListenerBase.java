package com.sarahk.togglyfiers.listeners;

import java.util.ArrayList;
import java.util.List;

import com.sarahk.togglyfiers.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class ListenerBase {
	//we will keep track of each block by it's position
	private List<BlockPos> TogglyfierList= new ArrayList<BlockPos>();
	private World world;
	private Block blockType;

	
	public ListenerBase(World worldIn, Block blockIn) {
		this.world=worldIn;
		this.blockType=blockIn;
	}
	public void OnPlaced(BlockPos pos) {
		TogglyfierList.add(pos);
	}

	public void OnDestroied(BlockPos pos) {
		TogglyfierList.remove(pos);
	}

	public void OnTogglyfierActivate() {}
	public void OnTogglyfierDeactivate() {}
	
	public void OnWorldTick() {
		//ping each togglyfier
		for(BlockPos tPos:TogglyfierList) {
			if(this.world.getBlockState(tPos).getBlock()==this.blockType ) {
				OnDestroied(tPos);
			}
		}
	}
	public abstract void LoadNBT(NBTTagCompound data);
	public abstract void SaveNBT();
}
