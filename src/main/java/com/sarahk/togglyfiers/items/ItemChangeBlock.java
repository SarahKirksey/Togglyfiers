package com.sarahk.togglyfiers.items;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.ModConfig;
import com.sarahk.togglyfiers.blocks.BlockChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemChangeBlock extends ItemBlock
{
	private static int maxChangeBlockRange = 48;
	
	public ItemChangeBlock(Block var1)
	{
		super(var1);
	}
	
	@Override
	public boolean placeBlockAt(@NotNull ItemStack var1, @NotNull EntityPlayer var2, World var3, @NotNull BlockPos pos, EnumFacing var7, float var8, float var9, float var10, IBlockState state)
	{
		if(BlockChangeBlock.doesStackHaveTogglyfierInfo(var1))
		{
			NBTTagCompound var11 = var1.getTagCompound();
			
			assert var11!=null;
			if(var11.getInteger("Dim")==var2.dimension)
			{
				TileEntity var12 = var3.getTileEntity(new BlockPos(var11.getInteger("X"), var11.getInteger("Y"), var11.getInteger("Z")));
				
				if(var12 instanceof TileEntityTogglyfier)
				{
					TileEntityTogglyfier var13 = (TileEntityTogglyfier) var12;
					
					if(!var13.isEditing())
					{
						if(!var3.isRemote)
						{
							Main.notifyPlayer("Owning Toggle Block not in Edit mode!");
							BlockChangeBlock.removeChangeBlocksFromPlayer(var2, var1.getTagCompound());
						}
					}
					else if(var13.getNumberOfChangeBlocks() >= var13.getMaxChangeBlocks())
					{
						if(!var3.isRemote)
						{
							Main.notifyPlayer("Change Block limit reached!");
							BlockChangeBlock.removeChangeBlocksFromPlayer(var2, var13);
						}
					}
					else
					{
						BlockPos pos2 = pos;
						Block var17 = var3.getBlockState(pos).getBlock();
						
						//if(var17.canBeReplacedByLeaves(var3.getBlockState(pos), var3, pos))
						if(var17!=Blocks.SNOW && var17!=Blocks.VINE && var17!=Blocks.TALLGRASS && var17!=Blocks.DEADBUSH)
						{
							pos2 = pos.offset(var7);
						}
						
						if(isChangeBlockInRange(var13, pos2))
						{
							return super.placeBlockAt(var1, var2, var3, pos, var7, var8, var9, var10, state);
						}
						else
						{
							if(!var3.isRemote)
							{
								BlockPos var20 = pos.subtract(var13.getPos());
								Main.notifyPlayer("Change Block not in range! (Max range is " + maxChangeBlockRange + ")");
								StringBuilder var21 = new StringBuilder();
								
								int var23 = Math.abs(var20.getX());
								int var24 = Math.abs(var20.getY());
								int var25 = Math.abs(var20.getZ());
								
								if(var23>maxChangeBlockRange)
								{
									var21.append('X').append(" distance is ").append(var23);
								}
								
								if(var24>maxChangeBlockRange)
								{
									(var21.append(var21.length()>0 ? ", Y" : "Y")).append(" distance is ").append(var24);
								}
								
								if(var25>maxChangeBlockRange)
								{
									(var21.append(var21.length()>0 ? ", Z" : "Z")).append(" distance is ").append(var25);
								}
								
								Main.notifyPlayer(var21.toString());
							}
						}
					}
				}
				else if(!var3.isRemote)
				{
					Main.notifyPlayer("Owning Toggle Block was destroyed!");
					BlockChangeBlock.removeChangeBlocksFromPlayer(var2, var1.getTagCompound());
				}
			}
			else if(!var3.isRemote)
			{
				Main.notifyPlayer("Not in correct dimension!");
			}
		}
		else if(!var3.isRemote)
		{
			Main.notifyPlayer("Change Block missing owning Toggle Block info!");
			BlockChangeBlock.removeChangeBlocksFromPlayer(var2, new ItemStack(Main.changeBlock).getTagCompound());
		}
		return false;
	}
	
	/**
	 * allows items to add custom lines of information to the mouseover description
	@Override
	public void addInformation(ItemStack var1, List<String> var2)
	{
		if (!BlockChangeBlock.stackHasTogglyfierInfo(var1))
		{
			var2.add("\u00a7cMissing owning Toggle Block information!");
		}
		else
		{
			NBTTagCompound var3 = var1.getTagCompound();
			var2.add("Owning Toggle Block:");
			StringBuilder var4 = new StringBuilder("Dimension: ");
			assert var3!=null;
			int var5 = var3.getInteger("Dim");
			
			switch (var5)
			{
			case -1:
				var4.append("Nether");
				break;
			
			case 0:
				var4.append("Surface");
				break;
			
			case 1:
				var4.append("The End");
				break;
			
			default:
				var4.append('#').append(var5);
			}
			
			var4.append(" @");
			var2.add(var4.toString());
			var2.add("(" + var3.getInteger("X") + ", " + var3.getInteger("Y") + ", " + var3.getInteger("Z") + ")");
		}
	}
	 */
	
	/**
	 * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
	 */
	@Override
	public boolean getShareTag()
	{
		return true;
	}
	
	private static boolean isChangeBlockInRange(TileEntityTogglyfier var1, BlockPos pos)
	{
		BlockPos diff = var1.getPos().subtract(pos);
		return Math.max(Math.abs(diff.getX()), Math.max(Math.abs(diff.getY()), Math.abs(diff.getZ()))) <= maxChangeBlockRange;
	}
	
	static
	{
		maxChangeBlockRange = ModConfig.getInt(ModConfig.TogglyfierOptions.MAX_CB_DISTANCE);
	}
}
