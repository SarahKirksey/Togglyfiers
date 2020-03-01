package com.sarahk.togglyfiers.util;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.tileentity.TileEntityChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Objects;

public class TBChangeBlockInfo
{
	private boolean overrideOff = false;
	private boolean overrideOn = false;
	private BlockPos pos;
	
	public TBChangeBlockInfo()
	{
		expectedBlock = new Block[] {Blocks.AIR, Blocks.AIR};
		containerNBT = new NBTTagCompound[2];
		alignment = EnumFacing.DOWN;
	}
	
	public TBChangeBlockInfo(BlockPos pos)
	{
		this();
		this.pos = pos;
	}
	
	public TBChangeBlockInfo(int x, int y, int z)
	{
		this(new BlockPos(x, y, z));
	}
	
	public TBChangeBlockInfo(@Nonnull TBChangeBlockInfo that)
	{
		this();
		//this.expectedBlock = new Block[] {that.expectedBlock[0], that.expectedBlock[1]};
		//this.savedMetadata = that.savedMetadata;
		//this.current = new ItemStack[2];
		//this.containerNBT = new NBTTagCompound[2];
		
		this.pos = that.pos;
		
		for(int var2 = 0; var2<2; ++var2)
		{
			if(that.getCurrent()[var2]!=null)
			{
				current[var2] = new MutableItemStack(that.getCurrent()[var2]);
			}
		}
		this.overrideOff = that.overrideOff;
		this.overrideOn = that.overrideOn;
		
		alignment = that.getAlignment();
	}
	
	public TBChangeBlockInfo(@Nonnull NBTTagCompound var1, @Nonnull TileEntity var2)
	{
		this();
		
		BlockPos pos = var2.getPos();
		
		if(var2 instanceof TileEntityTogglyfier || var2 instanceof TileEntityChangeBlock)
		{
			// Get the position by adding the saved offset to the TE's absolute position
			pos = new BlockPos(
				var1.getByte("X") + pos.getX(),
				var1.getByte("Y") + pos.getY(),
				var1.getByte("Z") + pos.getZ());
		}
		else
		{
			throw new IllegalArgumentException("TBChangeBlockInfo(NBTTagCompound, TileEntity): Invalid Tile Entity Type: " + var2.getClass().getName());
		}
		
		NBTTagList var3 = var1.getTagList("Current", NBTTagType.Compound.ordinal());
		
		int tagCount = var3.tagCount();
		for(int var4 = 0; var4<tagCount; ++var4)
		{
			NBTTagCompound var5 = var3.getCompoundTagAt(var4);
			byte var6 = var5.getByte("Slot");
			
			if(var6 >= 0 && var6<current.length)
			{
				current[var6] = new MutableItemStack(new ItemStack(var5));
			}
		}
		
		NBTTagList var8 = var1.getTagList("Containers", NBTTagType.Compound.ordinal());
		
		int tagCount2 = var8.tagCount();
		for(int var9 = 0; var9<tagCount2; ++var9)
		{
			NBTTagCompound var10 = var8.getCompoundTagAt(var9);
			byte var7 = var10.getByte("Slot");
			
			if(var7 >= 0 && var7<containerNBT.length)
			{
				containerNBT[var7] = var10.getCompoundTag("NBT");
			}
		}
		
		overrideOff = var1.getBoolean("OverrideOff");
		overrideOn = var1.getBoolean("OverrideOn");
		expectedBlock[0] = Block.getBlockFromName(var1.getString("ExpectedBlockOff"));
		expectedBlock[1] = Block.getBlockFromName(var1.getString("ExpectedBlockOn"));
		alignment = EnumFacing.values()[var1.getByte("Alignment")];
		
		if(var1.hasKey("SavedMetadata"))
		{
			savedMetadata = var1.getByteArray("SavedMetadata");
		}
	}
	
	@Nonnull
	public EnumFacing getAlignment()
	{
		return alignment;
	}
	
	@Nonnull
	public NBTTagCompound[] getContainerNBT()
	{
		return containerNBT;
	}
	
	@Nonnull
	public MutableItemStack[] getCurrent()
	{
		return current;
	}
	
	@Nonnull
	public Block[] getExpectedBlock()
	{
		return expectedBlock;
	}
	
