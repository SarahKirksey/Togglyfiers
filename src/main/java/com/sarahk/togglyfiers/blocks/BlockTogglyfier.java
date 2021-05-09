package com.sarahk.togglyfiers.blocks;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.gui.GuiHandler;
import com.sarahk.togglyfiers.items.ItemTogglyfier;
import com.sarahk.togglyfiers.listeners.ListenerBase;
import com.sarahk.togglyfiers.listeners.ListenerRegister;
import com.sarahk.togglyfiers.listeners.TogglyfierListeners;
import com.sarahk.togglyfiers.tileentity.TileEntityChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import com.sarahk.togglyfiers.util.TBChangeBlockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.minecraft.util.EnumBlockRenderType.ENTITYBLOCK_ANIMATED;

public class BlockTogglyfier extends BlockContainer
{
	@NotNull
	public static final PropertyEnum<EnumTogglyfierMode> STATE = PropertyEnum.create("state", EnumTogglyfierMode.class, EnumTogglyfierMode.values());
	@NotNull
	public static final PropertyEnum<EnumTogglyfierSize> SIZE = PropertyEnum.create("size", EnumTogglyfierSize.class, EnumTogglyfierSize.values());
	
	public BlockTogglyfier()
	{
		this(
			Objects.requireNonNull(Material.IRON),
			"togglyfier",
			"togglyfier",
			"pickaxe",
			1,
			4.0f,
			5.0f,
			CreativeTabs.REDSTONE);
	}
	
	private BlockTogglyfier(
		@NotNull Material material,
		@NotNull String unlocalizedName,
		@NotNull String registryName,
		@NotNull String harvestTool,
		int harvestLevel,
		float hardness,
		float resistance,
		@NotNull CreativeTabs tab)
	{
		super(material);
		setTranslationKey(unlocalizedName);
		setRegistryName(registryName);
		setHarvestLevel(harvestTool, harvestLevel);
		setHardness(hardness);
		setResistance(resistance);
		setCreativeTab(tab);
		setDefaultState(this.blockState.getBaseState().withProperty(STATE, EnumTogglyfierMode.DEACTIVATED));
	}
	
	public IBlockState getStateByMode(@NotNull final EnumTogglyfierMode mode)
	{
		return getDefaultState().withProperty(STATE, mode);
	}
	
	@Override
	public TileEntity createNewTileEntity(@NotNull World worldIn, int meta)
	{
		return new TileEntityTogglyfier(worldIn);
	}
	
