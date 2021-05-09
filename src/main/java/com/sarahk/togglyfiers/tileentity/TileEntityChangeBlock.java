package com.sarahk.togglyfiers.tileentity;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.ModConfig;
import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.gui.GuiChangeBlock;
import com.sarahk.togglyfiers.items.ModItems;
import com.sarahk.togglyfiers.util.ChunkCoordinates;
import com.sarahk.togglyfiers.util.MutableItemStack;
import com.sarahk.togglyfiers.util.TBChangeBlockInfo;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.NotNull;

public class TileEntityChangeBlock extends TileEntityInventory
{
	private static final double HALF = 0.5D;
	private TBChangeBlockInfo changeBlock;
	private final MutableItemStack[] replenish = new MutableItemStack[this.getSizeInventory()];
	private ChunkCoordinates tbCoords = null;
	private boolean deregisterOnDelete = true;
	public TileEntityChangeBlock()
	{
		super("Change Block");
		this.errorState = ChangeBlockError.NO_ERROR;
		this.changeBlock = new TBChangeBlockInfo();
	}
	
	/**
	 * signs and mobSpawners use this to send text and meta-data
	public Packet getAuxillaryInfoPacket()
	{
		ByteArrayOutputStream var1 = new ByteArrayOutputStream();
		DataOutputStream var2 = new DataOutputStream(var1);
		
		try
		{
			var2.writeByte(1);
			var2.writeInt(this.xCoord);
			var2.writeShort(this.yCoord);
			var2.writeInt(this.zCoord);
			
			for(int var3 = 0; var3<2; ++var3)
			{
				Packet.writeItemStack(this.replenish[var3], var2);
			}
			
			var2.writeByte(this.errorState.ordinal());
			var2.writeByte(this.changeBlock.alignment);
			var2.writeByte((this.changeBlock.override[0] ? 1 : 0) | (this.changeBlock.override[1] ? 2 : 0));
			byte[] var5 = var1.toByteArray();
			var2.close();
			return new Packet250CustomPayload("Togglyfiers", var5);
		}
		catch(IOException var4)
		{
			var4.printStackTrace();
			return null;
		}
	}
	 */
	
	public TBChangeBlockInfo getChangeBlock()
	{
		return this.changeBlock;
	}
	
	public boolean getDeregisterOnDelete()
	{
		return this.deregisterOnDelete;
	}
	
	public void setDeregisterOnDelete(boolean var1)
	{
		this.deregisterOnDelete = var1;
	}
	
	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory()
	{
		return 2;
	}
	
	/**
	 * Returns the stack in slot i
	 */
	@Override
	public ItemStack getStackInSlot(int var1)
	{
		return this.changeBlock.getCurrent()[var1].toItemStack();
	}
	
	/**
	 * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
	 * new stack.
	 */
	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		MutableItemStack var3 = this.changeBlock.getCurrent()[var1];
		
