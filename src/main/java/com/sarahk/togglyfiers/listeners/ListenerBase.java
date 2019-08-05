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
	protected World world;
	private Block blockType;


	
	public ListenerBase(World worldIn, Block blockIn) {
		this.world=worldIn;
		this.blockType=blockIn;
	}
	public void OnPlaced(BlockPos pos) {
		TogglyfierList.add(pos);
		System.out.println(blockType+" placed at "+pos);
	}

	public void OnDestroied(BlockPos pos) {
		TogglyfierList.remove(pos);
		System.out.println(blockType+" destroyed at "+pos);
	}	
	public void OnWorldTick() {
		//ping each togglyfier
		for(BlockPos tPos:TogglyfierList) {
			if(this.world.getBlockState(tPos).getBlock()==this.blockType ) {
				OnDestroied(tPos);
			}
		}
		System.out.println("WorldTicked");
	}
	public World GetWorld() {
		return world;
	}
	public abstract void LoadNBT(NBTTagCompound data);
	public abstract void SaveNBT();
}
