package com.sarahk.togglyfiers.listeners;

import java.util.ArrayList;
import java.util.List;

import com.sarahk.togglyfiers.blocks.ModBlocks;

import net.minecraft.block.Block;
import net.minecraft.world.World;


public class ListenerRegister {
	private static List <ListenerBase> ListenerList = new ArrayList<ListenerBase>();
	
	public static ListenerBase GetListeners(World worldIn, Block BlockIn) {
		List <ListenerBase> L = ListenerList;
		if(BlockIn==ModBlocks.togglifyer) {	
			for(int i = 0;i<ListenerList.size();i++) {
				if(ListenerList.get(i)instanceof ToglyfierListeners &&ListenerList.get(i).GetWorld()==worldIn) {
					return ListenerList.get(i);
				}
			}
			ToglyfierListeners newListener=new ToglyfierListeners(worldIn);
			ListenerList.add(newListener);
			return newListener;
		}
		
		if(BlockIn==ModBlocks.changeBlock) {	
			for(int i = 0;i<ListenerList.size();i++) {
				if(ListenerList.get(i)instanceof ChangeBlockListeners &&ListenerList.get(i).GetWorld()==worldIn) {
					return ListenerList.get(i);
				}
			}
			ChangeBlockListeners newListener=new ChangeBlockListeners(worldIn);
			ListenerList.add(newListener);
			return newListener;
		}
		System.out.println("GET LISTENER ERROR : block is not a valid block, returning null");
		return null;
	}
	
	
	

}
