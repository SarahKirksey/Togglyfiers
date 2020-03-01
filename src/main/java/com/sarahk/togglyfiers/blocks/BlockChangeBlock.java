package com.sarahk.togglyfiers.blocks;

import com.sarahk.togglyfiers.ModConfig;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockChangeBlock extends BlockDirectional
{
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
	
	@Nullable
	public static EnumFacing getFacing(int meta)
	{
		int i = meta & 7;
		return i>5 ? null : EnumFacing.getFront(i);
	}
	
	public static boolean stackHasTogglyfierInfo(ItemStack var1)
	{
		return false;
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
		i = i | ((EnumFacing) state.getValue(FACING)).getIndex();
		
		return i;
	}
	
	/**
	 * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot)
	{
		return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
	}
	
	/**
	 * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed
	 * blockstate.
	 */
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
		return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
	{
		worldIn.setBlockState(pos, state.withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer)), 2);
	}
	
	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {FACING});
	}
	
	static
	{
		ignoreEmpty = ModConfig.getBoolean(ModConfig.TogglyfierOptions.IGNORE_EMPTY_CB);
	}
}