		if(var3!=null)
		{
			if(var3.getCount()<=var2)
			{
				var2 = var3.getCount();
			}
			
			var3 = new MutableItemStack(this.changeBlock.getCurrent()[var1].splitStack(var2));
			
			if(this.changeBlock.getCurrent()[var1].getCount()==0)
			{
				this.changeBlock.getCurrent()[var1] = null;
			}
			
			this.markDirty();
			return var3.toItemStack();
		}
		else
		{
			return null;
		}
	}
	
	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int var1, @NotNull ItemStack var2)
	{
		if(var2!=ItemStack.EMPTY)
		{
			if(var2.getItem()==ModItems.togglyfierAssistant)
			{
				if(var2.getItemDamage()==2)
				{
					GuiScreen var3 = Minecraft.getMinecraft().currentScreen;
					
					if(var3 instanceof GuiChangeBlock)
					{
						((GuiChangeBlock) var3).updateAlignerButtonText();
					}
				}
			}
			else
			{
				this.changeBlock.getCurrent()[var1].set(var2);
				
				if(var2.getCount()>this.getInventoryStackLimit())
				{
					var2.setCount(getInventoryStackLimit());
				}
				
				this.markDirty();
			}
		}
		else
		{
			this.changeBlock.getCurrent()[var1].set(var2);
			this.markDirty();
		}
	}
	
	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended. *Isn't
	 * this more of a set than a get?*
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	
	public TileEntityTogglyfier getTogglyfier()
	{
		if(this.tbCoords==null)
		{
			return null;
		}
		else
		{
			TileEntity var1 = this.world.getTileEntity(this.tbCoords.toBlockPos());
			return !(var1 instanceof TileEntityTogglyfier) ? null : (TileEntityTogglyfier) var1;
		}
	}
	
	public ChunkCoordinates getTogglyfierCoords()
	{
		return this.tbCoords!=null ? new ChunkCoordinates(this.tbCoords) : null;
	}
	
	public void setTogglyfierCoords(ChunkCoordinates var1)
	{
		this.tbCoords = var1;
	}
	
	public boolean checkForErrors()
	{
		if(this.changeBlock.getCurrent()[0]==null
			   && this.changeBlock.getCurrent()[1]==null
			   && this.replenish[0]==null
			   && this.replenish[1]==null
			   && !this.changeBlock.getOverride(0)
			   && !this.changeBlock.getOverride(1))
		{
			this.errorState = ChangeBlockError.CHANGE_BLOCK_EMPTY;
			return false;
		}
		else
		{
			for(int var1 = 0; var1<2; ++var1)
			{
				if(this.changeBlock.getCurrent()[var1]!=null &&
					   (this.changeBlock.getCurrent()[var1].getItem()==Item.getItemFromBlock(ModBlocks.togglyfier)
							|| this.changeBlock.getCurrent()[var1].getItem()==Item.getItemFromBlock(ModBlocks.changeBlock)))
				{
					this.errorState = ChangeBlockError.CONTAINS_TOGGLE_BLOCK;
					return false;
				}
			}
			
			if(this.tbCoords==null)
			{
				return false;
			}
			else
			{
				TileEntityTogglyfier var6 = (TileEntityTogglyfier) this.world.getTileEntity(this.tbCoords.toBlockPos());
				assert var6!=null;
				TBChangeBlockInfo var2 = var6.getChangeBlock(this.getPos());
				
				for(int var3 = 0; var3<2; ++var3)
				{
					if(var2.getContainerNBT()[var3]!=null)
					{
						Block var4 = ModBlocks.togglyfier;
						ItemStack var5 = (this.changeBlock.getCurrent()[var3].toItemStack().isEmpty() ? this.replenish[var3] : this.changeBlock.getCurrent()[var3]).toItemStack();
						
						if(!ModConfig.isExpectedBlock(var5, var4))
						{
							this.errorState = ChangeBlockError.WRONG_CONTAINER_TYPE;
							return false;
						}
					}
				}
				
				this.errorState = ChangeBlockError.NO_ERROR;
				return true;
			}
		}
	}
	
	public void closeChest() {}
	
	public void forceReadyMode()
	{
		this.checkForErrors();
		
		if(this.errorState==ChangeBlockError.CONTAINS_TOGGLE_BLOCK)
		{
			for(int var1 = 0; var1<2; ++var1)
			{
				if(this.changeBlock.getCurrent()[var1]!=null)
				{
					if(this.changeBlock.getCurrent()[var1].getItem()==Item.getItemFromBlock(ModBlocks.togglyfier))
					{
						ItemStack var2 = this.changeBlock.getCurrent()[var1].toItemStack();
						
						if(this.tbCoords!=null)
						{
							Main.dropItemStack(this.world, var2, this.tbCoords.toBlockPos(), changeBlock.getAlignment());
						}
						else
						{
							Main.dropItemStack(this.world, var2, this.pos, changeBlock.getAlignment());
						}
						
						this.changeBlock.getCurrent()[var1] = null;
					}
					else if(this.changeBlock.getCurrent()[var1].getItem()==Item.getItemFromBlock(ModBlocks.changeBlock))
					{
						this.changeBlock.getCurrent()[var1] = null;
					}
				}
			}
		}
	}
	
	public boolean getOverride(int var1)
	{
		return var1 >= 0 && var1<this.getSizeInventory() && this.changeBlock.getOverride(var1);
	}
	
	@NotNull
	public ItemStack getReplenish(int var1)
	{
		return var1 >= 0 && var1<this.getSizeInventory() ? this.replenish[var1].toItemStack() : ItemStack.EMPTY;
	}
	
	/**
	 * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
	 * like when you close a workbench GUI.
	@Override
	public ItemStack getStackInSlotOnClosing(int var1)
	{
		if(this.changeBlock.getCurrent()[var1]!=null)
		{
			MutableItemStack var2 = this.changeBlock.getCurrent()[var1];
			this.changeBlock.getCurrent()[var1] = null;
			return var2.toItemStack();
		}
		else
		{
			return ItemStack.EMPTY;
		}
	}
	 */
	
	/**
	 * Do not make give this method the name canInteractWith because it clashes with Container
	 */
	@Override
	public boolean isUsableByPlayer(EntityPlayer var1)
	{
		return this.world.getTileEntity(this.pos)==this && var1.getDistanceSq(this.pos.add(HALF, HALF, HALF))<=64.0D;
	}
	
	public void openChest()  {}
	
	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(NBTTagCompound var1)
	{
		super.readFromNBT(var1);
		byte var2 = var1.getByte("EngineVersion");
		int var3;
		NBTTagList var4;
		byte var6;
		
		if(var2<3)
		{
			this.changeBlock.setBlockPos(this.pos);
			var4 = var1.getTagList("Items", 10);		//10 represents a list of NBTTagCompound
			
			for(var3 = 0; var3<var4.tagCount(); ++var3)
			{
				NBTTagCompound var5 = var4.getCompoundTagAt(var3);
				var6 = var5.getByte("Slot");
				
				if(var6 >= 0 && var6<this.changeBlock.getCurrent().length)
				{
					this.changeBlock.getCurrent()[var6] = new MutableItemStack(var5);
				}
			}
			
			this.changeBlock.setOverride(var1.getBoolean("OverrideOff"), 0);
			this.changeBlock.setOverride(var1.getBoolean("OverrideOn"), 1);
			this.changeBlock.setAlignment(EnumFacing.VALUES[var1.getByte("Alignment")]);
		}
		else
		{
			this.changeBlock = new TBChangeBlockInfo(var1.getCompoundTag("ChangeBlock"), this);
		}
		
		var4 = var1.getTagList("Replenish", 10);
		
		for(NBTBase var5 : var4)
		{
			assert var5 instanceof NBTTagCompound;
			var6 = ((NBTTagCompound)var5).getByte("Slot");
			
			if(var6 >= 0 && var6<this.replenish.length)
			{
				this.replenish[var6] = new MutableItemStack((NBTTagCompound) var5);
			}
		}
		
		if(var1.hasKey("TBX"))
		{
			this.tbCoords = new ChunkCoordinates(var1.getInteger("TBX"), var1.getInteger("TBY"), var1.getInteger("TBZ"));
		}
	}
	
	/**
	 * Writes a tile entity to NBT.
	 * @return
	 */
	@NotNull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound var1)
	{
		super.writeToNBT(var1);
		
		ChunkCoordinates tbPos = this.tbCoords;
		
		if(tbPos!=null)
		{
			var1.setInteger("TBX", tbPos.getX());
			var1.setInteger("TBY", tbPos.getY());
			var1.setInteger("TBZ", tbPos.getZ());
		}
		
		NBTTagCompound var2 = new NBTTagCompound();
		this.changeBlock.writeToNBT(var2, this);
		var1.setTag("ChangeBlock", var2);
		NBTTagList var3 = new NBTTagList();
		
		for(int var4 = 0; var4<this.replenish.length; ++var4)
		{
			if(this.replenish[var4]!=null)
			{
				NBTTagCompound var5 = new NBTTagCompound();
				var5.setByte("Slot", (byte) var4);
				this.replenish[var4].toItemStack().writeToNBT(var5);
				var3.appendTag(var5);
			}
		}
		
		var1.setTag("Replenish", var3);
		//var1.setByte("EngineVersion", Main.getEngineVersion());
		return var1;
	}
	
	/**
	 * validates a tile entity
	 */
	@Override
	public void validate()
	{
		super.validate();
		this.changeBlock.setBlockPos(this.pos);
	}
	
	public void setOverride(int var1, boolean var2)
	{
		if(var1 >= 0 && var1<this.getSizeInventory())
		{
			this.changeBlock.setOverride(var2, var1);
		}
	}
	
	public void setReplenish(int var1, ItemStack var2)
	{
		setReplenish(var1, new MutableItemStack(var2));
	}
	
	public void setReplenish(int var1, MutableItemStack var2)
	{
		if(var1 >= 0 && var1<this.getSizeInventory())
		{
			this.replenish[var1] = var2;
		}
	}
	
	public enum ChangeBlockError
	{
		NO_ERROR("No error"),
		CHANGE_BLOCK_EMPTY("Empty Change Block"),
		CONTAINS_TOGGLE_BLOCK("Contains Toggle and/or Change Block"),
		WRONG_CONTAINER_TYPE("Wrong container type");
		private final String errorMessage;
		
		private ChangeBlockError(String var3)
		{
			this.errorMessage = var3;
		}
		
		public static String getErrorMessage(int var0)
		{
			ChangeBlockError[] var1 = values();
			int var2 = var1.length;
			
			for(int var3 = 0; var3<var2; ++var3)
			{
				ChangeBlockError var4 = var1[var3];
				
				if(var4.ordinal()==var0)
				{
					return var4.getErrorMessage();
				}
			}
			
			return "";
		}
		
		public String getErrorMessage()
		{
			return this.errorMessage;
		}
	}
	public ChangeBlockError errorState;
	
	
	public static class ContainerChangeBlock extends Container
	{
		@NotNull private final TileEntityChangeBlock tileEntity;
		
		public ContainerChangeBlock(final IInventory var1, final TileEntityChangeBlock var2)
		{
			tileEntity = var2;
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn)
		{
			return tileEntity.isUsableByPlayer(playerIn);
		}
	}
	
}