	@NotNull
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(STATE, EnumTogglyfierMode.values()[meta]);
	}
	
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return state.getValue(STATE).ordinal();
	}
	
	@Override
	public void onPlayerDestroy(World worldIn, BlockPos pos, IBlockState state)
	{
		ListenerBase lb = ListenerRegister.GetListeners(worldIn, ModBlocks.togglyfier);
		if(lb != null) { lb.OnDestroyed(pos); }
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
	{
		EnumTogglyfierMode val = state.getValue(STATE);
		boolean powered = (worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up()));
		if((val==EnumTogglyfierMode.ACTIVATED || val==EnumTogglyfierMode.DEACTIVATED))
		{
			EnumTogglyfierMode toggle = powered ? EnumTogglyfierMode.ACTIVATED : EnumTogglyfierMode.DEACTIVATED;
			worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
			worldIn.setBlockState(pos, state.withProperty(STATE, toggle));
			
			TogglyfierListeners listener = (TogglyfierListeners) ListenerRegister.GetListeners(worldIn, ModBlocks.togglyfier);
			if(powered)
			{
				listener.OnActivate(pos);
			}
			else
			{
				listener.OnDeactivate(pos);
			}
		}
		TileEntityTogglyfier te = (TileEntityTogglyfier) worldIn.getTileEntity(pos);
		if(te!=null)
		{ te.setPowered(powered); }
	}
	
	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
	{
		ListenerBase listener = ListenerRegister.GetListeners(worldIn, ModBlocks.togglyfier);
		if(listener != null)
		{
			listener.OnPlaced(pos);
		}
	}
	
	@Override
	public void onExplosionDestroy(World worldIn, BlockPos pos, Explosion explosionIn)
	{
		ListenerBase listener = ListenerRegister.GetListeners(worldIn, ModBlocks.togglyfier);
		
		if(listener != null)
		{
			listener.OnDestroyed(pos);
		}
	}
	
	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing var6, float var7, float var8, float var9)
	{
		if (!worldIn.isRemote && playerIn instanceof EntityPlayerMP)
		{
			TileEntityTogglyfier var10 = (TileEntityTogglyfier)worldIn.getTileEntity(pos);
			
			if(playerIn.isSneaking() && playerIn.getHeldItem(hand).isEmpty())
			{
				EnumTogglyfierMode mode;
				if(state.getValue(STATE)==EnumTogglyfierMode.EDIT)
				{
					TileEntityTogglyfier te = (TileEntityTogglyfier) worldIn.getTileEntity(pos);
					if(te==null)
					{ return false; }
					mode = te.isPowered() ? EnumTogglyfierMode.ACTIVATED : EnumTogglyfierMode.DEACTIVATED;
				}
				else
				{
					mode = EnumTogglyfierMode.EDIT;
				}
				worldIn.setBlockState(pos, state.withProperty(STATE, mode));
			}
			else
			{
				playerIn.openGui(Main.instance, GuiHandler.gui_togglyfier, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			
			if (var10.isReady())
			{
				var10.onInventoryChanged();
			}
		}
		
		return true;
	}
	
	/**
	 * Called when the block is placed in the world.
	 */
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if (!worldIn.isRemote)
		{
			TileEntityTogglyfier togglyfier = (TileEntityTogglyfier)worldIn.getTileEntity(pos);
			if(togglyfier==null)
			{ throw new AssertionError(); }
			togglyfier.setMode(EnumTogglyfierMode.EDIT);
		}
	}
	
	@NotNull
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, STATE);
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {return true;}
	
	@Nullable
	@Override
	public TileEntity createTileEntity(@NotNull World world, @NotNull IBlockState state)
	{
		return new TileEntityTogglyfier(world);
	}
	
	@NotNull
	@Override
	public IBlockState getStateForPlacement(World world, @NotNull BlockPos pos, @NotNull EnumFacing facing,
											float hitX, float hitY, float hitZ, int meta,
											@NotNull EntityLivingBase placer, EnumHand hand)
	{
		EnumTogglyfierMode state = world.isBlockPowered(pos) || world.isBlockPowered(pos.up()) ? EnumTogglyfierMode.ACTIVATED : EnumTogglyfierMode.DEACTIVATED;
		return getDefaultState().withProperty(STATE, state);
	}
	
	protected EnumTogglyfierMode cycleState(@NotNull EnumTogglyfierMode state)
	{
		return EnumTogglyfierMode.values()[(state.ordinal() + 1)%(EnumTogglyfierMode.values().length)];
	}
	
	public enum EnumTogglyfierMode implements IStringSerializable
	{
		@NotNull DEACTIVATED,	// Used to represent a lack of redstone signal: togglyfier is "off"
		@NotNull ACTIVATED,		// Used to represent redstone power: togglyfier is "on"
		@NotNull EDIT,			// Used to represent a togglyfier in edit mode
		@NotNull LOCKDOWN,		// Used to represent a togglyfier that has encountered a problem, and need player involvement.
		@NotNull MODE_READY;	// A special value sometimes used BRIEFLY in code as a togglyfier moves from edit/lockdown to deactivated/activated
		
		@Override
		public String getName()
		{
			return this.name().toLowerCase();
		}
	}
	
	
	
	protected BlockTogglyfier(int var1, Material var2)
	{
		super(var2, MapColor.IRON);
	}
	
	/**
	 * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
	 * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
	 */
	@Override
	public boolean isNormalCube(IBlockState state)
	{
		return true;
	}
	
	/**
	 * Determines the damage on the item the block drops. Used in cloth and wood.
	 */
	@Override
	public int damageDropped(IBlockState var1)
	{
		return ((Enum)var1.getProperties().get(SIZE)).ordinal();
	}
	
	/**
	 * ejects contained items into the world, and notifies neighbours of an update, as appropriate
	 */
	@Override
	public void breakBlock(World var1, BlockPos pos, IBlockState state)
	{
		if (!var1.isRemote)
		{
			TileEntityTogglyfier var7 = (TileEntityTogglyfier)var1.getTileEntity(pos);
			int var8;
			
			for (var8 = 0; var8 < var7.getSizeInternalInventory(); ++var8)
			{
				if (var8 != 2)
				{
					Main.dropItemStack(var1, var7.getStackInSlot(var8), pos);
				}
			}
			
			var8 = var7.getNumberOfChangeBlocks();
			EnumTogglyfierMode var9 = var7.getMode();
			int var10;
			TBChangeBlockInfo var11;
			
			if (var9 != EnumTogglyfierMode.EDIT && var9 != EnumTogglyfierMode.LOCKDOWN)
			{
				var7.retrieveBlocks();
				var7.prepareForDestruction();
				
				for (var10 = 0; var10 < var8; ++var10)
				{
					var11 = var7.getChangeBlock(var10);
					
					for (int var14 = 0; var14 < 2; ++var14)
					{
						Main.dropItemStack(var1, var11.getCurrent()[var14], pos);
						var7.dropItemsFromNBT(var11.getContainerNBT()[var14]);
					}
				}
			}
			else
			{
				var7.prepareForDestruction();
				
				for (var10 = 0; var10 < var8; ++var10)
				{
					var11 = var7.getChangeBlock(var10);
					
					if (var11 != null)
					{
						TileEntityChangeBlock var12 = (TileEntityChangeBlock)var1.getTileEntity(var11.getBlockPos());
						int var13;
						
						if (var12 != null)
						{
							for (var13 = 0; var13 < var12.getSizeInventory(); ++var13)
							{
								Main.dropItemStack(var1, var12.getStackInSlot(var13), pos);
								var12.setInventorySlotContents(var13, ItemStack.EMPTY);
							}
							
							var12.setDeregisterOnDelete(false);
							var1.setBlockToAir(var11.getBlockPos());
						}
						
						for (var13 = 0; var13 < 2; ++var13)
						{
							var7.dropItemsFromNBT(var11.getContainerNBT()[var13]);
						}
					}
				}
			}
		}
		
		super.breakBlock(var1, pos, state);
	}
	
	/**
	 * Called when the block is clicked by a player. Args: x, y, z, entityPlayer
	 */
	@Override
	public void onBlockClicked(World var1, BlockPos pos, EntityPlayer var5)
	{
		if(var1.isRemote)
		{
			return;
		}
		
		TileEntityTogglyfier var6 = (TileEntityTogglyfier)var1.getTileEntity(pos);
		long var7 = var1.getWorldTime();
		
		if (var7 - var6.lastClicked < 10L)
		{
			var6.switchMode();
			var6.lastClicked = var7 - 11L;
			
			if (var6.getMode() == EnumTogglyfierMode.MODE_READY)
			{
				BlockChangeBlock.removeChangeBlocksFromPlayer(var5, var6);
			}
		}
		else
		{
			var6.lastClicked = var7;
		}
	}
	
	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are
	 * their own) Args: x, y, z, neighbor blockID
	 */
	@Override
	public void onNeighborChange(IBlockAccess var1, BlockPos pos, BlockPos neighbor)
	{
		TileEntityTogglyfier var6 = (TileEntityTogglyfier)var1.getTileEntity(pos);
		
		if (!var6.isProcessing())
		{
			var6.checkPoweredState();
		}
	}
	
	/**
	 * The type of render function that is called for this block
	 * @return
	 */
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return ENTITYBLOCK_ANIMATED;
	}
	
	/**
	 * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
	 */
	@Override
	public void getSubBlocks(CreativeTabs var2, NonNullList<ItemStack> var3)
	{
		if (Main.doesServerHaveMod())
		{
			for (int var4 = 0; var4 < ItemTogglyfier.togglyfierRefNames.length; ++var4)
			{
				var3.add(new ItemStack(this, 1, var4));
			}
		}
	}
	
	public enum EnumTogglyfierSize implements IStringSerializable
	{
		TINY, SMALL, MEDIUM_IRON, MEDIUM_GOLD, LARGE_IRON, LARGE_GOLD;
		
		@Override
		public String getName()
		{
			return this.name();
		}
	}
}
