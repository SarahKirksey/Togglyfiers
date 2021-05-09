package com.sarahk.togglyfiers.items;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.blocks.BlockChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import com.sarahk.togglyfiers.util.ChunkCoordinates;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.sarahk.togglyfiers.blocks.BlockChangeBlock.FACING;

public class ItemTogglyfierAssistant extends Item
{
	private static ArrayList<ChunkCoordinates> scannerBlockList = new ArrayList<>();
	private static final String[] assistantRefNames = new String[] {"spoofer", "scanner", "aligner"};
	public static final int assistantID_Spoofer = 0;
	public static final int assistantID_Scanner = 1;
	public static final int assistantID_Aligner = 2;
	
	public ItemTogglyfierAssistant(int var1)
	{
		super();
		this.hasSubtypes = true;
		this.maxStackSize = 1;
	}
	
	public static void setScannerBlockList(ArrayList<ChunkCoordinates> scannerBlockList)
	{
		ItemTogglyfierAssistant.scannerBlockList = scannerBlockList;
	}
	
	public static ArrayList<ChunkCoordinates> getScannerBlockList()
	{
		return scannerBlockList;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer var2, World var3, BlockPos pos, EnumHand hand, EnumFacing facing, float var8, float var9, float var10)
	{
		ItemStack var1 = var2.getHeldItem(hand);
		if (var1.getItemDamage() == 0)
		{
			NBTTagCompound var11 = var1.getTagCompound();
			
			if (var11 == null || !var11.hasKey("Dim") || !var11.hasKey("X") || !var11.hasKey("Y") || !var11.hasKey("Z"))
			{
				if (!var3.isRemote)
				{
					Main.notifyPlayer("No Toggle Block set for this Assistant!");
				}
				
				var1.setCount(var1.getCount() - 1);
				return EnumActionResult.FAIL;
			}
			
			if (var11.getInteger("Dim") != var2.dimension)
			{
				if (!var3.isRemote)
				{
					Main.notifyPlayer("Not in correct dimension!");
				}
				
				return EnumActionResult.FAIL;
			}
			
			if (!var3.isBlockFullCube(pos))
			{
				if (!var3.isRemote)
				{
					Main.notifyPlayer("The block set must be an opaque cube!");
				}
				
				return EnumActionResult.FAIL;
			}
			
			TileEntity var12 = var3.getTileEntity(new BlockPos(var11.getInteger("X"), var11.getInteger("Y"), var11.getInteger("Z")));
			
			if (!(var12 instanceof TileEntityTogglyfier))
			{
				if (!var3.isRemote)
				{
					Main.notifyPlayer("The set Toggle Block was destroyed!");
				}
				
				var1.setCount(var1.getCount() - 1);
				return EnumActionResult.FAIL;
			}
			
			IBlockState var13 = var3.getBlockState(pos);
			String var15 = var13.getBlock().getLocalizedName(); //StringTranslate.getInstance().translateNamedKey(var13.getBlock().getUnlocalizedName());
			
			if (!var3.isRemote)
			{
				Main.notifyPlayer("Block set to " + var15);
			}
			
			((TileEntityTogglyfier)var12).setHiddenBlock(var13);
			var1.setCount(var1.getCount() - 1);
		}
		else
		{
			if (var1.getItemDamage() == 1)
			{
				return EnumActionResult.FAIL;
			}
			
			if (var1.getItemDamage() == 2)
			{
				if (var3.getBlockState(pos) != Main.changeBlock)
				{
					return EnumActionResult.FAIL;
				}
				
				TileEntityChangeBlock var16 = (TileEntityChangeBlock)var3.getTileEntity(pos);
				EnumFacing newFacing = BlockChangeBlock.getAlignmentByPlayer(pos, var16.getChangeBlock().getAlignment(), var2);
				var16.getChangeBlock().setAlignment(newFacing);
				var3.notifyBlockUpdate(pos, var3.getBlockState(pos), var3.getBlockState(pos).withProperty(FACING, newFacing), 2);
			}
		}
		
		return EnumActionResult.SUCCESS;
	}
	
	/**
	 * called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount
	 */
	@Override
	public void onPlayerStoppedUsing(ItemStack var1, World var2, EntityLivingBase var3, int var4)
	{
		if (var1.getItemDamage() == 1)
		{
			if (var1.hasTagCompound() && var1.getTagCompound().getBoolean("rclick"))
			{
				var1.getTagCompound().setBoolean("rclick", false);
				return;
			}
			
			if (!var2.isRemote)
			{
				return;
			}
			
			Vec3d var5 = new Vec3d(var3.posX, var3.posY, var3.posZ);
			float var6 = MathHelper.cos(-var3.rotationYaw * 0.01745329F - (float)Math.PI);
			float var7 = MathHelper.sin(-var3.rotationYaw * 0.01745329F - (float)Math.PI);
			float var8 = -MathHelper.cos(-var3.rotationPitch * 0.01745329F);
			float var9 = MathHelper.sin(-var3.rotationPitch * 0.01745329F);
			float var10 = var7 * var8;
			float var11 = var6 * var8;
			double var12 = 10.0D;
			Vec3d var14 = var5.add(var10* var12, var9* var12, var11* var12);
			RayTraceResult var15 = var2.rayTraceBlocks(var5, var14, true);
			
			if (var15 != null && var15.typeOfHit == RayTraceResult.Type.BLOCK)
			{
				registerScannerCoordinates(var15.getBlockPos());
			}
		}
	}
	
