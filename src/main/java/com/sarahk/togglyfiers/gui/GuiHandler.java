package com.sarahk.togglyfiers.gui;

import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler
{
	public static final int gui_togglyfier = 0;
	public static final int gui_change_block = 1;
	
	
	@Nullable
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		switch(ID)
		{
		case gui_togglyfier:
			return new TileEntityTogglyfier.ContainerTogglyfier(player.inventory, (TileEntityTogglyfier) world.getTileEntity(new BlockPos(x, y, z)));
		default:
			return null;
		}
	}
	
	@Nullable
	@Override
	public GuiTogglyfier getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		switch(ID)
		{
		case gui_togglyfier:
			TileEntityTogglyfier te = (TileEntityTogglyfier) world.getTileEntity(new BlockPos(x, y, z));
			assert te!=null;
			return new GuiTogglyfier(player.inventory, te, te.isReady());
		default:
			return null;
		}
	}
}
