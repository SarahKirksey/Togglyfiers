package com.sarahk.togglyfiers.util;

import com.sarahk.togglyfiers.tileentity.TileEntityChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{
	public enum GuiId
	{
		TOGGLYFIER_EDIT,
		TOGGLYFIER_READY,
		CHANGE_BLOCK,
	}
	
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if(tileEntity instanceof TileEntityTogglyfier)
		{
			if(id==GuiId.TOGGLYFIER_EDIT.ordinal() || id==GuiId.TOGGLYFIER_READY.ordinal())
			{
				return new TileEntityTogglyfier.ContainerTogglyfier(player.inventory, (TileEntityTogglyfier) tileEntity, ((TileEntityTogglyfier) tileEntity).isReady());
			}
		}
		else if(id == GuiId.CHANGE_BLOCK.ordinal() && tileEntity instanceof TileEntityChangeBlock)
		{
			return new TileEntityChangeBlock.ContainerChangeBlock(player.inventory, (TileEntityChangeBlock) tileEntity);
		}
		return null;
	}
	
	//returns an instance of the Gui you made earlier
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world,
									  int x, int y, int z)
	{
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if(tileEntity instanceof TileEntityTogglyfier && (id==GuiId.TOGGLYFIER_EDIT.ordinal() && id==GuiId.TOGGLYFIER_READY.ordinal()))
		{
			return new TileEntityTogglyfier.ContainerTogglyfier(player.inventory, (TileEntityTogglyfier) tileEntity, ((TileEntityTogglyfier) tileEntity).isReady());
		}
		else if(id==GuiId.CHANGE_BLOCK.ordinal() && tileEntity instanceof TileEntityChangeBlock)
		{
			return new TileEntityChangeBlock.ContainerChangeBlock(player.inventory, (TileEntityChangeBlock) tileEntity);
		}
		
		return null;
	}
}
