package com.sarahk.togglyfiers.listeners;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;

public abstract class ListenerBase
{
	//each listener is specific to one World.
	protected World world;
	//we will keep track of each block by it's position.
	protected final Collection<BlockPos> blockList = new ArrayList<BlockPos>();
	protected final Block blockType;
	
	public ListenerBase(World worldIn, Block blockIn)
	{
		this.world = worldIn;
		this.blockType = blockIn;
	}
	
	World GetWorld()
	{
		return world;
	}
	
	public abstract void LoadNBT(NBTTagCompound data);
	
	public void OnDestroyed(BlockPos pos)
	{
		blockList.remove(pos);
		System.out.println(blockType + " destroyed at " + pos);
	}
	
	public void OnPlaced(BlockPos pos)
	{
		blockList.add(pos);
		System.out.println(blockType + " placed at " + pos);
	}
	
	public void OnWorldTick()
	{
		//ping each togglyfier
		for(BlockPos tPos : blockList)
		{
			if(this.world.getBlockState(tPos).getBlock()==this.blockType)
			{
				OnDestroyed(tPos);
			}
		}
		System.out.println("WorldTicked");
	}

	public abstract void SaveNBT();
}
