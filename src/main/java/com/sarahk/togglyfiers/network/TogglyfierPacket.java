package com.sarahk.togglyfiers.network;

import com.sarahk.togglyfiers.blocks.BlockTogglyfier;
import com.sarahk.togglyfiers.gui.GuiTogglyfier;
import com.sarahk.togglyfiers.tileentity.TileEntityChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;

import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.EnumSet;

//TODO: Everything
public interface TogglyfierPacket
{
	ByteBuf data = Unpooled.buffer();
	
	static String readLine(ByteBuf data)
	{
		StringBuilder str = new StringBuilder();
		while(data.readableBytes() > 0)
		{
			char c = data.readChar();
			if(c == '\n')
			{
				break;
			}
			else
			{
				str.append(c);
			}
		}
		
		return str.toString();
	}
	
	static void writeString(ByteBuf data, String str)
	{
		for(int i = 0; i < str.length(); i++)
		{
			data.writeChar(str.charAt(i));
		}
	}
	
	TogglyfierPacket generatePacket(Object... data);
	TogglyfierPacket consumePacket(ByteBuf data);
	void execute(EntityPlayer player);
	
	default EnumSet<Side> getSenderSide()
	{
		return EnumSet.allOf(Side.class);
	}
	
	//byte[] getBufferData();

//	public TogglyfierPacket(@NotNull final String togglyfiers, @NotNull final byte[] toByteArray)
//	{
//		super(togglyfiers, new PacketBuffer(Unpooled.buffer().writeBytes(toByteArray)));
//	}
	
	class TogglyfierInventoryChangedPacket implements TogglyfierPacket
	{
		int type;
		boolean b1, b2;
		
		public TogglyfierInventoryChangedPacket() {}
		
		public TogglyfierInventoryChangedPacket(final String channel, final byte[] dataIn)
		{
			for(byte datum : dataIn)
			{
				this.data.writeByte(datum);
			}
		}
		
		public ArrayList<ItemStack> inventory;
		
		@Override
		public TogglyfierPacket generatePacket(Object... data)
		{
			this.data.writeByte((Integer) data[0]);
			if(data[1] instanceof ArrayList)
			{
				this.data.writeBoolean((Boolean) data[2]);
				this.data.writeBoolean((Boolean) data[3]);
				ArrayList<ItemStack> list = (ArrayList<ItemStack>) data[1];
				for(ItemStack stack : list)
				{
					ByteBufUtils.writeItemStack(this.data, stack);
				}
			} else
			{
				this.data.writeBoolean((Boolean) data[1]);
			}
			
			return this;
		}
		
		@Override
		public TogglyfierPacket consumePacket(ByteBuf data)
		{
			this.type = data.readByte();
			
			if(data.readableBytes() == 1)
			{
				b1 = data.readBoolean();
			}
			else
			{
				b1 = data.readBoolean();
				b2 = data.readBoolean();
				inventory = new ArrayList<ItemStack>();
				while(data.readableBytes() > 0)
				{
					inventory.add(ByteBufUtils.readItemStack(data));
				}
			}
			return this;
		}
		
		@Override
		public void execute(EntityPlayer player)
		{
			if(type==0)
			{
				if(player.world.isRemote && this.inventory!=null && player.openContainer instanceof TileEntityTogglyfier.ContainerTogglyfier)
				{
					int size = inventory.size();
					for(int i = 0; i<size; i++)
					{
						((TileEntityTogglyfier.ContainerTogglyfier) player.openContainer).inventoryItemStacks.set(i, inventory.get(i)==null ? null : inventory.get(i).copy());
						((TileEntityTogglyfier.ContainerTogglyfier) player.openContainer).inventory.setInventorySlotContents(i, inventory.get(i));
					}
					
					if(FMLClientHandler.instance().getClient().currentScreen instanceof GuiTogglyfier)
					{
						FMLClientHandler.instance().getClient().currentScreen.initGui();
					}
				}
			}
		}
	}
	
	class TogglyfierGuiClosed implements TogglyfierPacket
	{
		public TogglyfierGuiClosed() {}
		
		public TogglyfierGuiClosed(final String togglyfiers, final byte[] dataIn)
		{
			for(byte datum : dataIn)
			{
				this.data.writeByte(datum);
			}
		}
		
		@Override
		public TogglyfierPacket generatePacket(final Object... data)
		{
			return null;
		}
		
		@Override
		public TogglyfierPacket consumePacket(final ByteBuf data)
		{
			return null;
		}
		
		@Override
		public void execute(final EntityPlayer player)
		{
		
		}
	}
	
	class TogglyfierSwitchModePacket implements TogglyfierPacket
	{
		public TogglyfierSwitchModePacket() {}
		
		public TogglyfierSwitchModePacket(final String togglyfiers, final BlockTogglyfier.EnumTogglyfierMode edit)
		{
			this.data.writeByte(1);
			this.data.writeByte(edit.ordinal());
		}
		
		public TogglyfierSwitchModePacket(final String togglyfiers, final byte[] dataIn)
		{
			for(byte datum : dataIn)
			{
				this.data.writeByte(datum);
			}
		}
		
		@Override
		public TogglyfierPacket generatePacket(final Object... data)
		{
			return null;
		}
		
		@Override
		public TogglyfierPacket consumePacket(final ByteBuf data)
		{
			return null;
		}
		
		@Override
		public void execute(final EntityPlayer player)
		{
		
		}
	}
	
	class PlayerNotificationPacket implements TogglyfierPacket
	{
		public PlayerNotificationPacket() {}
		
		@Override
		public TogglyfierPacket generatePacket(final Object... data)
		{
			return null;
		}
		
		@Override
		public TogglyfierPacket consumePacket(final ByteBuf data)
		{
			return null;
		}
		
		@Override
		public void execute(final EntityPlayer player)
		{
		
		}
	}
}