	public byte[] getSavedMetadata()
	{
		return savedMetadata;
	}
	
	public void setAlignment(@NotNull EnumFacing alignment)
	{
		this.alignment = alignment;
	}
	
	public void setContainerNBT(@NotNull NBTTagCompound[] containerNBT)
	{
		this.containerNBT = containerNBT;
	}
	
	public void setCurrent(@Nonnull MutableItemStack[] current)
	{
		this.current = current;
	}
	
	public void setExpectedBlock(@NotNull Block[] expectedBlock)
	{
		this.expectedBlock = expectedBlock;
	}
	
	public void setExpectedBlock(@NotNull Block expectedBlock, int index)
	{
		this.expectedBlock[index] = expectedBlock;
	}
	
	public void setSavedMetadata(byte[] savedMetadata)
	{
		this.savedMetadata = savedMetadata;
	}
	
	public IBlockState toBlockState()
	{
		return ModBlocks.changeBlock.getDefaultState().withProperty(BlockDirectional.FACING, alignment);
	}
	
	private boolean getOverride(boolean on)
	{
		return on ? overrideOn : overrideOff;
	}
	
	public BlockPos getBlockPos() { return pos; }
	public void setBlockPos(BlockPos setBlockPos) { pos = setBlockPos; }
	
	public boolean getOverride(int slot)
	{
		return getOverride(slot!=0);
	}
	
	public void setOverride(boolean value, int slot)
	{
		setOverride(value, slot!=0);
	}
	
	public void writeToNBT(NBTTagCompound var1, TileEntity var2)
	{
		if(var2 instanceof TileEntityTogglyfier)
		{
			var1.setByte("X", (byte) (pos.getX() - var2.getPos().getX()));
			var1.setByte("Y", (byte) (pos.getY() - var2.getPos().getY()));
			var1.setByte("Z", (byte) (pos.getZ() - var2.getPos().getZ()));
		}
		else if(!(var2 instanceof TileEntityChangeBlock))
		{
			throw new IllegalArgumentException("TBChangeBlockInfo.writeToNBT: Invalid Tile Entity Type: " + var2.getClass().getName());
		}
		
		NBTTagList var3 = new NBTTagList();
		
		for(int var4 = 0; var4<current.length; ++var4)
		{
			if(this.getCurrent()[var4]!=null)
			{
				NBTTagCompound var5 = new NBTTagCompound();
				var5.setByte("Slot", (byte) var4);
				this.getCurrent()[var4].toItemStack().writeToNBT(var5);
				var3.appendTag(var5);
			}
		}
		
		NBTTagList var7 = new NBTTagList();
		
		for(int var8 = 0; var8<containerNBT.length; ++var8)
		{
			if(this.getContainerNBT()[var8]!=null)
			{
				NBTTagCompound var6 = new NBTTagCompound();
				var6.setByte("Slot", (byte) var8);
				var6.setTag("NBT", this.getContainerNBT()[var8]);
				var7.appendTag(var6);
			}
		}
		
		var1.setTag("Current", var3);
		var1.setTag("Containers", var7);
		
		var1.setBoolean("OverrideOff", overrideOff);
		var1.setBoolean("OverrideOn", overrideOn);
		var1.setString("ExpectedBlockOff", Objects.requireNonNull(expectedBlock[0].getRegistryName()).toString());
		var1.setString("ExpectedBlockOn", Objects.requireNonNull(expectedBlock[1].getRegistryName()).toString());
		
		var1.setByte("Alignment", (byte) this.getAlignment().ordinal());
		
		if(savedMetadata != null && (savedMetadata[0]>0 || savedMetadata[1]>0))
		{
			var1.setByteArray("SavedMetadata", savedMetadata);
		}
	}
	
	private void setOverride(boolean value, boolean on)
	{
		if(on)
		{
			overrideOn = value;
		}
		else
		{
			overrideOff = value;
		}
	}
	
	@Nonnull private MutableItemStack[] current = new MutableItemStack[] {new MutableItemStack(ItemStack.EMPTY), new MutableItemStack(ItemStack.EMPTY)};
	@Nonnull private NBTTagCompound[] containerNBT;
	@Nonnull private Block[] expectedBlock;
	@Nonnull private EnumFacing alignment;
	private byte[] savedMetadata;
}