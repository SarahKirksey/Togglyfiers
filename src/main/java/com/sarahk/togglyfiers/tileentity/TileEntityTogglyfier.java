package com.sarahk.togglyfiers.tileentity;

import com.mojang.authlib.GameProfile;
import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.ModConfig;
import com.sarahk.togglyfiers.blocks.BlockChangeBlock;
import com.sarahk.togglyfiers.blocks.BlockTogglyfier.EnumTogglyfierMode;
import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.items.ModItems;
import com.sarahk.togglyfiers.util.ChunkCoordinates;
import com.sarahk.togglyfiers.util.MutableItemStack;
import com.sarahk.togglyfiers.util.NBTTagType;
import com.sarahk.togglyfiers.util.Packet250CustomPayload;
import com.sarahk.togglyfiers.util.TBChangeBlockInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemRecord;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.registry.RegistryDefaulted;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import static com.sarahk.togglyfiers.blocks.BlockTogglyfier.EnumTogglyfierMode.*;
import static net.minecraft.block.BlockJukebox.HAS_RECORD;
import static net.minecraft.block.BlockJukebox.TileEntityJukebox;
import static net.minecraft.init.Blocks.*;
import static net.minecraft.init.Items.*;
import static net.minecraft.util.EnumFacing.*;

public class TileEntityTogglyfier extends TileEntityInventory implements ITickable
{
	public static final int DEFAULT_INTERNAL_INVENTORY_SIZE = 12;
	public static final Integer[][] PRIORITY_LIST = new Integer[0][0];
	public static final RegistryDefaulted<Item, IBehaviorDispenseItem> TOGGLY_BEHAVIOR_REGISTRY =
		new RegistryDefaulted<>(new DefaultTogglyfyItem());
	
	
	// Does not include potions, as those are a special case. (They are projectiles only if they are splash potions.)
	private static final Item[] projectiles = new Item[] {
		ARROW,
		EGG,
		SNOWBALL,
		EXPERIENCE_BOTTLE,
		FIRE_CHARGE
	};
	public static final float PROJECTILE_VELOCITY_SLOW = 1.1F;
	public static final float PROJECTILE_INACCURACY_HIGH = 6.0F;
	public static final float PROJECTILE_INACCURACY_LOW = 3.0F;
	public static final double HALF = 0.5D;
	public static final float PROJECTILE_VELOCITY_HIGH = 1.375F;
	public static final double BLOCK_HEIGHT_TO_PLAYER_HEIGHT_OFFSET = 2.6D;
	public static final float PLAYER_PITCH_UP = 90.0f;
	public static final float PLAYER_PITCH_DOWN = -90.0f;
	private static final float PLAYER_YAW_NORTH = 0.0f;
	public static final float PLAYER_YAW_SOUTH = 180.0f;
	public static final float PLAYER_YAW_WEST = 270.0f;
	private static final float PLAYER_YAW_EAST = 90.0f;
	
	private final NonNullList<ItemStack> inventory = NonNullList.<ItemStack> withSize(3, ItemStack.EMPTY);
	private final MutableItemStack[] tbSlots;
	private final ArrayList<TBChangeBlockInfo> changeBlocks = new ArrayList<>();
	private final List<ChunkCoordinates> containerRegistry = new ArrayList<>();
	private IInventory replenisher;
	private final Collection<TBChangeBlockInfo> tempTileEntities = new ArrayList<TBChangeBlockInfo>();
	private final Item[] itemsInIDOrder =
		new Item[] {
			IRON_SHOVEL, IRON_PICKAXE, IRON_AXE, FLINT_AND_STEEL, APPLE,
			BOW, ARROW, COAL, DIAMOND, IRON_INGOT,
			GOLD_INGOT, IRON_SWORD, WOODEN_SWORD, WOODEN_SHOVEL, WOODEN_PICKAXE,
			WOODEN_AXE, STONE_SWORD, STONE_SHOVEL, STONE_PICKAXE, STONE_AXE,
			DIAMOND_SWORD, DIAMOND_SHOVEL, DIAMOND_PICKAXE, DIAMOND_AXE, STICK,
			BOWL, MUSHROOM_STEW, GOLDEN_SWORD, GOLDEN_SHOVEL, GOLDEN_PICKAXE,
			GOLDEN_AXE,
			STRING, FEATHER, GUNPOWDER, WOODEN_HOE, STONE_HOE,
			IRON_HOE, DIAMOND_HOE, GOLDEN_HOE, WHEAT_SEEDS, Items.WHEAT,
			BREAD, LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS,
			CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS, IRON_HELMET,
			IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS, DIAMOND_HELMET, DIAMOND_CHESTPLATE,
			DIAMOND_LEGGINGS, DIAMOND_BOOTS, GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS,
			GOLDEN_BOOTS, FLINT, PORKCHOP, COOKED_PORKCHOP, PAINTING,
			GOLDEN_APPLE, SIGN, Items.OAK_DOOR, BUCKET, WATER_BUCKET,
			LAVA_BUCKET, MINECART, SADDLE, Items.IRON_DOOR, REDSTONE,
			SNOWBALL, BOAT, LEATHER, MILK_BUCKET, BRICK,
			CLAY_BALL, Items.REEDS, PAPER, BOOK, SLIME_BALL,
			CHEST_MINECART, FURNACE_MINECART, EGG, COMPASS, FISHING_ROD,
			CLOCK, GLOWSTONE_DUST, FISH, COOKED_FISH, DYE,
			BONE, SUGAR, Items.CAKE, Items.BED, REPEATER,
			COOKIE, MAP, SHEARS, MELON, PUMPKIN_SEEDS,
			MELON_SEEDS, BEEF, COOKED_BEEF, CHICKEN, COOKED_CHICKEN,
			ROTTEN_FLESH, ENDER_PEARL, BLAZE_ROD, GHAST_TEAR, GOLD_NUGGET,
			Items.NETHER_WART,  /*WATER BOTTLE*/ null, GLASS_BOTTLE, SPIDER_EYE, FERMENTED_SPIDER_EYE,
			BLAZE_POWDER, MAGMA_CREAM, Items.BREWING_STAND, Items.CAULDRON, ENDER_EYE,
			SPECKLED_MELON, SPAWN_EGG, EXPERIENCE_BOTTLE, FIRE_CHARGE, WRITABLE_BOOK,
			WRITTEN_BOOK, EMERALD
		};
	private final int recordIDOffset = 2256;
	private final Item[] records =
		new Item[] {
			RECORD_13, RECORD_CAT, RECORD_BLOCKS, RECORD_CHIRP, RECORD_FAR, RECORD_MALL,
			RECORD_MELLOHI, RECORD_STAL, RECORD_STRAD, RECORD_WARD, RECORD_11, RECORD_WAIT
		};
	private boolean isCreative = false;
	@Nonnull private String customName = "";
	private EnumTogglyfierMode mode = EnumTogglyfierMode.EDIT;
	private boolean powered = false;
	private Integer[][] priorityList = PRIORITY_LIST;
	private byte ticksLeft = 4;
	private boolean waitingOnUpdate = false;
	private AtomicBoolean processing = new AtomicBoolean(false);
	private TogglyfierPlayer fakePlayer = null;
	private byte containerCheckRadius = 2;
	private IBlockState hideAsBlock = null;
	private boolean guiNeedsReplenishUpdate = false;
	private short clientChangeBlockCount = 0;
	// This array stores vanilla blocks such that their index is the ID associated with that block back in 1.3.2.
	private final Block[] blocksInIDOrder = new Block[] {
		Blocks.AIR,
		STONE, GRASS, DIRT, COBBLESTONE, PLANKS,
		SAPLING, BEDROCK, FLOWING_WATER, WATER, FLOWING_LAVA,
		LAVA, SAND, GRAVEL, GOLD_ORE, IRON_ORE,
		COAL_ORE, LOG, LEAVES, SPONGE, GLASS,
		LAPIS_ORE, LAPIS_BLOCK, DISPENSER, SANDSTONE, NOTEBLOCK,
		Blocks.BED, GOLDEN_RAIL, DETECTOR_RAIL, STICKY_PISTON, WEB,
		TALLGRASS, DEADBUSH, PISTON, PISTON_HEAD, WOOL,
		PISTON_EXTENSION, YELLOW_FLOWER, RED_FLOWER, BROWN_MUSHROOM, RED_MUSHROOM,
		GOLD_BLOCK, IRON_BLOCK, DOUBLE_STONE_SLAB, STONE_SLAB, BRICK_BLOCK,
		TNT, BOOKSHELF, MOSSY_COBBLESTONE, OBSIDIAN, TORCH,
		FIRE, MOB_SPAWNER, OAK_STAIRS, CHEST, REDSTONE_WIRE,
		DIAMOND_ORE, DIAMOND_BLOCK, CRAFTING_TABLE, Blocks.WHEAT, FARMLAND,
		FURNACE, LIT_FURNACE, STANDING_SIGN, Blocks.OAK_DOOR, LADDER,
		RAIL, STONE_STAIRS, WALL_SIGN, LEVER, STONE_PRESSURE_PLATE,
		Blocks.IRON_DOOR, WOODEN_PRESSURE_PLATE, REDSTONE_ORE, LIT_REDSTONE_ORE, UNLIT_REDSTONE_TORCH,
		REDSTONE_TORCH, STONE_BUTTON, SNOW_LAYER, ICE, SNOW,
		CACTUS, CLAY, Blocks.REEDS, JUKEBOX, OAK_FENCE,
		PUMPKIN, NETHERRACK, SOUL_SAND, GLOWSTONE, PORTAL,
		LIT_PUMPKIN, Blocks.CAKE, UNPOWERED_REPEATER, POWERED_REPEATER,
		null,   //Used to be the "locked chest"
		TRAPDOOR, MONSTER_EGG, STONEBRICK, BROWN_MUSHROOM_BLOCK, RED_MUSHROOM_BLOCK,
		IRON_BARS, GLASS_PANE, MELON_BLOCK, PUMPKIN_STEM, MELON_STEM,
		VINE, OAK_FENCE_GATE, BRICK_STAIRS, STONE_BRICK_STAIRS, MYCELIUM,
		WATERLILY, NETHER_BRICK, NETHER_BRICK_FENCE, NETHER_BRICK_STAIRS, Blocks.NETHER_WART,
		ENCHANTING_TABLE, Blocks.BREWING_STAND, Blocks.CAULDRON, END_PORTAL, END_PORTAL_FRAME,
		END_STONE, DRAGON_EGG, REDSTONE_LAMP, LIT_REDSTONE_LAMP, DOUBLE_WOODEN_SLAB,
		WOODEN_SLAB, COCOA, SANDSTONE_STAIRS, EMERALD_ORE, ENDER_CHEST,
		TRIPWIRE_HOOK, TRIPWIRE, EMERALD_BLOCK, SPRUCE_STAIRS, BIRCH_STAIRS,
		JUNGLE_STAIRS
	};
	private boolean hasHadAChangeToFreakingLoad;
	private final int numberOfChangeBlocks;
	
	// For stability reasons, it's somewhat necessary to keep a no-args constructor for tile entities.
	public TileEntityTogglyfier()
	{
		this(Minecraft.getMinecraft().getIntegratedServer().worlds[Minecraft.getMinecraft().player.dimension]);
	}
	
	public TileEntityTogglyfier(World worldin)
	{
		this(DEFAULT_INTERNAL_INVENTORY_SIZE, worldin, 10);
		
		//TODO: Notify the listener registry about the new TileEntityTogglyfier.
	}
	
	private TileEntityTogglyfier(final int internalInventorySize, World worldIn, int numChangeBlocks)
	{
		super("Togglyfier");
		this.world = worldIn;
		containerCheckRadius = (byte) ModConfig.getInt(ModConfig.TogglyfierOptions.CONTAINER_CHECK_RADIUS);
		numberOfChangeBlocks = numChangeBlocks;
		tbSlots = new MutableItemStack[Math.max(3, internalInventorySize)];
	}
	
	public static void writeItemStackToPacket(ItemStack stack, DataOutputStream stream)
	{
		throw new NotImplementedException("writeItemStackToPacket is not implemented!");
	}
	
