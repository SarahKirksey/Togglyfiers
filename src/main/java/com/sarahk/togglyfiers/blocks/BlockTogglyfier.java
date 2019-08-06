package com.sarahk.togglyfiers.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.gui.GuiHandler;
import com.sarahk.togglyfiers.listeners.ListenerRegister;
import com.sarahk.togglyfiers.listeners.ToglyfierListeners;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockTogglyfier extends Block
{
    public static final PropertyEnum<EnumState> STATE = PropertyEnum.create("state", EnumState.class, EnumState.values());
    
    public BlockTogglyfier()
    {
        super(Material.IRON);
        setUnlocalizedName("togglyfier");
        setRegistryName("togglyfier");
        setHarvestLevel("pickaxe",1);
        setHardness(4.0f);
        setResistance(5.0f);
        setCreativeTab(CreativeTabs.REDSTONE);
        setDefaultState(this.blockState.getBaseState().withProperty(STATE, EnumState.DEACTIVATED));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState blockstate, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        EnumState state;
        if(!playerIn.isSneaking())
        {
            if(!worldIn.isRemote)
                playerIn.openGui(Main.instance, GuiHandler.gui_togglyfier, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        else
        {
            if(!blockstate.getValue(STATE).equals(EnumState.EDIT)) state = EnumState.EDIT;
            else
            {
                TileEntityTogglyfier te = (TileEntityTogglyfier)worldIn.getTileEntity(pos);
                if(te == null) return false;
                state = te.powered ? EnumState.ACTIVATED : EnumState.DEACTIVATED;
            }
            worldIn.setBlockState(pos, blockstate.withProperty(STATE, state));
        }


        return true;
    }

    //redstone interaction

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos)
    {
        EnumState val = state.getValue(STATE);
        boolean powered = (worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(pos.up()));
        if((val.equals(EnumState.ACTIVATED) || val.equals(EnumState.DEACTIVATED)))
        {
            EnumState toggle = powered ? EnumState.ACTIVATED : EnumState.DEACTIVATED;
            worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
            worldIn.setBlockState(pos, state.withProperty(STATE, toggle));
            if(powered) {
            	((ToglyfierListeners)ListenerRegister.GetListeners(worldIn, ModBlocks.togglifyer)).OnActivate(pos);
            }else {
            	((ToglyfierListeners)ListenerRegister.GetListeners(worldIn, ModBlocks.togglifyer)).OnDeactivate(pos);
            }
        }
        TileEntityTogglyfier te = (TileEntityTogglyfier) worldIn.getTileEntity(pos);
        if(te != null) te.powered = powered;
    }


    //Tile Entity

    @Override
    public boolean hasTileEntity(IBlockState state) {return true;}
    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityTogglyfier(world);
    }

    //Block State Stuff
    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STATE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STATE, EnumState.values()[meta]);
    }

    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, new IProperty[] {STATE});
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        EnumState state = world.isBlockPowered(pos) || world.isBlockPowered(pos.up()) ? EnumState.ACTIVATED : EnumState.DEACTIVATED;
        return getDefaultState().withProperty(STATE, state);
    }

    protected EnumState cycleState(EnumState state)
    {
        return EnumState.values()[(state.ordinal()+1) % (EnumState.values().length)];
    }



    enum EnumState implements IStringSerializable
    {
        DEACTIVATED,
        ACTIVATED,
        EDIT,
        LOCKDOWN;

        @Override
        public String getName()
        {
            return this.name().toLowerCase();
        }




    }
    
    //listener stuff
    public void onBlockDestroyedByExplosion(World worldIn, BlockPos pos, Explosion explosionIn){
    	ListenerRegister.GetListeners(worldIn, ModBlocks.togglifyer).OnDestroied(pos);
    }
    @Override
    public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, IBlockState state) {
    	ListenerRegister.GetListeners(worldIn, ModBlocks.togglifyer).OnDestroied(pos);
    }
    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
    	ListenerRegister.GetListeners(worldIn, ModBlocks.togglifyer).OnPlaced(pos);
    }


}
