package com.sarahk.togglyfiers.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockTogglyfier extends Block
{

    public BlockTogglyfier() {
        super(Material.CLAY);
        setUnlocalizedName("togglyfier");
        setRegistryName("togglyfier");
        setHarvestLevel("pickaxe",0);
        setCreativeTab(CreativeTabs.REDSTONE);
    }
}
