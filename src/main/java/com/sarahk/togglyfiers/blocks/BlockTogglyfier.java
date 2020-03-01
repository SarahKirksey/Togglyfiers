package com.sarahk.togglyfiers.blocks;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.gui.GuiHandler;
import com.sarahk.togglyfiers.listeners.ListenerBase;
import com.sarahk.togglyfiers.listeners.ListenerRegister;
import com.sarahk.togglyfiers.listeners.TogglyfierListeners;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class BlockTogglyfier extends BlockContainer
{
	@Nonnull
	public static final PropertyEnum<EnumTogglyfierMode> STATE = PropertyEnum.create("state", EnumTogglyfierMode.class, EnumTogglyfierMode.values());
	
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
		@Nonnull Material material,
		@Nonnull String unlocalizedName,
		@Nonnull String registryName,
		@Nonnull String harvestTool,
		int harvestLevel,
		float hardness,
		float resistance,
		@Nonnull CreativeTabs tab)
	{
		super(material);
		setUnlocalizedName(unlocalizedName);
		setRegistryName(registryName);
		setHarvestLevel(harvestTool, harvestLevel);
		setHardness(hardness);
		setResistance(resistance);
		setCreativeTab(tab);
		setDefaultState(this.blockState.getBaseState().withProperty(STATE, EnumTogglyfierMode.DEACTIVATED));
	}
	
	public IBlockState getStateByMode(@Nonnull final EnumTogglyfierMode mode)
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
	public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state)
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
	public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn)
	{
		ListenerBase listener = ListenerRegister.GetListeners(worldIn, ModBlocks.togglyfier);
		
		if(listener != null)
		{
			listener.OnDestroyed(pos);
		}
	}
	
	@Override
	public boolean onBlockActivated(
		World worldIn,
		BlockPos pos,
		IBlockState blockState,
		EntityPlayer playerIn,
		EnumHand hand,
		EnumFacing facing,
		float hitX,
		float hitY,
		float hitZ)
	{
		if(playerIn.isSneaking())
		{
			EnumTogglyfierMode state;
			if(blockState.getValue(STATE)==EnumTogglyfierMode.EDIT)
			{
				TileEntityTogglyfier te = (TileEntityTogglyfier) worldIn.getTileEntity(pos);
				if(te==null)
				{ return false; }
				state = te.isPowered() ? EnumTogglyfierMode.ACTIVATED : EnumTogglyfierMode.DEACTIVATED;
			}
			else { state = EnumTogglyfierMode.EDIT; }
			worldIn.setBlockState(pos, blockState.withProperty(STATE, state));
		}
		else
		{
			if(!worldIn.isRemote)
			{
				playerIn.openGui(Main.instance, GuiHandler.gui_togglyfier, worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
		}
		
		
		return true;
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		if(!worldIn.isRemote)
		{
			TileEntityTogglyfier togglyfier = (TileEntityTogglyfier) worldIn.getTileEntity(pos);
			if(togglyfier==null)
			{ throw new AssertionError(); }
			togglyfier.setMode(EnumTogglyfierMode.EDIT);
		}
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
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
	
	protected EnumTogglyfierMode cycleState(@Nonnull EnumTogglyfierMode state)
	{
		return EnumTogglyfierMode.values()[(state.ordinal() + 1)%(EnumTogglyfierMode.values().length)];
	}
	
	public enum EnumTogglyfierMode implements IStringSerializable
	{
		@Nonnull DEACTIVATED,	// Used to represent a lack of redstone signal: togglyfier is "off"
		@Nonnull ACTIVATED,		// Used to represent redstone power: togglyfier is "on"
		@Nonnull EDIT,			// Used to represent a togglyfier in edit mode
		@Nonnull LOCKDOWN,		// Used to represent a togglyfier that has encountered a problem, and need player involvement.
		@Nonnull MODE_READY;		// A special value sometimes used BRIEFLY in code as a togglyfier moves from edit/lockdown to deactivated/activated
		
		@Override
		public String getName()
		{
			return this.name().toLowerCase();
		}
	}
	
	
}