	@NotNull
	@Override
	public ItemStack onItemUseFinish(ItemStack var1, World var2, EntityLivingBase var3)
	{
		if (var1.getItemDamage() == 1 && !getScannerBlockList().isEmpty())
		{
			getScannerBlockList().clear();
			
			if(!var2.isRemote)
			{
				Main.notifyPlayer("Scanner list cleared");
			}
			
			NBTTagCompound tag = var1.hasTagCompound() ? var1.getTagCompound() : new NBTTagCompound();
			assert tag!=null;
			tag.setBoolean("rclick", true);
			var1.setTagCompound(tag);
		}
		
		return var1;
	}
	
	/**
	 * How long it takes to use or consume an item
	 */
	@Override
	public int getMaxItemUseDuration(ItemStack var1)
	{
		return var1.getItemDamage() == 1 ? 20 : 0;
	}
	
	/**
	 * allows items to add custom lines of information to the mouseover description
	 */
	@Override
	public void addInformation(ItemStack var1, World world, List<String> var2, ITooltipFlag flag)
	{
		if (var1.getItemDamage() == 0)
		{
			NBTTagCompound tagCompound = var1.getTagCompound();
			
			if (tagCompound != null && tagCompound.hasKey("Dim") && tagCompound.hasKey("X") && tagCompound.hasKey("Y") && tagCompound.hasKey("Z"))
			{
				var2.add("Owning Toggle Block:");
				StringBuilder var4 = new StringBuilder("Dimension: ");
				int var5 = tagCompound.getInteger("Dim");
				
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
				var2.add("(" + tagCompound.getInteger("X") + ", " + tagCompound.getInteger("Y") + ", " + tagCompound.getInteger("Z") + ")");
			}
			else
			{
				var2.add("\u00a7cMissing owning Toggle Block information!");
			}
		}
	}
	
	/**
	 * If this function returns true (or the item is damageable), the ItemStack's NBT tag will be sent to the client.
	 */
	@Override
	public boolean getShareTag()
	{
		return true;
	}
	
	public static void removeAssistantFromPlayer(IInventory var0, int var1)
	{
		for (int var2 = 0; var2 < var0.getSizeInventory(); ++var2)
		{
			ItemStack var3 = var0.getStackInSlot(var2);
			
			if (!var3.isEmpty() && var3.getItem() == Main.togglyfierAssistant && (var1 < 0 || var3.getItemDamage() == var1))
			{
				var0.setInventorySlotContents(var2, ItemStack.EMPTY);
			}
		}
	}
	
	public static void removeAssistantFromPlayer(IInventory var0, int var1, TileEntity var2)
	{
		for (int var3 = 0; var3 < var0.getSizeInventory(); ++var3)
		{
			ItemStack var4 = var0.getStackInSlot(var3);
			
			if (!var4.isEmpty() && var4.getItem() == Main.togglyfierAssistant && var1 == var4.getItemDamage())
			{
				NBTTagCompound var5 = var4.getTagCompound();
				
				if (doesNBTCheckOut(var2, var5))
				{
					var0.setInventorySlotContents(var3, ItemStack.EMPTY);
				}
			}
		}
	}
	
	public static boolean doesInventoryHaveAssistant(IInventory var0, int var1)
	{
		for (int var2 = 0; var2 < var0.getSizeInventory(); ++var2)
		{
			ItemStack var3 = var0.getStackInSlot(var2);
			
			if (!var3.isEmpty() && var3.getItem() == Main.togglyfierAssistant && (var1 < 0 || var3.getItemDamage() == var1))
			{
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean doesInventoryHaveAssistant(IInventory var0, int var1, TileEntity var2)
	{
		for (int var3 = 0; var3 < var0.getSizeInventory(); ++var3)
		{
			ItemStack var4 = var0.getStackInSlot(var3);
			
			if (!var4.isEmpty() && var4.getItem() == Main.togglyfierAssistant && var4.getItemDamage() == var1)
			{
				NBTTagCompound var5 = var4.getTagCompound();
				
				if (doesNBTCheckOut(var2, var5))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private static boolean doesNBTCheckOut(final TileEntity var2, final NBTTagCompound var5)
	{
		if(var5==null)
		{
			return false;
		}
		
		if(var5.hasKey("Dim") && var5.hasKey("X") && var5.hasKey("Y") && var5.hasKey("Z"))
		{
			if(var5.getInteger("Dim")==var2.getWorld().provider.getDimensionType().ordinal())
			{
				BlockPos pos = var2.getPos();
				return var5.getInteger("X")==pos.getX() && var5.getInteger("Y")==pos.getY() && var5.getInteger("Z")==pos.getZ();
			}
		}
		
		return false;
	}
	
	private static void registerScannerCoordinates(BlockPos pos)
	{
		ChunkCoordinates var3 = new ChunkCoordinates(pos.getX(), pos.getY(), pos.getZ());
		
		if (getScannerBlockList().contains(var3))
		{
			getScannerBlockList().remove(var3);
		}
		else
		{
			if (getScannerBlockList().size() >= 5)
			{
				Main.notifyPlayer("Only up to 5 blocks can be scanned at once!");
				return;
			}
			
			getScannerBlockList().add(var3);
		}
	}
	
	public static void clearInfo()
	{
		getScannerBlockList().clear();
	}
}