	/**
	 * signs and mobSpawners use this to send text and meta-data
	public Packet getAuxillaryInfoPacket()
	{
		ByteArrayOutputStream var1 = new ByteArrayOutputStream();
		DataOutputStream var2 = new DataOutputStream(var1);
		
		try
		{
			var2.writeByte(0);
			var2.writeInt(getPosX());
			var2.writeShort(getPosY());
			var2.writeInt(getPosZ());
			
			writeItemStackToPacket(this.tbSlots[0], var2);
			writeItemStackToPacket(this.tbSlots[1], var2);
			
			var2.writeByte(this.getMode().ordinal());
			var2.writeBoolean(this.isPowered());
			var2.writeShort(this.changeBlocks.size());
			var2.writeByte(this.containerCheckRadius);
			
			if(this.hideAsBlock==null)
			{
				var2.writeShort(-1);
				var2.writeByte(0);
			}
			else
			{
				var2.writeInt(Block.getStateId(hideAsBlock));
			}
			
			var2.close();
			return new Packet250CustomPayload("Togglyfiers", var1.toByteArray());
		}
		catch(IOException var4)
		{
			var4.printStackTrace();
			return null;
		}
	}
	 */
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		SPacketUpdateTileEntity result =new SPacketUpdateTileEntity(this.pos, 9, this.getUpdateTag());
		return new SPacketUpdateTileEntity(this.pos, 9, this.getUpdateTag());
	}
	
	private short getClientChangeBlockCount()
	{
		return clientChangeBlockCount;
	}
	
	public IBlockState getHiddenBlock()
	{
		return this.hideAsBlock;
	}
	
	private int getIndexFromPower()
	{
		return this.isPowered() ? 1 : 0;
	}
	
	public int getMaxChangeBlocks()
	{
		final int result;
		switch(this.getBlockMetadata())
		{
		case 0:
			result = 3;
			break;
		case 1:
			result = 10;
			break;
		case 2:
		case 3:
			result = 25;
			break;
		case 4:
		case 5:
			result = 50;
			break;
		default:
			result = 1;
			break;
		}
		return result;
	}
	
	public EnumTogglyfierMode getMode()                {return mode;}
	
	public int getContainerCheckRadius()
	{
		return containerCheckRadius;
	}
	
	public void setContainerCheckRadius(byte input)
	{
		containerCheckRadius = input;
	}
	
	public void setMode(@Nonnull EnumTogglyfierMode mode)
	{
		this.mode = mode;
		
		if(mode==EnumTogglyfierMode.EDIT)
		{
			tbSlots[2] = new MutableItemStack(makeStackOfChangeBlocks());
		}
		else if(mode==LOCKDOWN)
		{
			final int size = this.changeBlocks.size();
			int var3 = TileEntityChangeBlock.ChangeBlockError.values().length;
			final int[] var4 = new int[var3];
			int var15;
			
			for(var15 = 0; var15<var3; ++var15)
			{
				var4[var15] = 0;
			}
			
			boolean var2 = true;
			for(var15 = 0; var15<size; ++var15)
			{
				TBChangeBlockInfo var6 = this.changeBlocks.get(var15);
				TileEntityChangeBlock var7 = (TileEntityChangeBlock) getWorld().getTileEntity(var6.getBlockPos());
				
				if(var7!=null && !var7.checkForErrors() && (var7.errorState!=TileEntityChangeBlock.ChangeBlockError.CHANGE_BLOCK_EMPTY || !BlockChangeBlock.ignoreEmpty))
				{
					var2 = false;
					getWorld().setBlockState(getPos(), ModBlocks.togglyfier.getStateByMode(mode));
					++var4[var7.errorState.ordinal()];
				}
			}
			
			if(var2)
			{
				this.switchMode();
			}
			else
			{
				StringBuilder var16 = new StringBuilder("Errors were found: ");
				
				for(var15 = 1; var15<var3; ++var15)
				{
					if(var4[var15]>0)
					{
						if(var16.length()>20)
						{
							var16.append(", ");
						}
						
						var16.append(TileEntityChangeBlock.ChangeBlockError.getErrorMessage(var15)).append(" x").append(var4[var15]);
					}
				}
				
				Main.notifyPlayer(var16.toString());
			}
		}
		else if(isReady())
		{
			this.processing.set(true);
			this.updateContainerRegistry();
			this.readFromChangeBlocks();
			this.sortChangeBlocks();
			
			final int size = this.changeBlocks.size();
			int var14 = 0;
			final Collection<NBTTagCompound> var17 = new ArrayList<>();
			int var13;
			
			TBChangeBlockInfo var6 = null;
			for(var13 = 0; var13<size; ++var13)
			{
				var6 = this.changeBlocks.get(var13);
				TileEntityChangeBlock var7 = (TileEntityChangeBlock) getWorld().getTileEntity(var6.getBlockPos());
				
				for(int var8 = 0; var8<2; ++var8)
				{
					if(var6.getCurrent()[var8]!=null && !isCreative() && var6.getCurrent()[var8].getItem()==FLINT_AND_STEEL && var6.getCurrent()[var8].getCount()>0 &&
						   ModConfig.getBoolean(ModConfig.TogglyfierOptions.MOVE_FLINT_AND_STEEL))
					{
						foo:
						for(ChunkCoordinates pos : containerRegistry)
						{
							final TileEntity var11 = getWorld().getTileEntity(pos.toBlockPos());
							
							if(var11 instanceof IInventory)
							{
								IInventory var12 = var11 instanceof TileEntityTogglyfier ? getReplenisher() : (IInventory) var11;
								
								for(int sizeInventory = var12.getSizeInventory(); var14<sizeInventory; ++var14)
								{
									if((var12).getStackInSlot(var14).isEmpty())
									{
										var12.setInventorySlotContents(var14, var6.getCurrent()[var8].toItemStack());
										var6.getCurrent()[var8].setCount(0);
										++var14;
										break foo;
									}
								}
								var14 = 0;
							}
						}
					}
					
					assert var7!=null;
					if((var7.errorState==TileEntityChangeBlock.ChangeBlockError.WRONG_CONTAINER_TYPE ||
							var7.errorState==TileEntityChangeBlock.ChangeBlockError.CHANGE_BLOCK_EMPTY) && var6.getContainerNBT()[var8]!=null)
					{
						Item var19 = var6.getCurrent()[var8]!=null ? var6.getCurrent()[var8].getItem() : Items.AIR;
						
						if(Item.getItemFromBlock(ModBlocks.togglyfier) != var19)
						{
							var17.add(var6.getContainerNBT()[var8]);
							(this.changeBlocks.get(var13)).getContainerNBT()[var8] = null;
						}
					}
					
					var7.setInventorySlotContents(var8, ItemStack.EMPTY);
				}
				
				var7.setDeregisterOnDelete(false);
				getWorld().setBlockState(var6.getBlockPos(), ModBlocks.togglyfier.getStateByMode(mode));
			}
			
			for(var13 = 0; var13<2; ++var13)
			{
				Main.dropItemStack(getWorld(), this.getStackInSlot(var13), getPos());
				this.setInventorySlotContents(var13, ItemStack.EMPTY);
			}
			
			for(NBTTagCompound nbt : var17)
			{
				this.dropItemsFromNBT(nbt);
			}
			
			this.setPowered(getWorld().isBlockIndirectlyGettingPowered(getPos())>0);
			this.waitingOnUpdate = false;
			this.placeBlocks();
			this.ticksLeft = 4;
			this.processing.set(false);
		}
		else
		{
			final int size = this.changeBlocks.size();
			int var3;
			TBChangeBlockInfo var6;
			TileEntityChangeBlock var7;
			int x = 1/0;    // Yes, this throws an exception. That's the point.
		}
		
		
		getWorld().setBlockState(getPos(), ModBlocks.togglyfier.getStateByMode(mode));
	}
	
	@NotNull
	@Override
	public String getName()
	{
		return this.hasCustomName() ? customName : "container.togglyfier";
	}
	
	@Override
	public boolean hasCustomName()
	{
		return !customName.isEmpty();
	}
	
	public List<ItemStack> getNeedsReplenishList()
	{
		List<ItemStack> var1 = new ArrayList<>();
		
		if(isReady() && !isCreative())
		{
			ItemStack var10;
			
			for(TBChangeBlockInfo changeBlockInfo : this.changeBlocks)
			{
				for(int var4 = 0; var4<2; ++var4)
				{
					MutableItemStack var5 = changeBlockInfo.getCurrent()[var4];
					
					if(var5!=null && var5.getCount()<=0)
					{
						boolean var6 = !var5.getItem().getHasSubtypes();
						Block var7 = getWorld().getBlockState(changeBlockInfo.getBlockPos()).getBlock();
						
						if(var4!=this.getIndexFromPower() || !ModConfig.isExpectedBlock(var5.toItemStack(), var7)
							   || (!var6 && var7.damageDropped(getWorld().getBlockState(changeBlockInfo.getBlockPos()))!=var5.getItemDamage()))
						{
							boolean var8 = true;
							
							for(final ItemStack itemStack : var1)
							{
								var10 = itemStack;
								
								if(var10.getItem()==var5.getItem() && (var6 || var10.getItemDamage()==var5.getItemDamage()))
								{
									var10.setCount(var10.getCount() + 1);
									var8 = false;
									break;
								}
							}
							
							if(var8)
							{
								var1.add(new ItemStack(var5.getItem(), 1, var6 ? 0 : var5.getItemDamage()));
							}
						}
					}
				}
			}
			
			this.checkContainerRegistry();
			Iterator var11 = var1.iterator();
			
			while(var11.hasNext())
			{
				ItemStack var13 = (ItemStack) var11.next();
				boolean var15 = !var13.getItem().getHasSubtypes();
				Iterator<ChunkCoordinates> var16 = this.containerRegistry.iterator();
				
				while(var16.hasNext())
				{
					ChunkCoordinates var17 = var16.next();
					TileEntity var18 = getWorld().getTileEntity(var17.toBlockPos());
					
					if(var18 instanceof IInventory)
					{
						Object var20 = var18 instanceof TileEntityTogglyfier ? getReplenisher() : (IInventory) var18;
						
						for(int var19 = 0; var19<((IInventory) var20).getSizeInventory(); ++var19)
						{
							var10 = ((IInventory) var20).getStackInSlot(var19);
							
							if(var10!=null && var10.getItem()==var13.getItem() && (var15 || var10.getItemDamage()==var13.getItemDamage()))
							{
								var13.setCount(var13.getCount() - var10.getCount());
								
								if(var13.getCount()<=0)
								{
									break;
								}
							}
						}
						
						if(var13.getCount()<=0)
						{
							break;
						}
					}
				}
				
				if(var13.getCount()<=0)
				{
					var11.remove();
				}
			}
		}
		
		byte var12 = 9;
		
		for(int var14 = 0; var14<getReplenisher().getSizeInventory(); ++var14)
		{
			if(!getReplenisher().getStackInSlot(var14).isEmpty())
			{
				--var12;
			}
		}
		
		while(var1.size()>var12)
		{
			var1.remove(var1.size() - 1);
		}
		
		this.guiNeedsReplenishUpdate = false;
		return var1;
	}
	
	public int getNumberOfChangeBlocks()
	{
		return getWorld().isRemote ? this.getClientChangeBlockCount() : this.changeBlocks.size();
	}
	
	private int getPosX()	{ return pos.getX(); }
	private int getPosY()   { return pos.getY(); }
	private int getPosZ()   { return pos.getZ(); }
	
	public boolean getPowered()
	{
		return this.isPowered();
	}
	
	public static int getSizeInternalInventory()
	{
		return 12;
	}
	
	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory()
	{
		return TileEntityTogglyfier.getSizeInternalInventory() + (this.changeBlocks.size() << 1);
	}
	
	@Override
	public boolean isEmpty()
	{
		for(final ItemStack stack : this.inventory)
		{
			if(!stack.isEmpty())
			{
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Returns the stack in slot i
	 */
	@Override
	@NotNull
	public ItemStack getStackInSlot(int var1)
	{
		ItemStack result = ItemStack.EMPTY;
		if(var1 < TileEntityTogglyfier.getSizeInternalInventory())
		{
			if(this.tbSlots[var1]==null)
			{
				this.tbSlots[var1] = new MutableItemStack(ItemStack.EMPTY);
			}
			result = this.tbSlots[var1].toItemStack();
		}
		else
		{
			var1 -= TileEntityTogglyfier.getSizeInternalInventory();
			result = (this.changeBlocks.get(var1/2)).getCurrent()[var1%2].toItemStack();
		}
		return result;
	}
	
	/**
	 * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
	 * new stack.
	 */
	@NotNull
	@Override
	public ItemStack decrStackSize(int var1, int var2)
	{
		@NotNull MutableItemStack result = new MutableItemStack(ItemStack.EMPTY);
		MutableItemStack var3;
		
		if(var1<TileEntityTogglyfier.getSizeInternalInventory())        // Items in the togglyfier
		{
			var3 = this.tbSlots[var1];
			
			if(var3!=null)
			{
				if(var3.getCount()<=var2)
				{
					var2 = var3.getCount();
				}
				
				ItemStack temp = var3.toItemStack();
				var3.set(temp.splitStack(var2));
				this.tbSlots[var1].set(temp);
				
				if(this.tbSlots[var1].getCount()==0)
				{
					this.tbSlots[var1] = null;
				}
				
				this.onInventoryChanged();
				result = var3;
			}
		}
		else    // Items in change blocks
		{
			var1 -= TileEntityTogglyfier.getSizeInternalInventory();
			var3 = (this.changeBlocks.get(var1/2)).getCurrent()[var1%2];
			
			if(var3!=null)
			{
				if(var3.getCount()<var2)
				{
					var2 = var3.getCount();
				}
				
				var3.set((this.changeBlocks.get(var1/2)).getCurrent()[var1%2].splitStack(var2));
				
				if((this.changeBlocks.get(var1/2)).getCurrent()[var1%2].getCount()==0)
				{
					(this.changeBlocks.get(var1/2)).getCurrent()[var1%2] = null;
				}
				
				this.onInventoryChanged();
				result = var3;
			}
		}
		return result.toItemStack();
	}
	
	//@Override    public ItemStack decrStackSize(int index, int count) {return ItemStackHelper.getAndSplit(inventory, index, count);}
	@NotNull
	@Override
	public ItemStack removeStackFromSlot(int index) {return ItemStackHelper.getAndRemove(inventory, index);}
	
	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(int index, @NotNull ItemStack stack)
	{
		if(stack.getItem()!=ModItems.togglyfierAssistant)
		{
			final ItemStack oldStack = inventory.get(index);
			boolean isStackable = !stack.isEmpty() && stack.isItemEqual(oldStack) && ItemStack.areItemStackTagsEqual(stack, oldStack);
			
			if(index<TileEntityTogglyfier.getSizeInternalInventory())
			{
				this.inventory.set(index, stack);
			}
			else
			{
				index -= TileEntityTogglyfier.getSizeInternalInventory();
				(this.changeBlocks.get(index/2)).getCurrent()[index%2].set(stack);
			}
			
			if(stack.getCount()>this.getInventoryStackLimit())
			{
				stack.setCount(this.getInventoryStackLimit());
			}
			
			this.onInventoryChanged();
		}
	}
	
	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}
	
	@Override
	public boolean isUsableByPlayer(@NotNull EntityPlayer var1)
	{
		return getWorld().getTileEntity(pos)==this && var1.getDistanceSq(pos)<=64.0D;
	}
	
	// These are defined here because the methods have no default behavior.
	@Override
	public void openInventory(@NotNull EntityPlayer player) {}
	
	@Override
	public void closeInventory(@NotNull EntityPlayer player) {}
	
	@Override
	public boolean isItemValidForSlot(final int index, @NotNull final ItemStack stack)
	{
		assert(index < 3);
		return index==0 ? stack.getItem()==Item.getItemFromBlock(ModBlocks.changeBlock) : stack.getItem()!=Item.getItemFromBlock(ModBlocks.togglyfier);
	}
	
	@Override public void clear() 								{ inventory.clear(); }
	
	/**
	 * Reads a tile entity from NBT.
	 */
	@Override
	public void readFromNBT(final NBTTagCompound compound)
	{
		super.readFromNBT(compound);

		/*
		super.readFromNBT(compound);

        inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, inventory);
        if(compound.hasKey("CustomName, 8")) {
            this.setCustomName(compound.getString("CustomName"));
        }
        if(compound.hasKey("TogglyMode")) {
            this.mode = EnumTogglyfierMode.values()[compound.getInteger("TogglyMode")];
        }
		 */
		
		final NBTTagList var3 = compound.getTagList("Items", NBTTagType.Compound.ordinal());
		
		int tagCount = var3.tagCount();
		for(int var4 = 0; var4<tagCount; ++var4)
		{
			final NBTTagCompound var5 = var3.getCompoundTagAt(var4);
			final byte var6 = var5.getByte("Slot");
			
			if(var6 >= 0 && var6<this.tbSlots.length)
			{
				if(this.tbSlots[var6] != null)
				{
					this.tbSlots[var6].set(new ItemStack(var5));
				}
				else
				{
					this.tbSlots[var6] = new MutableItemStack(var5);
				}
			}
		}
		
		NBTTagList var8 = compound.getTagList("ChangeBlocks", NBTTagType.Compound.ordinal());
		
		final int tagCount2 = var8.tagCount();
		for(int var9 = 0; var9<tagCount2; ++var9)
		{
			final NBTTagCompound var11 = var8.getCompoundTagAt(var9);
			final TBChangeBlockInfo changeBlockInfo = new TBChangeBlockInfo(var11, this);
			this.changeBlocks.add(changeBlockInfo);
		}
		
		final byte var10 = compound.getByte("CurrentState");
		this.mode = EnumTogglyfierMode.values()[var10];
		if(isReady())
		{
			this.sortChangeBlocks();
		}
		
		this.setPowered(compound.getBoolean("IsPowered"));
		this.containerCheckRadius = compound.getByte("CheckRadius");
		
		if(this.containerCheckRadius==0)
		{
			this.containerCheckRadius = (byte) ModConfig.getInt(ModConfig.TogglyfierOptions.CONTAINER_CHECK_RADIUS);
		}
		
		if(compound.hasKey("HideAsBlock"))
		{
			this.hideAsBlock = Block.getStateById(compound.getInteger("HideAsBlock"));
		}
	}
	
	/**
	 * Writes a tile entity to NBT.
	 *
	 * @return the modified compound tag
	 */
	@NotNull
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound)
	{
		super.writeToNBT(compound);

		/*
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, inventory);
        if(hasCustomName()) compound.setString("CustomName", customName);
        compound.setInteger("TogglyMode", mode.ordinal());
        return compound;
		 */
		
		NBTTagList var2 = new NBTTagList();
		
		for(int var3 = 0; var3<this.tbSlots.length; ++var3)
		{
			if(this.tbSlots[var3]!=null)
			{
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) var3);
				this.tbSlots[var3].toItemStack().writeToNBT(var4);
				var2.appendTag(var4);
			}
		}
		
		compound.setTag("Items", var2);
		NBTTagList var6 = new NBTTagList();
		
		for(TBChangeBlockInfo block : this.changeBlocks)
		{
			NBTTagCompound var5 = new NBTTagCompound();
			block.writeToNBT(var5, this);
			var6.appendTag(var5);
		}
		
		compound.setTag("ChangeBlocks", var6);
		byte var8 = (byte) getMode().ordinal();
		
		compound.setByte("CurrentState", var8);
		compound.setBoolean("IsPowered", this.isPowered());
		
		if(this.containerCheckRadius>0)
		{
			compound.setByte("CheckRadius", this.containerCheckRadius);
		}
		
		if(this.hideAsBlock!=null)
		{
			new NBTTagCompound();
			compound.setInteger("HideAsBlock", Block.getStateId(hideAsBlock));
		}
		
		ModConfig.saveBlockItemSettings();
		return compound;
	}
	
	/**
	 * validates a tile entity
	 */
	@Override
	public void validate()
	{
		super.validate();
		
		if(tbSlots[2] != null && !tbSlots[2].toItemStack().isEmpty() && this.tbSlots[2].getTagCompound() == null)
		{
			NBTTagCompound var1 = new NBTTagCompound();
			var1.setInteger("Dim", getWorld().provider.getDimension());
			var1.setInteger("X", getPosX());
			var1.setInteger("Y", getPosY());
			var1.setInteger("Z", getPosZ());
			this.tbSlots[2].setTagCompound(var1);
		}
		
		if(hasHadAChangeToFreakingLoad && this.fakePlayer == null)
		{
			this.fakePlayer = getFakePlayer();
		}
	}
	
	@NotNull
	@Override
	public ITextComponent getDisplayName()
	{
		return hasCustomName() ?
			new TextComponentString(getName()) :
			new TextComponentTranslation(getName());
	}
	
	private boolean isCreative()
	{
		return isCreative;
	}
	
	public void setCreative(boolean creative)
	{
		isCreative = creative;
	}
	
	public boolean isEditing()
	{
		return this.getMode()==EnumTogglyfierMode.EDIT;
	}
	
	public boolean isPowered()
	{
		return powered;
	}
	
	public void setPowered(boolean powered)
	{
		this.powered = powered;
	}
	
	public boolean isProcessing()
	{
		return this.processing.get();
	}
	
	public boolean isReady() { return getMode()==EnumTogglyfierMode.ACTIVATED || getMode()==EnumTogglyfierMode.DEACTIVATED || getMode()==EnumTogglyfierMode.MODE_READY; }
	
	public void checkPoweredState()
	{
		if(isReady())
		{
			boolean var1 = getWorld().isBlockIndirectlyGettingPowered(getPos())>0;
			
			if(this.isPowered()!=var1)
			{
				if(this.processing.get())
				{
					this.waitingOnUpdate = true;
				}
				else
				{
					this.processing.set(true);
					this.retrieveBlocks();
					this.setPowered(var1);
					this.checkContainerRegistry();
					this.placeBlocks();
					getWorld().setBlockState(getPos(), ModBlocks.togglyfier.getStateByMode(var1 ? ACTIVATED : DEACTIVATED));
					this.ticksLeft = 4;
					this.processing.set(false);
				}
			}
		}
	}
	
	public void deregisterChangeBlock(int var1, int var2, int var3)
	{
		if(!isReady())
		{
			int changeBlockIndex = this.findChangeBlock(var1, var2, var3);
			NBTTagCompound[] var5 = new NBTTagCompound[2];
			int var6;
			
			if(changeBlockIndex >= 0)
			{
				for(var6 = 0; var6<2; ++var6)
				{
					var5[var6] = (this.changeBlocks.get(changeBlockIndex)).getContainerNBT()[var6];
				}
				
				this.changeBlocks.remove(changeBlockIndex);
			}
			
			for(var6 = 0; var6<2; ++var6)
			{
				this.dropItemsFromNBT(var5[var6]);
			}
		}
	}
	
	public int findChangeBlock(final int x, final int y, final int z)
	{
		final int size = this.changeBlocks.size();
		for(int changeBlockIndex = 0; changeBlockIndex<size; changeBlockIndex++)
		{
			final TBChangeBlockInfo changeBlock = this.changeBlocks.get(changeBlockIndex);
			final BlockPos pos = changeBlock.getBlockPos();
			
			if(pos.getX()==x && pos.getY()==y && pos.getZ()==z)
			{
				return changeBlockIndex;
			}
		}
		
		return -1;
	}
	
	@Nullable
	public ItemStack findItemInContainer(Item targetItem, int targetMeta, int targetAmount)
	{
		boolean targetUndefined = (targetItem==null || targetItem==Items.AIR);
		boolean matchesAnyMeta = targetUndefined || (!targetItem.getHasSubtypes()) || targetMeta<0;
		boolean var5 = false;
		
		ItemStack result = null;
		for(ChunkCoordinates var6 : this.containerRegistry)
		{
			TileEntity tileEntity = getWorld().getTileEntity(var6.toBlockPos());
			
			if(tileEntity instanceof IInventory)
			{
				IInventory inv = tileEntity instanceof TileEntityTogglyfier ? getReplenisher() : (IInventory) tileEntity;
				
				int sizeInventory = inv.getSizeInventory();
				for(int slotIndex = 0; slotIndex<sizeInventory; ++slotIndex)
				{
					ItemStack var10 = inv.getStackInSlot(slotIndex);
					
					if(!var10.isEmpty())
					{
						if(targetUndefined)
						{
							return isCreative() ? new ItemStack(var10.getItem(), 1, var10.getItemDamage()) : inv.decrStackSize(slotIndex, targetAmount);
						}
						
						if(var10.getItem()==targetItem && (matchesAnyMeta==true || targetMeta==var10.getItemDamage()))
						{
							if(targetAmount==0)
							{
								return var10;
							}
							
							if(targetAmount==-1)
							{
								if(!isCreative())
								{
									var10.damageItem(1, null);
									
									if(var10.getCount()<=0)
									{
										inv.setInventorySlotContents(slotIndex, ItemStack.EMPTY);
									}
								}
								
								return var10;
							}
							
							if(targetAmount>0)
							{
								return isCreative() ? new ItemStack(var10.getItem(), targetAmount, var10.getItemDamage()) : inv.decrStackSize(slotIndex, targetAmount);
							}
						}
					}
				}
			}
		}	// Break outer
		
		return result;
	}
	
	@Nonnull
	public ItemStack findFirstInContainer(@Nonnull IInventory inventory, @Nonnull Predicate<ItemStack> isTarget, int requiredAmount)
	{
		inventory = inventory instanceof TileEntityTogglyfier ? getReplenisher() : inventory;
		
		int sizeInventory = inventory.getSizeInventory();
		for(int var9 = 0; var9<sizeInventory; ++var9)
		{
			ItemStack var10 = inventory.getStackInSlot(var9);
			
			if(!var10.isEmpty())
			{
				if(isTarget.test(var10))
				{
					if(requiredAmount==0)
					{
						return var10;
					}
					
					if(requiredAmount==-1)
					{
						if(!isCreative())
						{
							var10.damageItem(1, getFakePlayer());
							
							if(var10.getCount()<=0)
							{
								inventory.setInventorySlotContents(var9, ItemStack.EMPTY);
							}
						}
						
						return var10;
					}
					
					if(requiredAmount>0)
					{
						return isCreative() ? new ItemStack(var10.getItem(), requiredAmount, var10.getItemDamage()) : inventory.decrStackSize(var9, requiredAmount);
					}
				}
			}
		}
		return ItemStack.EMPTY;
	}
	
	public ItemStack findHoeInContainer()
	{
		ItemStack result = null;
		
		for(ChunkCoordinates var6 : this.containerRegistry)
		{
			TileEntity var7 = getWorld().getTileEntity(var6.toBlockPos());
			
			if(var7 instanceof IInventory)
			{
				// -1 for the required amount is used to indicate that the item simply needs to be USED.
				// The item is never taken from the associated inventory.
				result = findFirstInContainer((IInventory) var7, (stack)-> stack.getItem() instanceof ItemHoe, -1);
				if(!result.isEmpty())
				{
					return result;
				}
			}
		}	// Break outer
		
		return result;
	}
	
	public TBChangeBlockInfo getChangeBlock(final BlockPos var1)
	{
		return this.getChangeBlock(findChangeBlock(var1.getX(), var1.getY(), var1.getZ()));
	}
	
	public TBChangeBlockInfo getChangeBlock(final int var1)
	{
		if(var1 >= 0 && var1<this.changeBlocks.size())
		{
			return this.changeBlocks.get(var1);
		}
		else
		{
			Main.notifyPlayer("Index out of bounds! Size is " + this.changeBlocks.size() + " Index is " + var1);
			return null;
		}
	}
	
	public boolean doesGuiNeedReplenishUpdate()
	{
		return this.guiNeedsReplenishUpdate;
	}
	
	public boolean isOwningTogglyfier(ItemStack stack)
	{
		if(BlockChangeBlock.stackHasTogglyfierInfo(stack))
		{
			NBTTagCompound var2 = stack.getTagCompound();
			return var2!=null && var2.getInteger("Dim")==getWorld().provider.getDimension() && var2.getInteger("X")==getPosX() && var2.getInteger("Y")==getPosY() &&
					   var2.getInteger("Z")==getPosZ();
		}
		else
		{
			return false;
		}
	}
	
	public ItemStack makeTogglyfierSpoofer()
	{
		ItemStack var1 = new ItemStack(ModItems.togglyfierAssistant, 1, 0);
		NBTTagCompound var2 = new NBTTagCompound();
		var2.setInteger("Dim", getWorld().provider.getDimension());
		var2.setInteger("X", getPosX());
		var2.setInteger("Y", getPosY());
		var2.setInteger("Z", getPosZ());
		var1.setTagCompound(var2);
		return var1;
	}
	
	/**
	 * Called when an the contents of an Inventory change, usually
	 */
	public void onInventoryChanged()
	{
		if(this.isReady() && !getWorld().isRemote)
		{
			Collection<TogglyfierPlayer> var2 = new ArrayList<>();
			
			for(EntityPlayer var3 : getWorld().playerEntities)
			{
				if(var3 instanceof TogglyfierPlayer)
				{
					if(((ContainerTogglyfier) var3.inventoryContainer).getTogglyfier()==this)
					{
						var2.add((TogglyfierPlayer) var3);
					}
				}
			}
			
			if(!var2.isEmpty())
			{
				List<ItemStack> var10 = this.getNeedsReplenishList();
				ByteArrayOutputStream var4 = new ByteArrayOutputStream();
				DataOutputStream var5 = new DataOutputStream(var4);
				
				try
				{
					var5.writeByte(2);
					var5.writeByte(var10.size());
					
					for(final ItemStack itemStack : var10)
					{
						writeItemStackToPacket(itemStack, var5);
					}
					
					Packet250CustomPayload var11 = new Packet250CustomPayload("Togglyfiers", var4.toByteArray());
					var5.close();
					
					for(final EntityPlayerMP var8 : var2)
					{
						Minecraft.getMinecraft().getConnection().handleCustomPayload(var11);
					}
				}
				catch(IOException var9)
				{
					var9.printStackTrace();
				}
			}
		}
		markDirty();
	}
	
	public String capacityUsage()
	{
		return "" + getNumberOfChangeBlocks() + '/' + getMaxChangeBlocks() + " change blocks";
	}
	
	public void prepareForDestruction()
	{
		for(TBChangeBlockInfo changeBlockInfo : this.changeBlocks)
		{
			for(int var3 = 0; var3<2; ++var3)
			{
				MutableItemStack var4 = changeBlockInfo.getCurrent()[var3];
				
				if(var4!=null && Block.getBlockFromItem(var4.getItem())==JUKEBOX
					   && changeBlockInfo.getSavedMetadata()[var3]>0 && changeBlockInfo.getContainerNBT()[var3]!=null)
				{
					String var5 = changeBlockInfo.getContainerNBT()[var3].getString("Record");
					Item var6 = Item.getByNameOrId(var5);
					assert var6!=null;
					Main.dropItemStack(getWorld(), new ItemStack(var6, 1, 0), getPos(), changeBlockInfo.getAlignment());
				}
			}
		}
	}
	
	public boolean registerChangeBlock(BlockPos pos)
	{
		if(this.changeBlocks.size() >= this.getMaxChangeBlocks())    // We've already registered as many as we can.
		{
			return false;
		}
		else if(this.findChangeBlock(pos) >= 0)    // We've already registered a change block at that location.
		{
			return false;
		}
		else
		{
			this.changeBlocks.add(new TBChangeBlockInfo(pos));
			return true;
		}
	}
	
	private void retrieveBlocks()
	{
		int var1 = this.getIndexFromPower();
		
		if(isReady() && !this.changeBlocks.isEmpty())
		{
			for(int var2 = this.priorityList[var1].length - 1; var2 >= 0; --var2)
			{
				int var3 = this.priorityList[var1][var2];
				TBChangeBlockInfo changeBlock = this.changeBlocks.get(var3);
				MutableItemStack pullStack = changeBlock.getCurrent()[var1];
				Item itemID = pullStack.getItem();
				
				BlockPos pos = changeBlock.getBlockPos();
				IBlockState blockState = getWorld().getBlockState(pos);
				Block block = blockState.getBlock();
				
				if(pullStack.getItem()!=Items.AIR && !changeBlock.getOverride(var1))
				{
					int blockMeta = blockState.getBlock().getMetaFromState(blockState);
					int var9 = ModConfig.getItemFlags(pullStack.toItemStack());
					NBTTagCompound var11;
					
					if(ModConfig.isExpectedBlock(pullStack.toItemStack(), block)
						&& (!Item.getItemFromBlock(block).getHasSubtypes()
							|| block.damageDropped(blockState)==pullStack.getItemDamage()))
					{
						this.incrStack(pullStack);
						int var10 = var9 & 15;
						changeBlock.getSavedMetadata()[var1] = (byte) (blockMeta & var10);
						
						if(Block.getBlockFromItem(itemID)!=block
							   && (itemID instanceof ItemBlock || ModConfig.getExpectedBlockCount(pullStack.toItemStack())>1)
							   && (var9 & ModConfig.ItemFlags.IS_DOUBLE_BLOCK.getBitValue())==0 && (var9 & ModConfig.ItemFlags.REQUIRES_TILLED_SOIL.getBitValue())==0)
						{
							changeBlock.getExpectedBlock()[var1] = block;
						}
						
						if(block instanceof BlockContainer)
						{
							var11 = new NBTTagCompound();
							TileEntity var16 = getWorld().getTileEntity(pos);
							assert var16!=null;
							var16.writeToNBT(var11);
							var11.setString("Togglyfiers_BlockID", block.getRegistryName().toString());
							var11.setByte("Togglyfiers_Metadata", (byte) blockMeta);
							boolean var13 = var16 instanceof IInventory;
							
							if(var13)
							{
								IInventory var14 = (IInventory) var16;
								
								for(int var15 = 0; var15<var14.getSizeInventory(); ++var15)
								{
									var14.setInventorySlotContents(var15, ItemStack.EMPTY);
								}
								
								var11.setBoolean("Togglyfiers_IInventory", var13);
							}
							
							changeBlock.getContainerNBT()[var1] = var11;
						}
						
						if(itemID==Item.getItemFromBlock(JUKEBOX))
						{
							getWorld().playRecord(pos, null);
							TileEntityJukebox var17 = (TileEntityJukebox) getWorld().getTileEntity(pos);
							assert var17!=null;
							var17.setRecord(ItemStack.EMPTY);
							getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
						}
					}
					else if(blockState.getBlock()==WATER && blockMeta==0 && itemID==Items.BUCKET)
					{
						pullStack.setItem(WATER_BUCKET);
					}
					else if(blockState.getBlock()==LAVA && blockMeta==0 && itemID==Items.BUCKET)
					{
						pullStack.setItem(LAVA_BUCKET);
					}
					else if(block==OBSIDIAN && itemID==Items.BUCKET)
					{
						pullStack.setItem(LAVA_BUCKET);
					}
					else if((block==STANDING_SIGN || block==WALL_SIGN) && itemID==Items.SIGN)
					{
						this.incrStack(pullStack);
						var11 = new NBTTagCompound();
						TileEntity var12 = getWorld().getTileEntity(pos);
						assert var12!=null;
						var12.writeToNBT(var11);
						var11.setString("Togglyfiers_BlockID", Items.SIGN.getRegistryName().toString());
						changeBlock.getContainerNBT()[var1] = var11;
					}
					else if(block!=Blocks.AIR)
					{
						block.dropBlockAsItem(getWorld(), pos, blockState, 0);
					}
				}
				else
				{
					if(blockState!=Blocks.AIR.getDefaultState())
					{
						blockState.getBlock().dropBlockAsItem(getWorld(), pos, getWorld().getBlockState(pos), 0);
					}
				}
				
				getWorld().setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
	}
	
	public void setClientTileEntityInfo(EnumTogglyfierMode var1, boolean powered, short clientChangeBlockCount)
	{
		this.mode = var1;
		this.setPowered(powered);
		this.clientChangeBlockCount = clientChangeBlockCount;
	}
	
	public void setCustomName(final String customName) {this.customName = customName;}
	
	public void setHiddenBlock(Block var1, int var2)
	{
		if(var1==Blocks.AIR)
		{
			this.hideAsBlock = null;
		}
		else
		{
			this.hideAsBlock = var1.getStateFromMeta(var2);
		}
		
		getWorld().scheduleUpdate(getPos(), ModBlocks.togglyfier, 0);
	}
	
	private void switchMode()
	{
		if(this.mode==EnumTogglyfierMode.EDIT)
		{
			this.readFromChangeBlocks();
			this.setMode(LOCKDOWN);
		}
		else if(this.mode==LOCKDOWN)
		{
			for(TBChangeBlockInfo var2 : this.changeBlocks)
			{
				TileEntity var3 = getWorld().getTileEntity(var2.getBlockPos());
				
				if(var3!=null && var3 instanceof TileEntityChangeBlock)
				{
					((TileEntityChangeBlock)var3).forceReadyMode();
				}
			}
			
			this.setMode(EnumTogglyfierMode.MODE_READY);
		}
		else
		{
			this.processing.set(true);
			this.retrieveBlocks();
			this.waitingOnUpdate = false;
			
			for(TBChangeBlockInfo var2 : changeBlocks)
			{
				getWorld().setBlockState(var2.getBlockPos(), var2.toBlockState());
				TileEntityChangeBlock var3 = (TileEntityChangeBlock) getWorld().getTileEntity(var2.getBlockPos());
				
				if(var3==null)
				{
					this.tempTileEntities.add(var2);
					continue;
				}
				
			
				for(int var4 = 0; var4<2; ++var4)
				{
					MutableItemStack var5 = var2.getCurrent()[var4];
					
					if(var5!=null)
					{
						if(var5.getCount()>0)
						{
							var3.setInventorySlotContents(var4, var5.toItemStack());
						}
						else
						{
							var3.setReplenish(var4, var5);
						}
					}
					
					var3.setOverride(var4, var2.getOverride(var4));
				}
				
				var3.getChangeBlock().setAlignment(var2.getAlignment());
				var3.setTogglyfierCoords(new ChunkCoordinates(getPos()));
			}
			
			this.setMode(EnumTogglyfierMode.EDIT);
			this.processing.set(false);
		}
		
		getWorld().scheduleUpdate(getPos(), ModBlocks.togglyfier, 0);
	}
	
	public void updateContainerRegistry()
	{
		this.containerRegistry.clear();
		this.containerRegistry.add(new ChunkCoordinates(getPosX(), getPosY(), getPosZ()));
		
		for(int var1 = -this.containerCheckRadius; var1<=this.containerCheckRadius; ++var1)
		{
			for(int var2 = -this.containerCheckRadius; var2<=this.containerCheckRadius; ++var2)
			{
				if(ModConfig.isInContainerList(getWorld().getBlockState(new BlockPos(getPosX() + var1, getPosY(), getPosZ() + var2)).getBlock()))
				{
					this.containerRegistry.add(new ChunkCoordinates(getPosX() + var1, getPosY(), getPosZ() + var2));
				}
			}
		}
	}
	
	/**
	 * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner uses this to count
	 * ticks and creates a new spawn inside its implementation.
	 */
	@Override
	public void update()
	{
		if(getWorld().isRemote)
		{
			return;
		}
		
		hasHadAChangeToFreakingLoad = true;
		
		if(isReady())
		{
			if(!this.processing.get())
			{
				int var1 = this.getIndexFromPower();
				TBChangeBlockInfo var3;
				
				if(this.ticksLeft==0 && !this.waitingOnUpdate && getWorld().getWorldTime()%10L==0L)
				{
					for(TBChangeBlockInfo block : this.changeBlocks)
					{
						var3 = block;
						MutableItemStack var4 = var3.getCurrent()[var1];
						
						if(var4!=null)
						{
							Block var5 = getWorld().getBlockState(var3.getBlockPos()).getBlock();
							
							if(var5!=Blocks.AIR.getDefaultState()
								   && !ModConfig.isExpectedBlock(var4.toItemStack(), var5)
								   && ModConfig.couldBeExpectedBlock(var4.toItemStack(), Item.getItemFromBlock(var5)))
							{
								ModConfig.addExpectedBlock(var4.toItemStack(), var5);
							}
						}
					}
				}
				
				if(!this.tempTileEntities.isEmpty())
				{
					Iterator<TBChangeBlockInfo> var8 = this.tempTileEntities.iterator();
					
					while(var8.hasNext())
					{
						var3 = var8.next();
						TileEntity var11 = getWorld().getTileEntity(var3.getBlockPos());
						
						if(var11!=null)
						{
							var11.readFromNBT(var3.getContainerNBT()[var1]);
							var3.getContainerNBT()[var1] = null;
							var8.remove();
						}
					}
				}
				else if(this.ticksLeft>0 && --this.ticksLeft==0 && this.waitingOnUpdate)
				{
					this.waitingOnUpdate = false;
					this.checkPoweredState();
				}
			}
		}
		else if(!this.tempTileEntities.isEmpty())
		{
			Iterator<TBChangeBlockInfo> var7 = this.tempTileEntities.iterator();
			
			while(var7.hasNext())
			{
				TBChangeBlockInfo changeBlock = var7.next();
				BlockPos cbPos = changeBlock.getBlockPos();
				TileEntity var10 = getWorld().getTileEntity(cbPos);
				
				if(var10 instanceof TileEntityChangeBlock)
				{
					TileEntityChangeBlock var12 = (TileEntityChangeBlock) var10;
					
					for(int var5 = 0; var5<2; ++var5)
					{
						MutableItemStack var6 = changeBlock.getCurrent()[var5];
						
						if(var6!=null)
						{
							if(var6.getCount()>0)
							{
								var12.setInventorySlotContents(var5, var6.toItemStack());
							}
							else
							{
								var12.setReplenish(var5, var6);
							}
						}
						
						var12.setOverride(var5, changeBlock.getOverride(var5));
					}
					
					var12.getChangeBlock().setAlignment(changeBlock.getAlignment());
					var12.setTogglyfierCoords(new ChunkCoordinates(getPosX(), getPosY(), getPosZ()));
					var7.remove();
				}
				else
				{
					if(getWorld().getBlockState(cbPos)==Blocks.AIR.getDefaultState())
					{
						var7.remove();
					}
				}
			}
		}
	}
	
	private static boolean areItemStacksEqual(ItemStack var1, ItemStack var2)
	{
		return var1!=null && var2!=null ? var1.getItem()==var2.getItem() && var1.getItemDamage()==var2.getItemDamage() : var1==null && var2==null;
	}
	
	private Block blockFromID(int id) { return blocksInIDOrder[id]; }
	
	private void checkContainerRegistry()
	{
		if(this.containerRegistry.size()>1)
		{
			for(int var1 = 1; var1<this.containerRegistry.size(); ++var1)
			{
				ChunkCoordinates var2 = this.containerRegistry.get(var1);
				
				if(!ModConfig.isInContainerList(getWorld().getBlockState(var2.toBlockPos()).getBlock()))
				{
					this.updateContainerRegistry();
					break;
				}
			}
		}
		else
		{
			this.updateContainerRegistry();
		}
	}
	
	private boolean checkForTilledSoil(int var1, int var2, int var3)
	{
		Block block = getWorld().getBlockState(new BlockPos(var1, var2, var3)).getBlock();
		boolean var5 = false;
		
		if(block!=DIRT && block!=GRASS)
		{
			if(block==FARMLAND)
			{
				var5 = true;
			}
		}
		else
		{
			ItemStack var6 = null;
			
			if(!isCreative())
			{
				var6 = this.findHoeInContainer();
			}
			
			if(var6!=null || isCreative())
			{
				getWorld().setBlockState(new BlockPos(var1, var2, var3), FARMLAND.getDefaultState());
				var5 = true;
			}
		}
		
		return var5;
	}
	
	private void convertMetadataHacks()
	{
		for(final TBChangeBlockInfo changeBlockInfo : changeBlocks)
		{
			for(int var3 = 0; var3<2; ++var3)
			{
				final MutableItemStack var4 = changeBlockInfo.getCurrent()[var3];
				
				if(var4.isEmpty())
				{
					continue;
				}
				
				final Item item = var4.getItem();
				final int itemDamage = var4.getItemDamage();
				Block var9;
				
				if(item==Item.getItemFromBlock(DIRT))
				{
					var9 = this.removeExpectedBlockID(var4.toItemStack());
					
					if((itemDamage & 1)!=0)
					{
						var4.setDamage(0);
						var9 = GRASS;
					}
					
					changeBlockInfo.getExpectedBlock()[var3] = var9;
				}
				else if(item==Item.getItemFromBlock(FURNACE))
				{
					var9 = this.removeExpectedBlockID(var4.toItemStack());
					
					if((itemDamage & 8)!=0)
					{
						var4.setDamage(0);
						var9 = LIT_FURNACE;
					}
					
					changeBlockInfo.getExpectedBlock()[var3] = var9;
				}
				else if(item==Item.getItemFromBlock(REDSTONE_TORCH))
				{
					var9 = this.removeExpectedBlockID(var4.toItemStack());
					
					if((itemDamage & 8)!=0)
					{
						var4.setDamage(0);
						var9 = UNLIT_REDSTONE_TORCH;
					}
					
					changeBlockInfo.getExpectedBlock()[var3] = var9;
				}
				else if(item==REPEATER && (itemDamage & 16)!=0)
				{
					var9 = this.removeExpectedBlockID(var4.toItemStack());
					
					if((itemDamage & 16)==0 && var9==Blocks.AIR)
					{
						var9 = UNPOWERED_REPEATER;
					}
					else
					{
						var4.setDamage((itemDamage & 16)!=0 ? itemDamage & 15 : var4.getItemDamage());
						var9 = POWERED_REPEATER;
					}
					
					changeBlockInfo.getExpectedBlock()[var3] = var9;
				}
				else if(Block.getBlockFromItem(item)==JUKEBOX && itemDamage>0 && changeBlockInfo.getContainerNBT()[var3]==null)
				{
					final BlockPos pos = changeBlockInfo.getBlockPos();
					final NBTTagCompound var10 = new NBTTagCompound();
					var10.setString("id", "RecordPlayer");
					var10.setInteger("x", pos.getX());
					var10.setInteger("y", pos.getY());
					var10.setInteger("z", pos.getZ());
					var10.setInteger("Record", recordIDOffset + itemDamage - 1);
					var10.setString("Togglyfiers_BlockID", Objects.requireNonNull(JUKEBOX.getRegistryName()).toString());
					changeBlockInfo.getContainerNBT()[var3] = var10;
					var4.setDamage(1);
				}
				else
				{
					final Block var7 = ModConfig.getFirstExpectedBlock(item);
					final Block var8 = this.removeExpectedBlockID(var4.toItemStack());
					
					if(var7==Blocks.AIR)
					{
						continue;
					}
					changeBlockInfo.getExpectedBlock()[var3] = (var8!=Blocks.AIR ? var8 : var7);
				}
			}
		}
	}
	
	private void convertSavedMetadata()
	{
		for(final TBChangeBlockInfo changeBlockInfo : this.changeBlocks)
		{
			for(int var3 = 0; var3<2; ++var3)
			{
				final MutableItemStack var4 = changeBlockInfo.getCurrent()[var3];
				
				if(var4!=null)
				{
					final int var5 = ModConfig.getItemFlags(var4.toItemStack()) & 15;
					
					if(var5>0)
					{
						changeBlockInfo.getSavedMetadata()[var3] = (byte) (var4.getItemDamage() & var5);
						var4.setDamage(var4.getItemDamage() & ~var5);
					}
				}
			}
		}
	}
	
	private void decrStack(ItemStack var1)
	{
		if(!isCreative())
		{
			var1.setCount(var1.getCount() - 1);
		}
	}
	
	private void decrStack(MutableItemStack var1)
	{
		if(!isCreative())
		{
			var1.changeCount(-1);
		}
	}
	
	private void dropItemsFromNBT(NBTTagCompound var1)
	{
		if(var1==null)
		{
			return;
		}
		
		Item var2 = Item.getItemFromBlock(ModBlocks.togglyfier);
		@Nonnull Block var3 = Blocks.AIR;
		
		if(!(var2 instanceof ItemBlock))
		{
			if(ModConfig.hasExpectedBlock(var2))
			{
				var3 = ModConfig.getFirstExpectedBlock(var2);
				var2 = Item.getItemFromBlock(var3);
			}
			else
			{
				return;
			}
		}
		
		if(var2!=Items.AIR && var3 instanceof BlockContainer && var1.getBoolean("Togglyfiers_IInventory"))
		{
			BlockPos pos = new BlockPos(var1.getInteger("x"), var1.getInteger("y"), var1.getInteger("z"));
			getWorld().setBlockState(pos, var3.getStateFromMeta(var1.getByte("Togglyfiers_Metadata") & 255));
			final TileEntity var6 = getWorld().getTileEntity(pos);
			assert var6!=null;
			var6.readFromNBT(var1);
			final IInventory var7 = (IInventory) var6;
			
			final int sizeInventory = var7.getSizeInventory();
			for(int var8 = 0; var8<sizeInventory; ++var8)
			{
				Main.dropItemStack(getWorld(), var7.getStackInSlot(var8), getPos());
				var7.setInventorySlotContents(var8, ItemStack.EMPTY);
			}
			
			getWorld().setBlockToAir(pos);
		}
	}
	
	private int findChangeBlock(@Nonnull final Vec3i pos)
	{
		return findChangeBlock(pos.getX(), pos.getY(), pos.getZ());
	}
	
	private Block getBlockFromID(int id)
	{
		return blocksInIDOrder[id];
	}
	
	private BlockPos getPlacementLocation(@Nonnull TBChangeBlockInfo changeBlockInfo, int var2, boolean var3)
	{
		int[] data = getPlacementLocationWithFacing(changeBlockInfo, var2, var3);
		return new BlockPos(data[0], data[1], data[2]);
	}
	
	private int[] getPlacementLocationWithFacing(@Nonnull final TBChangeBlockInfo changeBlockInfo, int var2, boolean var3)
	{
		BlockPos pos = changeBlockInfo.getBlockPos();
		int[] var4 = new int[]
						 {
							 pos.getX(),
							 pos.getY(),
							 pos.getZ(),
							 changeBlockInfo.getAlignment().ordinal()
						 };
		
		int var5 = var3 ? 1 : -1;
		
		if((var2 & ModConfig.ItemFlags.DOWNWARD_ONLY.getBitValue())==0)
		{
			switch(changeBlockInfo.getAlignment().ordinal())
			{
			case 0:
				++var4[1];
				break;
			case 1:
				--var4[1];
				break;
			
			case 2:
				++var4[2];
				break;
			case 3:
				--var4[2];
				break;
			
			case 4:
				++var4[0];
				break;
			case 5:
				--var4[0];
			}
			
			Block block = getWorld().getBlockState(new BlockPos(var4[0], var4[1], var4[2])).getBlock();
			
			if(block==Blocks.AIR || block==SNOW_LAYER)
			{
				var4[1] = pos.getY() - var5;
				var4[3] = 1;
			}
		}
		else
		{
			var4[1] -= var5;
			var4[3] = 1;
		}
		
		return var4;
	}
	
	private void incrStack(MutableItemStack var1)
	{
		if(!isCreative())
		{
			var1.setCount(var1.getCount() + 1);
		}
	}
	
	private static boolean isItemSetToDrop(@Nonnull ItemStack var1)
	{
		return (ModConfig.getItemFlags(var1) & ModConfig.ItemFlags.DROP_AS_ITEM.getBitValue())!=0 || var1.getItem() instanceof ItemFood;
	}
	
	@Nonnull
	private ItemStack makeStackOfChangeBlocks()
	{
		int var2;
		
		if(isCreative())
		{
			var2 = 1;
		}
		else
		{
			var2 = this.getMaxChangeBlocks() - this.changeBlocks.size();
			
			if(var2<=0)
			{
				return ItemStack.EMPTY;
			}
			
			if(var2>this.getInventoryStackLimit())
			{
				var2 = this.getInventoryStackLimit();
			}
		}
		
		ItemStack var3 = new ItemStack(ModBlocks.changeBlock, var2);
		NBTTagCompound var4 = new NBTTagCompound();
		var4.setInteger("Dim", getWorld().provider.getDimension());
		var4.setInteger("X", getPosX());
		var4.setInteger("Y", getPosY());
		var4.setInteger("Z", getPosZ());
		var3.setTagCompound(var4);
		return var3;
	}
	
	@SuppressWarnings("SameParameterValue")	// Within the base mod, this is only used to make fire.
	private void placeBlockAtCB(TBChangeBlockInfo changeBlock, Block block)
	{
		placeBlockStateAtCB(changeBlock, block.getDefaultState());
	}
	
	private void placeBlockStateAtCB(TBChangeBlockInfo changeBlock, IBlockState block)
	{
		getWorld().setBlockState(changeBlock.getBlockPos(), block);
	}
	
	private void placeBlocks()
	{
		if(this.changeBlocks.isEmpty())
		{
			return;
		}
		
		int powerStateIndex = this.getIndexFromPower();
		for(Integer var3 : this.priorityList[powerStateIndex])
		{
			TBChangeBlockInfo changeBlock = this.changeBlocks.get(var3);
			MutableItemStack itemStackPush = changeBlock.getCurrent()[powerStateIndex];
			Item item = itemStackPush.getItem();
			
			if(changeBlock.getOverride(powerStateIndex) || itemStackPush.isEmpty() || isItemSetToDrop(itemStackPush.toItemStack()))
			{
				ItemStack var7 = null;
				
				if(itemStackPush.isEmpty())
				{
					if(changeBlock.getOverride(powerStateIndex))
					{
						var7 = this.findItemInContainer(Items.AIR, -1, 64);
					}
				}
				else
				{
					if(isCreative())
					{
						var7 = itemStackPush.toItemStack();
						var7.setCount(1);
					}
					else if(itemStackPush.getCount()==0)
					{
						var7 = this.findItemInContainer(item, itemStackPush.getItemDamage(), 64);
					}
					else
					{
						var7 = itemStackPush.toItemStack();
						itemStackPush.setCount(0);
					}
				}
				
				if(var7!=null)
				{
					Main.dropItemStack(getWorld(), var7, changeBlock.getBlockPos(), changeBlock.getAlignment());
				}
				continue;
			}
			
			if(itemStackPush.getCount()==0)
			{
				if(isCreative())
				{
					itemStackPush.setCount(1 + itemStackPush.getCount());
				}
				else if(itemStackPush.getItem()==FLINT_AND_STEEL && ModConfig.getBoolean(ModConfig.TogglyfierOptions.MOVE_FLINT_AND_STEEL))
				{
					// Fall through without doing anything
				}
				else if(this.findItemInContainer(item, itemStackPush.getItemDamage(), 1).isEmpty())
				{
					continue;
				}
				else
				{
					itemStackPush.changeCount(1);
				}
			}
			else if(item==MILK_BUCKET)
			{
				Boolean bucketFound = replaceItemInContainerWith(Items.BUCKET, 0, 1, MILK_BUCKET);
				
				if(bucketFound)
				{
					item = Items.BUCKET;
					itemStackPush.setItem(Items.BUCKET);
				}
			}
			
			int var42 = ModConfig.getItemFlags(itemStackPush.toItemStack());
			this.setupFakePlayer(changeBlock, var42);
			getFakePlayer().inventory.setPickedItemStack(itemStackPush.toItemStack());
			
			// There's a non-air expected block, and that block is not double, and does not need tilled soil
			if(ModConfig.hasExpectedBlock(itemStackPush)
				   && changeBlock.getExpectedBlock()[powerStateIndex]!=Blocks.AIR
				   && (var42 & ModConfig.ItemFlags.IS_DOUBLE_BLOCK.getBitValue())==0 &&
				   (var42 & ModConfig.ItemFlags.REQUIRES_TILLED_SOIL.getBitValue())==0)
			{
				item = Item.getItemFromBlock(changeBlock.getExpectedBlock()[powerStateIndex]);
				changeBlock.setExpectedBlock(Blocks.AIR, powerStateIndex);
			}
			
			boolean var49;
			
			int var8 = var42 & 15;
			if(item instanceof ItemBlock)
			{
				MutableItemStack var60 = new MutableItemStack(itemStackPush);
				
				if(item!=var60.getItem())
				{
					var60.setItem(item);
				}
				
				{
					BlockPos pos = this.getPlacementLocation(changeBlock, var42, true);
					ItemStack temp = var60.toItemStack();
					Main.setWorldManagerSoundStatus(getWorld(), false);
					var49 = ((ItemBlock) item).placeBlockAt(
						temp,
						getFakePlayer(),
						getWorld(),
						pos,
						changeBlock.getAlignment(),    // keyword
						0.0F,
						0.0F,
						0.0F,
						Block.getBlockFromItem(item).getDefaultState());
					Main.setWorldManagerSoundStatus(getWorld(), true);
					var60.set(temp);
				}
				
				if(var49 && !isCreative())
				{
					itemStackPush.setCount(var60.getCount());
				}
				
				if(var49)
				{
					if(var8>0)
					{
						IBlockState presentBlockState = getWorld().getBlockState(changeBlock.getBlockPos());
						Block presentBlock = presentBlockState.getBlock();
						
						getWorld().setBlockState(
							changeBlock.getBlockPos(),
							presentBlock.getStateFromMeta(presentBlock.getMetaFromState(presentBlockState) | changeBlock.getSavedMetadata()[powerStateIndex]));
						
						if((var42 & ModConfig.ItemFlags.IS_DOUBLE_BLOCK.getBitValue())!=0)
						{
							BlockPos var48 = this.getPlacementLocation(changeBlock, var42, false);
							IBlockState blockState = getWorld().getBlockState(var48);
							Block block = blockState.getBlock();
							getWorld().setBlockState(
								var48,
								block.getStateFromMeta(block.getMetaFromState(blockState) | changeBlock.getSavedMetadata()[powerStateIndex]));
						}
						
						changeBlock.getSavedMetadata()[powerStateIndex] = 0;
					}
					
					if(Block.getBlockFromItem(item) instanceof BlockContainer && changeBlock.getContainerNBT()[powerStateIndex]!=null)
					{
						TileEntity var52 = getWorld().getTileEntity(changeBlock.getBlockPos());
						
						if(var52!=null)
						{
							var52.readFromNBT(changeBlock.getContainerNBT()[powerStateIndex]);
							changeBlock.getContainerNBT()[powerStateIndex] = null;
						}
						else
						{
							this.tempTileEntities.add(changeBlock);
						}
					}
					
					if(item==Item.getItemFromBlock(JUKEBOX))
					{
						TileEntityJukebox var56 = (TileEntityJukebox) getWorld().getTileEntity(changeBlock.getBlockPos());
						
						if(!var56.getRecord().isEmpty())
						{
							//We set the block before, but we need to update the state (having a record is part of the blockstate)
							//We also want to tell the world to play the record, because that just makes sense.
							getWorld().setBlockState(changeBlock.getBlockPos(), JUKEBOX.getDefaultState().withProperty(HAS_RECORD, true));
							ItemRecord record = (ItemRecord) var56.getRecord().getItem();
							getWorld().playRecord(changeBlock.getBlockPos(), record.getSound());
						}
					}
				}
			}
			else if(item==WATER_BUCKET)
			{
				placeFluidFromBucket(changeBlock, FLOWING_WATER, itemStackPush);
			}
			else if(item==LAVA_BUCKET)
			{
				placeFluidFromBucket(changeBlock, FLOWING_LAVA, itemStackPush);
			}
			else if(item==FLINT_AND_STEEL)
			{
				var49 = true;
				
				if(itemStackPush.getCount()==0 && ModConfig.getBoolean(ModConfig.TogglyfierOptions.MOVE_FLINT_AND_STEEL))
				{
					var49 = this.findItemInContainer(item, 0, -1)!=null;
				}
				
				else if(!isCreative())
				{
					ItemStack temp = itemStackPush.toItemStack();
					temp.damageItem(1, fakePlayer);
					itemStackPush.set(temp);
				}
				
				if(var49)
				{
					placeBlockAtCB(changeBlock, Blocks.FIRE);
				}
			}
			else if(item==BOAT)
			{
				getWorld().spawnEntity(new EntityBoat(
					getWorld(),
					changeBlock.getBlockPos().getX() + HALF,
					changeBlock.getBlockPos().getY() + HALF,
					changeBlock.getBlockPos().getZ() + HALF));
				this.decrStack(itemStackPush);
			}
/*					else if(item==SPAWN_EGG)
			{
				ItemMonsterPlacer.spawnCreature(
					getWorld(),
					itemStackPush.getItemDamage(),
					(double) changeBlock.getBlockPos().getX() + HALF,
					(double) changeBlock.getBlockPos().getY() + HALF,
					(double) changeBlock.getBlockPos().getZ() + HALF);
			}
			else if(Arrays.asList(projectiles).contains(itemID) || (itemID==Items.potion && ItemPotion.isSplash(itemStackPush.getItemDamage())))
			{
				spawnProjectile(changeBlock, itemStackPush, itemID);
				this.decrStack(itemStackPush);
			}*/
			else if(item==Items.SIGN && STANDING_SIGN.canPlaceBlockAt(getWorld(), changeBlock.getBlockPos()))
			{
				EnumFacing var44 = changeBlock.getAlignment();
				
				// Determine orientation of the sign: is it a wall sign or a post sign?
				if(var44==DOWN || var44==UP)    // Changeblock is oriented up or down
				{
					getWorld().setBlockState(changeBlock.getBlockPos(), STANDING_SIGN.getDefaultState());
				}
				else
				{
					int[] var48;
					int mountBlockX = changeBlock.getBlockPos().getX();
					int mountBlockZ = changeBlock.getBlockPos().getZ();
					
					// Find the block the wall sign would be mounted on
					switch(var44.ordinal())
					{
					case 2:
						++mountBlockZ;
						break;
					case 3:
						--mountBlockZ;
						break;
					case 4:
						++mountBlockX;
						break;
					case 5:
						--mountBlockX;
						break;
					}
					
					// Determine if the wall is solid. If it's not, put the sign on a post.
					Block var53 = getWorld().getBlockState(new BlockPos(mountBlockX, changeBlock.getBlockPos().getY(), mountBlockZ))
									  .getMaterial().isSolid() ? WALL_SIGN : STANDING_SIGN;
					
					if(var53==STANDING_SIGN)
					{
						var48 = new int[] {8, 0, 4, 12};
						getWorld().setBlockState(changeBlock.getBlockPos(), var53.getDefaultState().withProperty(BlockStandingSign.ROTATION, var48[var44.ordinal() - 2]));
					}
					else
					{
						getWorld().setBlockState(changeBlock.getBlockPos(), var53.getDefaultState().withProperty(BlockWallSign.FACING, var44.getOpposite()));
					}
				}
				
				this.decrStack(itemStackPush);
				
				// Copy the text from the sign, if we can.
				if(changeBlock.getContainerNBT()[powerStateIndex]!=null)
				{
					TileEntitySign var54 = (TileEntitySign) getWorld().getTileEntity(changeBlock.getBlockPos());
					
					if(var54!=null)
					{
						var54.readFromNBT(changeBlock.getContainerNBT()[powerStateIndex]);
						changeBlock.getContainerNBT()[powerStateIndex] = null;
					}
					else
					{
						this.tempTileEntities.add(changeBlock);
					}
				}
			}
			else if(item instanceof ItemRecord)
			{
				BlockPos var45 = this.getPlacementLocation(changeBlock, 0, true);
				
				IBlockState jukeboxWithoutDisk = JUKEBOX.getDefaultState().withProperty(HAS_RECORD, true);
				// If it's an empty jukebox, then we better put this disk in it!
				if(getWorld().getBlockState(var45).equals(jukeboxWithoutDisk))
				{
					// Calling onBlockActivated serves to remove any disks that might already be in the jukebox.
					JUKEBOX.onBlockActivated(getWorld(), var45, jukeboxWithoutDisk, fakePlayer, EnumHand.OFF_HAND, EnumFacing.UP, 0.0f, 0.0f, 0.0f);
					getWorld().playRecord(var45, ((ItemRecord) item).getSound());
					this.decrStack(itemStackPush);
				}
			}
			else if(TOGGLY_BEHAVIOR_REGISTRY.containsKey(item))
			{
				IBehaviorDispenseItem dispenseBehavior = TOGGLY_BEHAVIOR_REGISTRY.getObject(item);
				ItemStack temp = itemStackPush.toItemStack();
				dispenseBehavior.dispense(new BlockSourceImpl(getWorld(), changeBlock.getBlockPos()), temp);
				itemStackPush.set(temp);
			}
			else if(BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.containsKey(item))
			{
				IBehaviorDispenseItem dispenseBehavior = BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(item);
				ItemStack temp = itemStackPush.toItemStack();
				dispenseBehavior.dispense(new BlockSourceImpl(getWorld(), changeBlock.getBlockPos()), temp);
				itemStackPush.set(temp);
			}
			else
			{
				int var43;
				int var46;
				
				if((var42 & ModConfig.ItemFlags.REQUIRES_TILLED_SOIL.getBitValue())!=0)
				{
					if(!this.checkForTilledSoil(changeBlock.getBlockPos().getX(), changeBlock.getBlockPos().getY() - 1, changeBlock.getBlockPos().getZ()))
					{
						continue;
					}
					
					if((var42 & ModConfig.ItemFlags.MELON_AND_PUMPKIN_GROWTH.getBitValue())!=0)
					{
						var43 = changeBlock.getBlockPos().getX();
						var46 = changeBlock.getBlockPos().getZ();
						
						switch(changeBlock.getAlignment().ordinal())
						{
						case 0:
						case 1:
						case 2:
							++var46;
							break;
						
						case 3:
							--var46;
							break;
						
						case 4:
							++var43;
							break;
						
						case 5:
							--var43;
						}
						
						this.checkForTilledSoil(var43, changeBlock.getBlockPos().getY() - 1, var46);
					}
				}
				
				int[] var45 = this.getPlacementLocationWithFacing(changeBlock, var42, true);
				
				Main.setWorldManagerSoundStatus(getWorld(), false);
				var43 = itemStackPush.getCount();
				var46 = itemStackPush.getItemDamage();
				getFakePlayer().setCurrentEquippedItem(itemStackPush.toItemStack());
				EnumActionResult var47 = item.onItemUse(
					getFakePlayer(),
					getWorld(),
					new BlockPos(var45[0], var45[1], var45[2]),
					EnumHand.MAIN_HAND,
					EnumFacing.values()[var45[3]],
					0.0F,
					0.0F,
					0.0F);
				Main.setWorldManagerSoundStatus(getWorld(), true);
				
				if(var47==EnumActionResult.SUCCESS)
				{
					Block var53 = getWorld().getBlockState(changeBlock.getBlockPos()).getBlock();
					ModConfig.addExpectedBlock(itemStackPush.toItemStack(), var53);
					
					if(var8>0)
					{
						Block currentBlock = getWorld().getBlockState(changeBlock.getBlockPos()).getBlock();
						getWorld().setBlockState(
							changeBlock.getBlockPos(),
							currentBlock.getStateFromMeta(
								currentBlock.getMetaFromState(getWorld().getBlockState(changeBlock.getBlockPos()))
								| changeBlock.getSavedMetadata()[powerStateIndex]));
						
						if((var42 & ModConfig.ItemFlags.IS_DOUBLE_BLOCK.getBitValue())!=0)
						{
							Block block = getWorld().getBlockState(changeBlock.getBlockPos()).getBlock();
							BlockPos placementLocation = this.getPlacementLocation(changeBlock, var42, false);
							getWorld().setBlockState(
								placementLocation,
								block.getStateFromMeta(
									currentBlock.getMetaFromState(getWorld().getBlockState(placementLocation))
									| changeBlock.getSavedMetadata()[powerStateIndex]));
						}
						
						changeBlock.getSavedMetadata()[powerStateIndex] = 0;
					}
					
					if(var53 instanceof BlockContainer && changeBlock.getContainerNBT()[powerStateIndex]!=null)
					{
						TileEntity var59 = getWorld().getTileEntity(changeBlock.getBlockPos());
						
						if(var59!=null)
						{
							var59.readFromNBT(changeBlock.getContainerNBT()[powerStateIndex]);
							changeBlock.getContainerNBT()[powerStateIndex] = null;
						}
						else
						{
							this.tempTileEntities.add(changeBlock);
						}
					}
					
					this.resetStack(itemStackPush, var43, var46);
				}
				else
				{
					assert item!=Items.AIR;
					TogglyfierPlayer fakePlayer = getFakePlayer();
					fakePlayer.setCurrentEquippedItem(itemStackPush.toItemStack());
					ActionResult<ItemStack> ar = item.onItemRightClick(getWorld(), getFakePlayer(), EnumHand.MAIN_HAND);
					EnumActionResult result = ar.getType();
					ItemStack stackAfter = ar.getResult();
					ItemStack var13 = fakePlayer.getCurrentEquippedItem();
					
					assert ItemStack.areItemStacksEqual(stackAfter, var13);
					assert (result != EnumActionResult.SUCCESS) || ItemStack.areItemStacksEqual(stackAfter, itemStackPush.toItemStack());
					
					if(var13==itemStackPush.toItemStack() && var43==itemStackPush.getCount() && var46==itemStackPush.getItemDamage())
					{
						BlockPos offset = BlockPos.ORIGIN.offset(changeBlock.getAlignment());
						
						int xOffset = 0;
						int yOffset = 0;
						int zOffset = 0;
						int reachDistance = (int) Minecraft.getMinecraft().playerController.getBlockReachDistance();
						
						for(int var21 = 1; var21<=reachDistance; ++var21)
						{
							xOffset += offset.getX();
							yOffset += offset.getY();
							zOffset += offset.getZ();
							
							BlockPos mountPoint = changeBlock.getBlockPos().add(xOffset, yOffset, zOffset);
							
							if(!getWorld().getBlockState(mountPoint).getMaterial().isSolid())
							{
								xOffset -= offset.getX();
								yOffset -= offset.getY();
								zOffset -= offset.getZ();
								break;
							}
						}
						
						Vec3i pos = changeBlock.getBlockPos();
						
						AxisAlignedBB var27 =
							new AxisAlignedBB(
								pos.getX(),	pos.getY(),	pos.getZ(),
								pos.getX(),	pos.getY(),	pos.getZ()
							).offset(
								xOffset,	yOffset,	zOffset
							).expand(HALF, HALF, HALF);
						
						BlockPos var28 = new BlockPos(pos);
						Entity var29 = null;
						List<Entity> var30 = getWorld().getEntitiesWithinAABBExcludingEntity(null, var27);
						double var31 = Double.MAX_VALUE;
						
						// Get the closest entity to the changeblock.
						for(Entity entity : var30)
						{
							if(entity.canBeCollidedWith())
							{
								double var35 = Math.sqrt(entity.getDistanceSq(var28));
								
								if(var35<var31)
								{
									var31 = var35;
									var29 = entity;
								}
							}
						}
						
						if(var29!=null)
						{
							var43 = itemStackPush.getCount();
							var46 = itemStackPush.getItemDamage();
							
							getFakePlayer().setCurrentEquippedItem(itemStackPush.toItemStack());
							if(var29.applyPlayerInteraction(getFakePlayer(), new Vec3d(0,0,0), EnumHand.MAIN_HAND)==EnumActionResult.SUCCESS)
							{
								ItemStack var69 = getFakePlayer().getCurrentEquippedItem();
								
								if(var69!=null)
								{
									if(itemStackPush.toItemStack()==var69)
									{
										this.resetStack(itemStackPush, var43, var46);
									}
									else
									{
										changeBlock.getCurrent()[powerStateIndex].set(getFakePlayer().getCurrentEquippedItem());
									}
								}
								
								continue;
							}
							
							if(var29 instanceof EntityLiving)
							{
								getFakePlayer().setCurrentEquippedItem(itemStackPush.toItemStack());
								var29.applyPlayerInteraction(getFakePlayer(), new Vec3d(0,0,0), EnumHand.MAIN_HAND);
								
								if(itemStackPush.getCount()!=var43 || itemStackPush.getItemDamage()!=var46)
								{
									this.resetStack(itemStackPush, var43, var46);
									continue;
								}
							}
						}
						
						byte var68 = 0;
						byte var71 = 0;
						EnumFacing var70 = changeBlock.getAlignment().ordinal()>1 ? changeBlock.getAlignment() : NORTH;
						
						switch(var70)
						{
						case NORTH:
							var71 = 1;
							break;
						
						case SOUTH:
							var71 = -1;
							break;
						
						case WEST:
							var68 = 1;
							break;
						
						case EAST:
							var68 = -1;
						}
						
						double var36 = changeBlock.getBlockPos().getX() + var68*HALF + HALF;
						double var38 = changeBlock.getBlockPos().getY() + HALF;
						double var40 = changeBlock.getBlockPos().getZ() + var71*HALF + HALF;
						//ModLoader.dispenseEntity(getWorld(), itemStackPush, getWorld().rand, getPos(), var68, var71, var36, var38, var40);
						this.resetStack(itemStackPush, var43, var46);
					}
					else if(!ItemStack.areItemStacksEqual(itemStackPush.toItemStack(), var13))
					{
						changeBlock.getCurrent()[powerStateIndex].set(var13);
					}
					else
					{
						this.resetStack(itemStackPush, var43, var46);
					}
				}
			}
		}
	}
	
	private Boolean replaceItemInContainerWith(final Item bucket, final int i, final int i1, final Item milkBucket)
	{
		// TODO: Write this function
		return false;
	}
	
	private void placeFluidFromBucket(TBChangeBlockInfo changeBlock, Block fluid, MutableItemStack bucket)
	{
		getWorld().setBlockState(changeBlock.getBlockPos(), fluid.getDefaultState(), 3);
		bucket.setItem(BUCKET);
	}
	
	private void readFromChangeBlocks()
	{
		int size = this.changeBlocks.size();
		for(int var1 = 0; var1<size; ++var1)
		{
			TBChangeBlockInfo changeBlockInfo1 = this.changeBlocks.get(var1);
			TileEntityChangeBlock var3 = (TileEntityChangeBlock) getWorld().getTileEntity(changeBlockInfo1.getBlockPos());
			if (var3==null) { setMode(LOCKDOWN); break; }
			TBChangeBlockInfo changeBlockInfo2 = new TBChangeBlockInfo(var3.getChangeBlock());
			
			for(int var5 = 0; var5<changeBlockInfo1.getCurrent().length; ++var5)
			{
				MutableItemStack cb1Stack = changeBlockInfo1.getCurrent()[var5];
				MutableItemStack cb2Stack = changeBlockInfo2.getCurrent()[var5];
				
				if(!cb1Stack.isEmpty())
				{
					if(TileEntityTogglyfier.areItemStacksEqual(cb1Stack.toItemStack(), cb2Stack.toItemStack()))
					{
						changeBlockInfo2.getSavedMetadata()[var5] = changeBlockInfo1.getSavedMetadata()[var5];
					}
					
					if(cb1Stack.getCount()==0 && cb2Stack.isEmpty())
					{
						changeBlockInfo2.getCurrent()[var5].set(cb1Stack);
					}
					
					if(!cb2Stack.isEmpty() && cb1Stack.getItem()==cb2Stack.getItem())
					{
						changeBlockInfo2.getExpectedBlock()[var5] = changeBlockInfo1.getExpectedBlock()[var5];
					}
					
					changeBlockInfo2.getContainerNBT()[var5] = changeBlockInfo1.getContainerNBT()[var5];
				}
			}
			
			this.changeBlocks.set(var1, changeBlockInfo2);
		}
	}
	
	private Block removeExpectedBlockID(ItemStack var1)
	{
		int var2 = var1.getItemDamage();
		Block var3 = blocksInIDOrder[var2 >> 8 & 255];
		var2 &= -65281;
		var1.setItemDamage(var2);
		return var3;
	}
	
	private void resetStack(ItemStack var1, int count, int damage)
	{
		if(isCreative())
		{
			var1.setCount(count);
			var1.setItemDamage(damage);
		}
	}
	
	private void resetStack(MutableItemStack var1, int count, int damage)
	{
		if(isCreative())
		{
			var1.setCount(count);
			var1.setDamage(damage);
		}
	}
	
	private void setupFakePlayer(TBChangeBlockInfo changeBlockInfo, int var2)
	{
		BlockPos blockPos = changeBlockInfo.getBlockPos();
		fakePlayer.prevPosX = fakePlayer.posX = blockPos.getX() + HALF;
		fakePlayer.prevPosY = fakePlayer.posY = blockPos.getY() + HALF;
		fakePlayer.prevPosZ = fakePlayer.posZ = blockPos.getZ() + HALF;
		
		if((var2 & ModConfig.ItemFlags.USES_UP_AND_DOWN.getBitValue())!=0)
		{
			float posOffset = (float) (changeBlockInfo.getAlignment()== DOWN ? -1.0f : BLOCK_HEIGHT_TO_PLAYER_HEIGHT_OFFSET);
			fakePlayer.posY = blockPos.getY() + posOffset;
			fakePlayer.prevPosY = fakePlayer.posY;
		}
		
		float fakePlayerYaw = 0.0F;
		float fakePlayerPitch = 0.0F;
		switch(changeBlockInfo.getAlignment())
		{
		case DOWN:		fakePlayerPitch = PLAYER_PITCH_DOWN;	break;
		case UP:		fakePlayerPitch = PLAYER_PITCH_UP;		break;
		case NORTH:		fakePlayerYaw = PLAYER_YAW_NORTH;		break;
		case SOUTH:		fakePlayerYaw = PLAYER_YAW_SOUTH;		break;
		case WEST:		fakePlayerYaw = PLAYER_YAW_WEST;		break;
		case EAST:		fakePlayerYaw = PLAYER_YAW_EAST;		break;
		}
		
		getFakePlayer().prevRotationPitch = getFakePlayer().rotationPitch = fakePlayerPitch;
		getFakePlayer().prevRotationYaw = getFakePlayer().rotationYaw = fakePlayerYaw;
	}
	
	private void sortChangeBlocks()
	{
		if(this.changeBlocks.isEmpty())
		{
			this.priorityList = PRIORITY_LIST;
		}
		else
		{
			int size = this.changeBlocks.size();
			Integer[][] var1 = new Integer[2][size];
			
			for(int var2 = 0; var2<size; ++var2)
			{
				var1[0][var2] = var1[1][var2] = var2;
			}
			
			ComparatorChangeBlock var3 = new ComparatorChangeBlock(this, 0);
			Arrays.sort(var1[0], var3);
			var3.setPowerIndex();
			Arrays.sort(var1[1], var3);
			this.priorityList = var1;
		}
	}
	
	@Nonnull
	private IInventory getReplenisher()
	{
		if(replenisher == null)
		{
			replenisher = new InventoryReplenish(this);
		}
		return replenisher;
	}
	
	@Nonnull
	private TogglyfierPlayer getFakePlayer()
	{
		if(getWorld() != null && (fakePlayer == null || fakePlayer.getEntityWorld() == null))
		{
			fakePlayer = new TogglyfierPlayer(this, getWorld());
		}
		return fakePlayer;
	}
	
	private void spawnProjectile(Vec3i changeBlock, ItemStack itemStack, ItemStack stack, EnumFacing facing)
	{
		float headingX = 0.0f;
		Float headingY = null;
		float headingZ = 0.0f;
		
		switch(facing)
		{
		case UP:    //up
			headingY = 1.0f;
			break;
		
		case DOWN:    //down
			headingY = -1.0f;
			break;
		
		case SOUTH:    //positive z
			headingZ = 1.0f;
			break;
		
		case NORTH:    //negative z
			headingZ = -1.0f;
			break;
		
		case EAST:    //positive x
			headingX = 1.0f;
			break;
		
		case WEST:    //negative x
			headingX = -1.0f;
		}
		
		double posX = changeBlock.getX() + headingX*HALF + HALF;
		double posY = changeBlock.getY() + (headingY==null ? 0.0D : (double) headingY*HALF) + HALF;
		double posZ = changeBlock.getZ() + headingZ*HALF + HALF;
		
		if(headingY == null) { headingY = 0.1F; }
		
		Item item = stack.getItem();
		
		if(item==ARROW)
		{
			EntityArrow var18 = new TogglyfierEntityArrow(getWorld(), posX, posY, posZ);
			
			var18.shoot(headingX, (double) headingY, headingZ, PROJECTILE_VELOCITY_SLOW, PROJECTILE_INACCURACY_HIGH);
			var18.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
			getWorld().spawnEntity(var18);
		}
		else if(item==EGG)
		{
			EntityEgg var64 = new EntityEgg(getWorld(), posX, posY, posZ);
			var64.shoot(headingX, (double) headingY, headingZ, PROJECTILE_VELOCITY_SLOW, PROJECTILE_INACCURACY_HIGH);
			getWorld().spawnEntity(var64);
		}
		else if(item==SNOWBALL)
		{
			EntitySnowball var63 = new EntitySnowball(getWorld(), posX, posY, posZ);
			var63.shoot(headingX, (double) headingY, headingZ, PROJECTILE_VELOCITY_SLOW, PROJECTILE_INACCURACY_HIGH);
			getWorld().spawnEntity(var63);
		}
		else if(item==POTIONITEM)
		{
			EntityPotion var62 = new EntityPotion(getWorld(), posX, posY, posZ, itemStack);
			var62.shoot(headingX, (double) headingY, headingZ, PROJECTILE_VELOCITY_HIGH, PROJECTILE_INACCURACY_LOW);
			getWorld().spawnEntity(var62);
		}
		else if(item==EXPERIENCE_BOTTLE)
		{
			EntityExpBottle var61 = new EntityExpBottle(getWorld(), posX, posY, posZ);
			var61.shoot(headingX, (double) headingY, headingZ, PROJECTILE_VELOCITY_HIGH, PROJECTILE_INACCURACY_LOW);
			getWorld().spawnEntity(var61);
		}
		else if(item==FIRE_CHARGE)
		{
			Entity var66 = new EntitySmallFireball(getWorld(),
				posX, posY, posZ,
				getWorld().rand.nextGaussian()*0.05D + headingX,
				getWorld().rand.nextGaussian()*0.05D + (Math.abs(headingY)>0.2F ? 0.0F : headingY),
				getWorld().rand.nextGaussian()*0.05D + headingZ);
			getWorld().spawnEntity(var66);
		}
	}
	
	private void togglePull()         { throw new NotImplementedException("togglePull is NYI!"); }
	
	private void togglePush()         { throw new NotImplementedException("togglePush is NYI!"); }
	
	static class InventoryReplenish implements IInventory
	{
		@Nonnull final TileEntityTogglyfier togglyfier;
		
		InventoryReplenish(@Nonnull TileEntityTogglyfier var1)
		{
			togglyfier = var1;
		}
		
		/**
		 * Returns the name of the inventory.
		 */
		@NotNull
		@Override
		public String getName()
		{
			return "Replenishment";
		}
		
		@Override
		public boolean hasCustomName()
		{
			return false;
		}
		
		@NotNull
		@Override
		public ITextComponent getDisplayName()
		{
			return new TextComponentString(getName());
		}
		
		/**
		 * Returns the number of slots in the inventory.
		 */
		@Override
		public int getSizeInventory()
		{
			return 9;
		}
		
		@Override
		public boolean isEmpty()
		{
			boolean flag = true;
			int togglyfierSizeInternalInventory = TileEntityTogglyfier.getSizeInternalInventory();
			for(int i = 3; i<togglyfierSizeInternalInventory; i++)
			{
				flag &= (togglyfier.tbSlots[i]!=null && !togglyfier.tbSlots[i].toItemStack().isEmpty() && togglyfier.tbSlots[i].getCount()>0);
			}
			return flag;
		}
		
		/**
		 * Returns the stack in slot i
		 */
		@NotNull
		@Override
		public ItemStack getStackInSlot(int var1)
		{
			return togglyfier.tbSlots[var1 + 3].toItemStack();
		}
		
		/**
		 * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
		 * new stack.
		 */
		@NotNull
		@Override
		public ItemStack decrStackSize(int var1, int var2)
		{
			var1 += 3;
			
			if(togglyfier.tbSlots[var1]==null)
			{
				return ItemStack.EMPTY;
			}
			else
			{
				if(var2>togglyfier.tbSlots[var1].getCount())
				{
					var2 = togglyfier.tbSlots[var1].getCount();
				}
				
				ItemStack var3 = togglyfier.tbSlots[var1].splitStack(var2);
				
				if(togglyfier.tbSlots[var1].getCount()==0)
				{
					togglyfier.tbSlots[var1] = null;
				}
				
				this.onInventoryChanged();
				return var3;
			}
		}
		
		@NotNull
		@Override
		public ItemStack removeStackFromSlot(int index)
		{
			return togglyfier.removeStackFromSlot(index + 3);
		}
		
		/**
		 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
		 */
		@Override
		public void setInventorySlotContents(int var1, ItemStack var2)
		{
			togglyfier.tbSlots[var1 + 3].set(var2);
			
			if(var2.getCount()>getInventoryStackLimit())
			{
				var2.setCount(getInventoryStackLimit());
			}
			
			this.onInventoryChanged();
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
		
		@Override
		public void markDirty()
		{
			togglyfier.markDirty();
		}
		
		/**
		 * Do not make give this method the name canInteractWith because it clashes with Container
		 */
		@Override
		public boolean isUsableByPlayer(@NotNull EntityPlayer player)
		{
			return true;
		}
		
		@Override
		public void openInventory(@NotNull EntityPlayer player)  {}
		
		@Override
		public void closeInventory(@NotNull EntityPlayer player) {}
		
		@Override
		public boolean isItemValidForSlot(int index, @NotNull ItemStack stack)
		{
			return index<getSizeInventory();
		}
		
		@Override
		public int getField(int id)
		{
			return togglyfier.getField(id);
		}
		
		@Override
		public void setField(int id, int value)
		{
			togglyfier.setField(id, value);
		}
		
		@Override
		public int getFieldCount()
		{
			return togglyfier.getFieldCount();
		}
		
		@Override
		public void clear()
		{
			int sizeInventory = getSizeInventory();
			for(int i = 0; i<sizeInventory; i++)
			{
				togglyfier.removeStackFromSlot(i + 3);
			}
		}
		
		void onInventoryChanged()
		{
			this.togglyfier.onInventoryChanged();
		}
	}
	
	static class ComparatorChangeBlock implements Comparator<Object>
	{
		final TileEntityTogglyfier togglyfier;
		private int powerIndex;
		
		ComparatorChangeBlock(TileEntityTogglyfier var1, int var2)
		{
			togglyfier = var1;
			powerIndex = var2;
		}
		
		private static List<TBChangeBlockInfo> getCBs(TileEntityTogglyfier var0)
		{
			return var0.changeBlocks;
		}
		
		@Override
		public int compare(Object var1, Object var2)
		{
			if(var1 instanceof Integer && var2 instanceof Integer)
			{
				return this.compare((Integer) var1, (Integer) var2);
			}
			else if(var1 instanceof TBChangeBlockInfo && var2 instanceof TBChangeBlockInfo)
			{
				return compare((TBChangeBlockInfo) var1, (TBChangeBlockInfo) var2);
			}
			return 0;
		}
		
		int compare(Integer var1, Integer var2)
		{
			TBChangeBlockInfo var3 = getCBs(togglyfier).get(var1);
			TBChangeBlockInfo var4 = getCBs(togglyfier).get(var2);
			return compare(var3, var4);
		}
		
		int compare(TBChangeBlockInfo cb1, TBChangeBlockInfo cb2)
		{
			ItemStack var5 = cb1.getCurrent()[powerIndex].toItemStack();
			ItemStack var6 = cb2.getCurrent()[powerIndex].toItemStack();
			int var7 = var5!=null ? ModConfig.getPriority(var5) : 0;
			int var8 = var6!=null ? ModConfig.getPriority(var6) : 0;
			return var7!=var8 ? var7 - var8 : cb1.getBlockPos().getY() - cb2.getBlockPos().getY();
		}
		
		void setPowerIndex()
		{
			powerIndex = 1;
		}
	}
	
	private static class TogglyfierEntityArrow extends EntityArrow
	{
		TogglyfierEntityArrow(final World world, final double posX, final double posY, final double posZ)
		{
			super(world, posX, posY, posZ);
		}
		
		@NotNull
		@Override
		protected ItemStack getArrowStack()
		{
			return new ItemStack(ARROW, 1);
		}
	}
	
	private static class DefaultTogglyfyItem extends BehaviorDefaultDispenseItem
	{
		@Override
		protected void playDispenseSound(final IBlockSource source)
		{
			//Do nothing
		}
		
		@Override
		protected void spawnDispenseParticles(final IBlockSource source, final EnumFacing facingIn)
		{
			//Do nothing
		}
	}
	
	private static class TogglyfierPlayer extends FakePlayer
	{
		private ItemStack equippedStack = ItemStack.EMPTY;
		
		TogglyfierPlayer(final TileEntityTogglyfier tileEntityTogglyfier, World world)
		{
			super(world == null
					  ? null
					  : world
							.getMinecraftServer()
							.getWorld(
								world
									.provider
									.getDimension()),
				new GameProfile(UUID.randomUUID(),
				"Togglyfiers Fake Player"));
		}
		
		public void setCurrentEquippedItem(ItemStack stack)
		{
			equippedStack = stack;
		}
		
		ItemStack getCurrentEquippedItem()
		{
			return equippedStack;
		}
	}
	
	public static class ContainerTogglyfier extends Container
	{
		@Nonnull private final TileEntityTogglyfier tileEntity;
		
		public ContainerTogglyfier(@Nonnull InventoryPlayer player, @Nonnull TileEntityTogglyfier tileEntity, boolean readyMode)
		{
			this.tileEntity = tileEntity;
			
			this.addSlotToContainer(new Slot(tileEntity, 0, 43, 34));
			
			for(int y = 0; y<3; y++)
			{
				for(int x = 0; x<9; x++)
				{
					this.addSlotToContainer(new Slot(player, x + y*9 + 9, x*18 + 8, 84 + y*18));
				}
			}
			
			for(int x = 0; x<9; x++)
			{
				this.addSlotToContainer(new Slot(player, x, 8 + x*18, 142));
			}
		}
		
		public ContainerTogglyfier(@Nonnull InventoryPlayer player, @Nonnull TileEntityTogglyfier tileEntity)
		{
			this(player, tileEntity, false);
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn)
		{
			return tileEntity.isUsableByPlayer(playerIn);
		}
		
		@Nonnull public TileEntityTogglyfier getTogglyfier()
		{
			return tileEntity;
		}
	}
}
