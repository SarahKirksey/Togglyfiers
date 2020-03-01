package com.sarahk.togglyfiers.listeners;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;


public final class ListenerRegister
{
	private static final Collection<TogglyfierListeners> TOGGLYFIER_LISTENERS = new ArrayList<>();
	private static final Collection<ChangeBlockListeners> CHANGE_BLOCK_LISTENERS = new ArrayList<>();
	
	public static ListenerBase GetListeners(World worldIn, Block blockIn)
	{
		ListenerBase result = null;
		if(blockIn==ModBlocks.togglyfier)
		{
			result = getListenerFromList(worldIn, TOGGLYFIER_LISTENERS);
			if(result == null)
			{
				TogglyfierListeners newListener = new TogglyfierListeners(worldIn);
				TOGGLYFIER_LISTENERS.add(newListener);
				result = newListener;
			}
		}
		else if(blockIn==ModBlocks.changeBlock)
		{
			result = getListenerFromList(worldIn, CHANGE_BLOCK_LISTENERS);
			if(result == null)
			{
				ChangeBlockListeners newListener = new ChangeBlockListeners(worldIn);
				CHANGE_BLOCK_LISTENERS.add(newListener);
				result = newListener;
			}
		}
		else
		{
			System.out.println("GET LISTENER ERROR : block is not a valid block, returning null");
		}
		
		return result;
	}
	
	private static <T extends ListenerBase> T getListenerFromList(World worldIn, Iterable<T> list)
	{
		T result = null;
		for(T listenerBase : list)
		{
			if(listenerBase!=null && listenerBase.GetWorld()==worldIn)
			{
				result = listenerBase;
				break;
			}
		}
		return result;
	}
}
