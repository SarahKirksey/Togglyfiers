package com.sarahk.togglyfiers.blocks;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.ModConfig;
import com.sarahk.togglyfiers.gui.GuiHandler;
import com.sarahk.togglyfiers.items.ModItems;
import com.sarahk.togglyfiers.tileentity.TileEntityChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import com.sarahk.togglyfiers.util.ChunkCoordinates;
import com.sarahk.togglyfiers.util.TBChangeBlockInfo;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class BlockChangeBlock extends BlockContainer
{
	public static final boolean ignoreEmpty = ModConfig.getBoolean(ModConfig.TogglyfierOptions.IGNORE_EMPTY_CB);
	public static final PropertyDirection FACING = BlockDirectional.FACING;
	
	public BlockChangeBlock(Material var2)
	{
		super(var2);
		setUnlocalizedName("changeBlock");
		setRegistryName("change_block");
		setHardness(4.0f);
		setResistance(5.0f);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
	}
	
	/**
	 * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
	 * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
	 */
	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return true;
	}
	
	
	/**
	 * Called by ItemBlocks after a block is set in the world, to allow post-place logic
	 */
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		super.onBlockPlacedBy(worldIn, pos, null, placer, null);
		TileEntity te = worldIn.getTileEntity(pos);
		EnumFacing newFacing = getAlignmentByPlayer(pos, placer.getAdjustedHorizontalFacing(), placer);
		if(!(te instanceof TileEntityChangeBlock))
		{
			return;
		}
		
		TileEntityChangeBlock teChangeBlock = (TileEntityChangeBlock) te;
		teChangeBlock.getChangeBlock().setAlignment(newFacing);
		
		if (worldIn.isRemote)
		{
			worldIn.notifyBlockUpdate(pos, worldIn.getBlockState(pos), worldIn.getBlockState(pos).withProperty(FACING, newFacing), 2);
		}
		else if(placer.getHeldItem(EnumHand.MAIN_HAND).getItem()==Item.getItemFromBlock(ModBlocks.changeBlock))
		{
			NBTTagCompound tagCompound = placer.getHeldItem(EnumHand.MAIN_HAND).getTagCompound();
			if(tagCompound!=null)
			{
				BlockPos pos2 = new BlockPos(tagCompound.getInteger("X"), tagCompound.getInteger("Y"), tagCompound.getInteger("Z"));
				TileEntityTogglyfier teTogglyfier = (TileEntityTogglyfier) worldIn.getTileEntity(pos2);
				teChangeBlock.setTogglyfierCoords(new ChunkCoordinates(pos2));
				
				if(teTogglyfier!=null && teTogglyfier.registerChangeBlock(pos))
				{
					StringBuilder complaints = new StringBuilder();
					
					for(int var11 = 0; var11<2; ++var11)
					{
						ItemStack var12 = ItemStack.EMPTY;
						ItemStack var13;
						
						if(teTogglyfier.isCreative())
						{
							var13 = teTogglyfier.getStackInSlot(var11);
							
							if(!var13.isEmpty())
							{
								var12 = new ItemStack(var13.getItem(), 1, var13.getItemDamage());
							}
						}
						else
						{
							var12 = teTogglyfier.decrStackSize(var11, 1);
						}
						
						teChangeBlock.setInventorySlotContents(var11, var12);
						
						if(var12.isEmpty() || teTogglyfier.isCreative())
						{
							continue;
						}
						
						var13 = teTogglyfier.getStackInSlot(var11);
						byte var14 = (byte) (var13.isEmpty() ? 0 : var13.getCount());
						
						if(var14 >= 0 && var14<5)
						{
							if(complaints.length()>0 && var11==1)
							{
								complaints.append(", ");
							}
							
							complaints.append(var11==0 ? "Off" : "On").append(" Quick-place slot ");
							
							if(var14 >= 1)
							{
								complaints.append("has ").append(var14).append(" items left");
							}
							else
							{
								complaints.append("is empty!");
							}
						}
					}
					
					if(complaints.length()>0)
					{
						Main.notifyPlayer(complaints.toString());
					}
				}
			}
			else
			{
				Main.notifyPlayer("Change Block stack has no NBT!?");
			}
		}
		else
		{
			Main.notifyPlayer("Change Block not currently held item!?");
		}
	}
	
	/**
	 * ejects contained items into the world, and notifies neighbours of an update, as appropriate
	 */
	@Override
	public void breakBlock(World var1, @NotNull BlockPos pos, @NotNull IBlockState state)
	{
		TileEntityChangeBlock var7 = (TileEntityChangeBlock)var1.getTileEntity(pos);
		assert var7!=null;
		TileEntityTogglyfier var8 = var7.getTogglyfier();
		
		int sizeInventory = var7.getSizeInventory();
		for (int var9 = 0; var9 <sizeInventory; ++var9)
		{
			ItemStack var10 = var7.getStackInSlot(var9);
			
			if (!var10.isEmpty() && var10.getItem() == Item.getItemFromBlock(Blocks.JUKEBOX) && var8 != null)
			{
				TBChangeBlockInfo var11 = var8.getChangeBlock(pos);
				
				if (var11.getSavedMetadata()[var9] > 0 && var11.getContainerNBT()[var9] != null)
				{
					Item var12 = Item.getByNameOrId(var11.getContainerNBT()[var9].getString("Record"));
					Main.dropItemStack(var1, new ItemStack(var12, 1, 0), pos);
				}
			}
			
			Main.dropItemStack(var1, var10, pos);
		}
		
		if (var8 != null && var7.getDeregisterOnDelete())
		{
			var8.deregisterChangeBlock(pos);
			
			if (!var8.isCreative())
			{
				Main.dropItemStack(var1, var8.makeStackOfChangeBlocks(), pos);
			}
		}
		
		super.breakBlock(var1, pos, state);
	}
	
	/**
	 * Called when the block is clicked by a player. Args: x, y, z, entityPlayer
	 */
	@Override
	public void onBlockClicked(World var1, BlockPos pos, EntityPlayer player)
	{
		TileEntityChangeBlock tileEntity = (TileEntityChangeBlock)var1.getTileEntity(pos);
		
		if (tileEntity != null)
		{
			TBChangeBlockInfo changeBlock = tileEntity.getChangeBlock();
			EnumFacing newFacing = getAlignmentByPlayer(pos, changeBlock.getAlignment(), player);
			changeBlock.setAlignment(newFacing);
			var1.notifyBlockUpdate(pos, var1.getBlockState(pos), var1.getBlockState(pos).withProperty(FACING, newFacing), 2);
		}
	}
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack var10 = player.getHeldItem(EnumHand.MAIN_HAND);
		
		if (!var10.isEmpty())
		{
			if (var10.getItem() == Item.getItemFromBlock(ModBlocks.changeBlock))
			{
				return false;
			}
			
			if (var10.getItem() == ModItems.togglyfierAssistant && var10.getItemDamage() == 2)
			{
				return false;
			}
		}
		
		if (!worldIn.isRemote && player instanceof EntityPlayerMP)
		{
			TileEntityChangeBlock var11 = (TileEntityChangeBlock)worldIn.getTileEntity(pos);
			TileEntityChangeBlock.ContainerChangeBlock var12 = new TileEntityChangeBlock.ContainerChangeBlock(player.inventory, var11);
			player.openGui(Main.instance, GuiHandler.gui_change_block, worldIn, pos.getX(), pos.getY(), pos.getZ());
		}
		
		return true;
	}
	
	/**
	 * each class overrides this to return a new <className>
	 */
	@Override
	public TileEntity createNewTileEntity(World var1, int meta)
	{
		return new TileEntityChangeBlock();
	}
	
	private static int flipAlignment(int var0)
	{
		int result = 0;
		switch(var0)
		{
		case 0:
		case 1:
			result = 1 - var0;
			break;
		case 2:
		case 3:
			result = 5 - var0;
			break;
		case 4:
		case 5:
			result = 9 - var0;
			break;
		}
		return result;
	}
	
	public static EnumFacing getAlignmentByPlayer(BlockPos pos, EnumFacing var3, EntityLivingBase player)
	{
		int var0 = pos.getX();
		int var1 = pos.getY();
		int var2 = pos.getZ();
		EnumFacing var5 = null;
		double var6 = player.posX - var0;
		double var8 = player.posY - var1 + (1.62D - (double)player.height);
		double var10 = player.posZ - var2;
		boolean var12 = var6 >= -0.35D && var6 <= 1.35D && var10 >= -0.35D && var10 < 1.35D;
		
		if (var12 && var8 > 2.5D)
		{
			var5 = EnumFacing.VALUES[1];
		}
		else if (var12 && var8 < 0.0D)
		{
			var5 = EnumFacing.VALUES[0];
		}
		else
		{
			int var13 = MathHelper.floor((player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			
			switch (var13)
			{
			case 0:
				var5 = EnumFacing.VALUES[2];
				break;
			
			case 1:
				var5 = EnumFacing.VALUES[5];
				break;
			
			case 2:
				var5 = EnumFacing.VALUES[3];
				break;
			
			case 3:
				var5 = EnumFacing.VALUES[4];
			}
		}
		
		if (var5 == var3)
		{
			var5 = EnumFacing.VALUES[flipAlignment(var5.ordinal())];
		}
		
		return var5;
	}
	
	public static void removeChangeBlocksFromPlayer(EntityPlayer var0, NBTTagCompound changeBlockTagCompoundToRemove)
	{
		if (var0 == null)
		{
			var0 = Minecraft.getMinecraft().player;
		}
		
		InventoryPlayer var2 = var0.inventory;
		
		int sizeInventory = var2.getSizeInventory();
		for (int var3 = 0; var3 <sizeInventory; ++var3)
		{
			ItemStack var4 = var2.getStackInSlot(var3);
			
			if (var4.getItem() == Item.getItemFromBlock(ModBlocks.changeBlock)
					&& (!doesStackHaveTogglyfierInfo(var4) || changeBlockTagCompoundToRemove.equals(var4.getTagCompound())))
			{
				var2.setInventorySlotContents(var3, ItemStack.EMPTY);
			}
		}
	}
	
	public static void removeChangeBlocksFromPlayer(EntityPlayer var0, TileEntityTogglyfier var1)
	{
		removeChangeBlocksFromPlayer(var0, var1.makeStackOfChangeBlocks().getTagCompound());
	}
	
	public static boolean doesStackHaveTogglyfierInfo(ItemStack var0)
	{
		if (var0 != null && var0.getItem() == Item.getItemFromBlock(ModBlocks.changeBlock))
		{
			NBTTagCompound var1 = var0.getTagCompound();
			return var1 != null && var1.hasKey("Dim") && var1.hasKey("X") && var1.hasKey("Y") && var1.hasKey("Z");
		}
		else
		{
			return false;
		}
	}
	
	@NotNull
	public static EnumFacing getFacing(int meta)
	{
		return EnumFacing.getFront(meta & 7);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(FACING, getFacing(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		int i = 0;
		i |= state.getValue(FACING).getIndex();
		
		return i;
	}
	 
	/**
	* Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	* blockstate.
	*/
	@NotNull
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot)
	{
		return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
	}
	
	/**
	* Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	* blockstate.
	*/
	@NotNull
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
		return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
	}
	
	@NotNull
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}
	
	@NotNull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, FACING);
	}
}

/*{
	public static final boolean ignoreEmpty;
	
	public BlockChangeBlock()
	{
		super(Material.CLOTH);
		setUnlocalizedName("changeBlock");
		setRegistryName("change_block");
		setHardness(4.0f);
		setResistance(5.0f);
		setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		
		setCreativeTab(CreativeTabs.REDSTONE);
	}
	
	public static boolean stackHasTogglyfierInfo(ItemStack var1)
	{
		return false;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
	}
}
*/