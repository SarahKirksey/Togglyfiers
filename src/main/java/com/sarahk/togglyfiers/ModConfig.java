package com.sarahk.togglyfiers;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.util.MutableItemStack;
import com.sarahk.togglyfiers.util.TogglyfierItemInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public final class ModConfig
{
	private static final String[][] defaultExpectedBlocks = new String[][] {
		{"3", "2"},
		{"61", "62"},
		{"76", "75"},
		{"295", "59"},
		{"324", "64"},
		{"330", "71"},
		{"331", "55"},
		{"338", "83"},
		{"351", "0"},
		{"354", "92"},
		{"355", "26"},
		{"356", "93, 94"},
		{"361", "104"},
		{"362", "105"}
	};
	private static final String[][] defaultItemFlags = new String[][] {
		{"6", "save4"},
		{"29", "updown"},
		{"33", "updown"},
		{"69", "save4"},
		{"81", "save"},
		{"84", "save1"},
		{"96", "save3"},
		{"107", "save23"},
		{"295", "save123, seeds"},
		{"324", "save3, down, double"},
		{"328", "down"},
		{"330", "save3, down, double"},
		{"338", "save"},
		{"342", "down"},
		{"343", "down"},
		{"346", "drop"},
		{"354", "save123"},
		{"355", "double"},
		{"356", "save34"},
		{"361", "save123, seeds, pumpkin"},
		{"362", "save123, seeds, pumpkin"}
	};
	private static final String[][] defaultPriorities =
		new String[][] {{"-1", "54"}, {"1", "50, 65, 69, 77, 96, 107"}, {"2", "323, 324, 330, 355"}, {"3", "76, 259, 326, 327, 351, 2256, 2257"}};
	private static Properties configValues;
	private static Properties configExpectedBlocks;
	private static Properties configItemFlags;
	private static Properties configPriorities;
	private static boolean configUnread = true;
	private static boolean isModified = false;
	private static final File configDir = new File(Minecraft.getMinecraft().mcDataDir, "mods" + File.separator + "ApatheticMods");
	private static final File configFile = new File(configDir, "ToggleBlocks.properties");
	private static final String configHeader = "---Toggle Blocks---";
	private static final File configBlockItemDir = new File(configDir, "ToggleBlocks");
	private static final File configExpectedBlocksFile = new File(configBlockItemDir, "ExpectedBlocks.properties");
	private static final File configItemFlagsFile = new File(configBlockItemDir, "ItemFlags.properties");
	private static final File configPrioritiesFile = new File(configBlockItemDir, "Priorities.properties");
	private static final Map<TogglyfierItemInfo, Block[]> expectedBlocksMap = new HashMap<>();
	private static final Map<TogglyfierItemInfo, Integer> itemFlagsMap = new HashMap<>();
	private static final Map<Object, Object> prioritiesMap = new HashMap<>();
	private static final Collection<Block> containerList = new ArrayList<>();
	private static final File oldConfigFile = new File(Minecraft.getMinecraft().mcDataDir, "ToggleBlocks.txt");
	
	public static void addExpectedBlock(ItemStack var0, Block var1)
	{
		if(var1!=Blocks.AIR && var0!=null && var0.getItem()!=Items.AIR)
		{
			TogglyfierItemInfo var2 = TogglyfierItemInfo.getFromPool(var0);
			
			if(findExpectedBlock(var2, var1)<0)
			{
				TogglyfierItemInfo var3 = getItemInfoForFoundKey(expectedBlocksMap, var2);
				
				if(var3!=null)
				{
					Block[] var4 = (Block[]) expectedBlocksMap.get(var3);
					
					if(var4[0]==Blocks.AIR)
					{
						return;
					}
					
					Block[] var5 = Arrays.copyOf(var4, var4.length + 1);
					var5[var4.length] = var1;
					expectedBlocksMap.put(var3.copy(), var5);
				}
				else
				{
					expectedBlocksMap.put(var2.copy(), new Block[] {var1});
				}
				
				isModified = true;
			}
		}
	}
	
	public static void checkContainerList()
	{
		containerList.removeIf(var1 -> !(var1 instanceof BlockContainer)
										   || !(((BlockContainer) var1).createNewTileEntity(Minecraft.getMinecraft().world, 0) instanceof IInventory)
										   || var1==ModBlocks.togglyfier
										   || var1==ModBlocks.changeBlock);
	}
	
	private static void convertExpectedBlocksToMap()
	{
		Iterator var0 = configExpectedBlocks.stringPropertyNames().iterator();
		
		while(var0.hasNext())
		{
			String var1 = (String) var0.next();
			String var2 = configExpectedBlocks.getProperty(var1);
			
			if(var2.trim().equals(""))
			{
				printError("Empty expected blocks entry for " + var1 + "! This shouldn\'t happen!");
			}
			else
			{
				String[] var3 = var2.split(",\\s?");
				ArrayList<Block> var4 = new ArrayList<>();
				
				for(int var5 = 0; var5<var3.length; ++var5)
				{
					try
					{
						var4.add(Block.getBlockFromName(var3[var5]));
					}
					catch(NumberFormatException var7)
					{
						printError("Invalid expected block format for " + var1 + ": " + var3[var5] + ", skipping...", var7);
					}
				}
				
				if(var4.size()!=0)
				{
					expectedBlocksMap.put(new TogglyfierItemInfo(var1), copyArrayList(var4));
				}
			}
		}
	}
	
	private static void convertExpectedBlocksToProps()
	{
		Iterator var0 = expectedBlocksMap.keySet().iterator();
		
		while(var0.hasNext())
		{
			TogglyfierItemInfo var1 = (TogglyfierItemInfo) var0.next();
			Block[] var2 = (Block[]) expectedBlocksMap.get(var1);
			
			if(var2!=null && var2.length>0)
			{
				StringBuilder var3 = (new StringBuilder()).append(var2[0]);
				
				for(int var4 = 1; var4<var2.length; ++var4)
				{
					var3.append(", ").append(var2[var4]);
				}
				
				configExpectedBlocks.setProperty(var1.toString(), var3.toString());
			}
			else
			{
				configExpectedBlocks.remove(var1.toString());
				var0.remove();
			}
		}
	}
	
	private static void convertItemFlagsToMap()
	{
		Iterator var0 = configItemFlags.stringPropertyNames().iterator();
		
		while(var0.hasNext())
		{
			String var1 = (String) var0.next();
			String var2 = configItemFlags.getProperty(var1).toLowerCase(Locale.ENGLISH);
			int var3 = 0;
			String[] var4 = var2.split(",\\s?");
			
			for(int var5 = 0; var5<var4.length; ++var5)
			{
				int var7;
				
				if(var4[var5].startsWith("save"))
				{
					if(var4[var5].length()==4)
					{
						var3 |= 15;
					}
					else
					{
						for(int var6 = 4; var6<var4[var5].length(); ++var6)
						{
							var7 = Integer.valueOf(var4[var5].substring(var6, var6 + 1)).intValue();
							
							if(var7 >= 1 && var7<=4)
							{
								var3 |= 1 << var7 - 1;
							}
						}
					}
				}
				else
				{
					ModConfig.ItemFlags[] var10 = ModConfig.ItemFlags.values();
					var7 = var10.length;
					
					for(int var8 = 0; var8<var7; ++var8)
					{
						ModConfig.ItemFlags var9 = var10[var8];
						
						if(var4[var5].equals(var9.getName()))
						{
							var3 |= var9.getBitValue();
							
							if(var9==ModConfig.ItemFlags.REQUIRES_TILLED_SOIL)
							{
								var3 |= ModConfig.ItemFlags.DOWNWARD_ONLY.getBitValue();
							}
						}
					}
				}
			}
			
			itemFlagsMap.put(new TogglyfierItemInfo(var1), Integer.valueOf(var3));
		}
	}
	
	private static void convertItemFlagsToProps()
	{
		Iterator var0 = itemFlagsMap.keySet().iterator();
		
		while(var0.hasNext())
		{
			TogglyfierItemInfo var1 = (TogglyfierItemInfo) var0.next();
			int var2 = ((Integer) itemFlagsMap.get(var1)).intValue();
			int var3 = var2 & 15;
			StringBuilder var4 = new StringBuilder();
			
			if(var3>0)
			{
				var4.append("save");
				
				if(var3<15)
				{
					for(int var5 = 0; var5<4; ++var5)
					{
						if((var3 & 1 << var5)!=0)
						{
							var4.append(var5 + 1);
						}
					}
				}
			}
			
			ModConfig.ItemFlags[] var9 = ModConfig.ItemFlags.values();
			int var6 = var9.length;
			
			for(int var7 = 4; var7<var6; ++var7)
			{
				ModConfig.ItemFlags var8 = var9[var7];
				
				if((var8.getBitValue() & var2)!=0)
				{
					if(var4.length()>0)
					{
						var4.append(", ");
					}
					
					var4.append(var8.getName());
				}
			}
			
			configItemFlags.setProperty(var1.toString(), var4.toString());
		}
	}
	
	private static void convertPrioritiesToMap()
	{
		Iterator var0 = configPriorities.stringPropertyNames().iterator();
		
		while(var0.hasNext())
		{
			String var1 = (String) var0.next();
			String var2 = configPriorities.getProperty(var1);
			
			if(var2.trim().equals(""))
			{
				printError("Empty priorities entry for " + var1 + "! This shouldn\'t happen!");
			}
			else
			{
				String[] var3 = var2.split(",\\s?");
				ArrayList var4 = new ArrayList();
				
				for(int var5 = 0; var5<var3.length; ++var5)
				{
					try
					{
						var4.add(new TogglyfierItemInfo(var3[var5]));
					}
					catch(NumberFormatException var7)
					{
						printError("Invalid priority format for " + var1 + ": " + var3[var5] + ", skipping...");
					}
				}
				
				if(var4.size()!=0)
				{
					prioritiesMap.put(Integer.valueOf(var1), var4);
				}
			}
		}
	}
	
	private static void convertPrioritiesToProps()
	{
		Iterator var0 = prioritiesMap.keySet().iterator();
		
		while(var0.hasNext())
		{
			Integer var1 = (Integer) var0.next();
			ArrayList var2 = (ArrayList) prioritiesMap.get(var1);
			
			if(var2!=null && var2.size()!=0)
			{
				StringBuilder var3 = (new StringBuilder()).append(((TogglyfierItemInfo) var2.get(0)).toString());
				
				for(int var4 = 1; var4<var2.size(); ++var4)
				{
					var3.append(", ").append(((TogglyfierItemInfo) var2.get(var4)).toString());
				}
				
				configPriorities.setProperty(String.valueOf(var1), var3.toString());
			}
			else
			{
				printError("Priorities entry for priority " + var1 + " is empty! This shouldn\'t happen!");
			}
		}
	}
	
	private static Block[] copyArrayList(ArrayList<Block> var0)
	{
		Block[] var1 = new Block[var0.size()];
		
		for(int var2 = 0; var2<var0.size(); ++var2)
		{
			var1[var2] = var0.get(var2);
		}
		
		return var1;
	}
	
	public static boolean couldBeExpectedBlock(ItemStack var0, Item var1)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		boolean result = true;
		if(var0!=null && var0.getItem()!=Items.AIR && var1!=Items.AIR)
		{
			if(var0.getItem()!=var1)
			{
				Block var3;
				ResourceLocation var4;
				
				if(var0.getItem() instanceof ItemBlock)
				{
					Block var2 = Block.getBlockFromItem(var0.getItem());
					var3 = Block.getBlockFromItem(var1);
					var4 = var2.getRegistryName();
					ResourceLocation var5 = var3.getRegistryName();
					result = var2.getClass()==var3.getClass() && var4!=null && var4.equals(var5);
				}
				else
				{
					Block[] var8 = getValueFromItemInfo(expectedBlocksMap, TogglyfierItemInfo.getFromPool(var0));
					
					boolean working = true;
					if(var8!=null && var8[0]!=Blocks.AIR)
					{
						var3 = Block.getBlockFromItem(var1);
						var4 = var3.getRegistryName();
						
						for(Block var6 : var8)
						{
							ResourceLocation var7 = var6.getRegistryName();
							
							if(var3.getClass()==var6.getClass() && var4!=null && var4.equals(var7))
							{
								working = false;
								break;
							}
						}
					}
					if(working)
					{
						result = false;
					}
				}
			}
		}
		else
		{
			result = false;
		}
		return result;
	}
	
	/**
	 *
	 */
	private static int findExpectedBlock(TogglyfierItemInfo var0, Block var1)
	{
		if(var1!=Blocks.AIR && var0!=null && var0.getItem()!=Items.AIR)
		{
			Block[] var2 = (Block[]) getValueFromItemInfo(expectedBlocksMap, var0);
			
			if(var2!=null)
			{
				for(int var3 = 0; var3<var2.length; ++var3)
				{
					if(var2[var3]==var1)
					{
						return var3;
					}
				}
				
			}
		}
		return -1;
	}
	
	public static boolean getBoolean(TogglyfierOptions var0)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		return Boolean.parseBoolean(configValues.getProperty(var0.getConfigName()));
	}
	
	public static int getExpectedBlockCount(final ItemStack var0)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		if(var0==null || var0.getItem()==Items.AIR)
		{
			return 0;
		}
		else
		{
			final Block[] var1 = getValueFromItemInfo(expectedBlocksMap, TogglyfierItemInfo.getFromPool(var0));
			return var1!=null ? var1.length : 0;
		}
	}
	
	public static Block[] getExpectedBlocks(TogglyfierItemInfo var0)
	{
		return getValueFromItemInfo(expectedBlocksMap, var0);
	}
	
	@Nonnull
	public static Block getFirstExpectedBlock(Item var0)
	{
		if(var0==Items.AIR)
		{
			return Blocks.AIR;
		}
		return getFirstExpectedBlock(new ItemStack(var0, 0, -1));
	}
	
	@Nonnull
	public static Block getFirstExpectedBlock(ItemStack var0)
	{
		if(var0!=null && var0.getItem()!=Items.AIR)
		{
			Block[] var1 = getValueFromItemInfo(expectedBlocksMap, TogglyfierItemInfo.getFromPool(var0));
			if(var1!=null) { return var1[0]; }
		}
		return Blocks.AIR;
	}
	
	public static int getInt(@Nonnull TogglyfierOptions var0)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		assert configValues!=null;
		return Integer.parseInt(configValues.getProperty(var0.getConfigName()));
	}
	
	public static int getItemFlags(TogglyfierItemInfo var0)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		if(var0!=null && var0.getItem()!=Items.AIR)
		{
			Integer var1 = (Integer) getValueFromItemInfo(itemFlagsMap, var0);
			return var1!=null ? var1 : 0;
		}
		else
		{
			return 0;
		}
	}
	
	public static int getItemFlags(ItemStack var0)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		return var0!=null && var0.getItem()!=Items.AIR ? getItemFlags(TogglyfierItemInfo.getFromPool(var0)) : 0;
	}
	
	private static TogglyfierItemInfo getItemInfoForFoundKey(Map var0, TogglyfierItemInfo var1)
	{
		if(var1==null || (var1.getItemDamage() >= 0 && var1.hasSubtypes() && var0.containsKey(var1)))
		{
			return var1;
		}
		else
		{
			TogglyfierItemInfo var2 = var1.stripDamage();
			return var0.containsKey(var2) ? var2 : null;
		}
	}
	
	public static int getPriority(TogglyfierItemInfo var0)
	{
		if(var0!=null && var0.getItem()!=Items.AIR)
		{
			for(int var1 = -1; var1<=3; ++var1)
			{
				if(var1!=0)
				{
					ArrayList var2 = (ArrayList) prioritiesMap.get(Integer.valueOf(var1));
					
					if(var2!=null)
					{
						if(var0.hasSubtypes() && var2.contains(var0))
						{
							return var1;
						}
						
						TogglyfierItemInfo var3 = var0.stripDamage();
						
						if(var2.contains(var3))
						{
							return var1;
						}
					}
				}
			}
			
			return 0;
		}
		else
		{
			return 0;
		}
	}
	
	public static int getPriority(ItemStack var0)
	{
		return var0!=null && var0.getItem()!=Items.AIR ? getPriority(TogglyfierItemInfo.getFromPool(var0)) : 0;
	}
	
	public static int getPriority(Item var0)
	{
		return getPriority(TogglyfierItemInfo.getFromPool(var0, -1));
	}
	
	public static String getString(TogglyfierOptions var0)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		return configValues.getProperty(var0.getConfigName());
	}
	
	private static <T> T getValueFromItemInfo(@Nonnull Map<TogglyfierItemInfo, T> var0, @Nullable TogglyfierItemInfo var1)
	{
		T result = null;
		if(var1!=null)
		{
			result = var0.get(((var1.getItemDamage() >= 0) && var1.hasSubtypes() && var0.containsKey(var1)) ? var1 : var1.stripDamage());
		}
		return result;
	}
	
	public static boolean hasExpectedBlock(@Nonnull ItemStack var0)
	{
		if(var0.isEmpty())
		{
			return false;
		}
		else
		{
			Block[] var1 = getValueFromItemInfo(expectedBlocksMap, TogglyfierItemInfo.getFromPool(var0));
			return var1!=null && var1[0]!=Blocks.AIR;
		}
	}
	
	public static boolean hasExpectedBlock(@Nonnull MutableItemStack var0)
	{
		return hasExpectedBlock(var0.toItemStack());
	}
	
	public static boolean hasExpectedBlock(Item var0)
	{
		return hasExpectedBlock(new ItemStack(var0, 1, -1));
	}
	
	private static void initConfig()
	{
		configValues = new Properties();
		configExpectedBlocks = new Properties();
		configItemFlags = new Properties();
		configPriorities = new Properties();
	}
	
	public static boolean isExpectedBlock(ItemStack var0, Block var1)
	{
		if(configUnread)
		{
			readConfig();
		}
		
		if(var1==Blocks.AIR || var0==null || var0.getItem()==Items.AIR)
		{ return false; }
		
		return (Block.getBlockFromItem(var0.getItem())==var1
					|| findExpectedBlock(TogglyfierItemInfo.getFromPool(var0), var1) >= 0);
	}
	
	public static boolean isInContainerList(Block var0)
	{
		return containerList.contains(var0.getRegistryName().toString());
	}
	
	private static void makeContainerList()
	{
		String var0 = getString(TogglyfierOptions.CONTAINER_BLOCK_NAMES);
		String[] var1 = var0.split(",\\s?");
		
		for(int var2 = 0; var2<var1.length; ++var2)
		{
			try
			{
				containerList.add(Block.getBlockFromName(var1[var2]));
			}
			catch(NumberFormatException var4)
			{
				printError("Invalid property format for " + TogglyfierOptions.CONTAINER_BLOCK_NAMES.getConfigName() + ": " + var1[var2] + ", skipping...");
			}
		}
	}
	
	private static void printError(String var0)
	{
		printError(var0, (Exception) null);
	}
	
	private static void printError(String var0, Exception var1)
	{
		System.out.println("--Toggle Blocks Config-- " + var0);
		
		if(var1!=null)
		{
			var1.printStackTrace();
		}
	}
	
	private static void readConfig()
	{
		FileInputStream var0;
		
		if(oldConfigFile.exists())
		{
			var0 = null;
			
			try
			{
				var0 = new FileInputStream(oldConfigFile);
				configValues.load(var0);
				var0.close();
				oldConfigFile.delete();
			}
			catch(IOException var144)
			{
				printError("Failed to read old config file!", var144);
				
				try
				{
					if(var0!=null)
					{
						var0.close();
					}
				}
				catch(IOException var143)
				{
					;
				}
			}
		}
		else if(configFile.exists())
		{
			var0 = null;
			
			try
			{
				var0 = new FileInputStream(configFile);
				configValues.load(var0);
			}
			catch(IOException var141)
			{
				printError("Failed to read config!", var141);
			}
			finally
			{
				try
				{
					if(var0!=null)
					{
						var0.close();
					}
				}
				catch(IOException var132)
				{
					;
				}
			}
		}
		
		boolean var148 = false;
		TogglyfierOptions[] var1 = TogglyfierOptions.values();
		int var2 = var1.length;
		int var3;
		TogglyfierOptions var4;
		
		for(var3 = 0; var3<var2; ++var3)
		{
			var4 = var1[var3];
			
			if(configValues.getProperty(var4.getConfigName())==null)
			{
				configValues.setProperty(var4.getConfigName(), var4.getDefaultValue());
				var148 = true;
			}
		}
		
		if(var148)
		{
			FileOutputStream var147 = null;
			
			try
			{
				if(!configDir.exists())
				{
					configDir.mkdirs();
				}
				
				StringBuilder var149 = new StringBuilder(configHeader);
				TogglyfierOptions[] var152 = TogglyfierOptions.values();
				int var153 = var152.length;
				
				for(int var5 = 0; var5<var153; ++var5)
				{
					TogglyfierOptions var6 = var152[var5];
					var149.append('\n').append(var6.getConfigName()).append(" (Default ").append(var6.getDefaultValue());
					
					if(var6.hasMinAndMax())
					{
						var149.append(" Min = ").append(var6.getMinValue()).append(" Max = ").append(var6.getMaxValue());
					}
					
					var149.append("): ").append(var6.getDescription());
				}
				
				var147 = new FileOutputStream(configFile);
				configValues.store(var147, var149.toString());
			}
			catch(IOException var145)
			{
				printError("Failed to write config!", var145);
			}
			finally
			{
				try
				{
					if(var147!=null)
					{
						var147.close();
					}
				}
				catch(IOException var133)
				{
					;
				}
			}
		}
		
		var1 = TogglyfierOptions.values();
		var2 = var1.length;
		
		for(var3 = 0; var3<var2; ++var3)
		{
			var4 = var1[var3];
			
			if(!var4.validate(configValues))
			{
				configValues.setProperty(var4.getConfigName(), var4.getDefaultValue());
			}
		}
		
		boolean var150 = false;
		
		if(configBlockItemDir.exists())
		{
			FileInputStream var151;
			
			if(configExpectedBlocksFile.exists())
			{
				var151 = null;
				
				try
				{
					var151 = new FileInputStream(configExpectedBlocksFile);
					configExpectedBlocks.load(var151);
				}
				catch(IOException var139)
				{
					printError("Failed to load expected blocks file!", var139);
				}
				finally
				{
					try
					{
						if(var151!=null)
						{
							var151.close();
						}
					}
					catch(IOException var134)
					{
						;
					}
				}
			}
			else
			{
				var150 = true;
			}
			
			if(configItemFlagsFile.exists())
			{
				var151 = null;
				
				try
				{
					var151 = new FileInputStream(configItemFlagsFile);
					configItemFlags.load(var151);
				}
				catch(IOException var137)
				{
					printError("Failed to load item flags file!", var137);
				}
				finally
				{
					try
					{
						if(var151!=null)
						{
							var151.close();
						}
					}
					catch(IOException var131)
					{
						;
					}
				}
			}
			else
			{
				var150 = true;
			}
			
			if(configPrioritiesFile.exists())
			{
				var151 = null;
				
				try
				{
					var151 = new FileInputStream(configPrioritiesFile);
					configPriorities.load(var151);
				}
				catch(IOException var135)
				{
					printError("Failed to load priorities file!", var135);
				}
				finally
				{
					try
					{
						if(var151!=null)
						{
							var151.close();
						}
					}
					catch(IOException var130)
					{
						;
					}
				}
			}
			else
			{
				var150 = true;
			}
		}
		else
		{
			var150 = true;
		}
		
		for(var2 = 0; var2<defaultExpectedBlocks.length; ++var2)
		{
			if(!configExpectedBlocks.containsKey(defaultExpectedBlocks[var2][0]) ||
				   !configExpectedBlocks.getProperty(defaultExpectedBlocks[var2][0]).equalsIgnoreCase(defaultExpectedBlocks[var2][1]))
			{
				configExpectedBlocks.setProperty(defaultExpectedBlocks[var2][0], defaultExpectedBlocks[var2][1]);
				var150 = true;
			}
		}
		
		for(var2 = 0; var2<defaultItemFlags.length; ++var2)
		{
			if(!configItemFlags.containsKey(defaultItemFlags[var2][0]) || !configItemFlags.getProperty(defaultItemFlags[var2][0]).equalsIgnoreCase(defaultItemFlags[var2][1]))
			{
				configItemFlags.setProperty(defaultItemFlags[var2][0], defaultItemFlags[var2][1]);
				var150 = true;
			}
		}
		
		for(var2 = 0; var2<defaultPriorities.length; ++var2)
		{
			if(!configPriorities.containsKey(defaultPriorities[var2][0]))
			{
				configPriorities.setProperty(defaultPriorities[var2][0], defaultPriorities[var2][1]);
				var150 = true;
			}
			else
			{
				boolean var154 = false;
				String var155 = configPriorities.getProperty(defaultPriorities[var2][0]);
				String[] var156 = var155.split(",\\s?");
				String[] var157 = defaultPriorities[var2][1].split(",\\s?");
				
				for(int var7 = 0; var7<var157.length; ++var7)
				{
					boolean var8 = false;
					
					for(int var9 = 0; var9<var156.length; ++var9)
					{
						if(var157[var7].equals(var156[var9]))
						{
							var8 = true;
							break;
						}
					}
					
					if(!var8)
					{
						var155 = var155 + ", " + var157[var7];
						var154 = true;
					}
				}
				
				if(var154)
				{
					configPriorities.setProperty(defaultPriorities[var2][0], var155);
					var150 = true;
				}
			}
		}
		
		if(var150)
		{
			isModified = true;
			saveBlockItemSettings();
		}
		
		configUnread = false;
		convertExpectedBlocksToMap();
		convertItemFlagsToMap();
		convertPrioritiesToMap();
		makeContainerList();
	}
	
	public static synchronized void saveBlockItemSettings()
	{
		if(isModified || !configExpectedBlocksFile.exists() || !configItemFlagsFile.exists() || !configPrioritiesFile.exists())
		{
			convertExpectedBlocksToProps();
			convertItemFlagsToProps();
			convertPrioritiesToProps();
			
			if(!configBlockItemDir.exists())
			{
				configBlockItemDir.mkdirs();
			}
			
			FileOutputStream var0 = null;
			
			try
			{
				var0 = new FileOutputStream(configExpectedBlocksFile);
				configExpectedBlocks.store(var0, "---Expected blocks---");
			}
			catch(IOException var54)
			{
				printError("Failed to save expected blocks file!", var54);
			}
			finally
			{
				try
				{
					if(var0!=null)
					{
						var0.close();
					}
				}
				catch(IOException var49)
				{
					;
				}
			}
			
			FileOutputStream var1 = null;
			
			try
			{
				var1 = new FileOutputStream(configItemFlagsFile);
				configItemFlags.store(var1, "---Item flags---");
			}
			catch(IOException var52)
			{
				printError("Failed to save item flags file!", var52);
			}
			finally
			{
				try
				{
					if(var1!=null)
					{
						var1.close();
					}
				}
				catch(IOException var48)
				{
					;
				}
			}
			
			FileOutputStream var2 = null;
			
			try
			{
				var2 = new FileOutputStream(configPrioritiesFile);
				configPriorities.store(var2, "---Priorities---");
			}
			catch(IOException var50)
			{
				printError("Failed to save priorities file!", var50);
			}
			finally
			{
				try
				{
					if(var2!=null)
					{
						var2.close();
					}
				}
				catch(IOException var47)
				{
					;
				}
			}
			
			isModified = false;
		}
	}
	
	public static void setExpectedBlocks(TogglyfierItemInfo var0, List<Block> var1)
	{
		if(var1.size()>0)
		{
			Block[] var2 = new Block[var1.size()];
			
			for(int var3 = 0; var3<var2.length; ++var3)
			{
				var2[var3] = (var1.get(var3));
			}
			
			expectedBlocksMap.put(var0.copy(), var2);
		}
		else
		{
			expectedBlocksMap.remove(var0);
			configExpectedBlocks.remove(var0.toString());
		}
		
		isModified = true;
	}
	
	public static void setItemFlags(TogglyfierItemInfo var0, int var1)
	{
		if(var1>0)
		{
			itemFlagsMap.put(var0.copy(), Integer.valueOf(var1));
		}
		else
		{
			itemFlagsMap.remove(var0);
			configItemFlags.remove(var0.toString());
		}
		
		isModified = true;
	}
	
	public static void setPriority(TogglyfierItemInfo var0, int var1)
	{
		int var2 = getPriority(var0);
		
		if(var2!=0)
		{
			((ArrayList) prioritiesMap.get(Integer.valueOf(var2))).remove(var0);
		}
		
		if(var1!=0 && (!var0.hasSubtypes() || var0.getItemDamage()<0 || var1!=getPriority(var0.stripDamage())))
		{
			((ArrayList) prioritiesMap.get(Integer.valueOf(var1))).add(var0.copy());
		}
		
		isModified = true;
	}
	
	static void loadConfigFile(File suggestedConfigurationFile, Side side)
	{
	}
	
	public enum ItemFlags
	{
		SAVE1("save", "1"),
		SAVE2("save", "2"),
		SAVE3("save", "3"),
		SAVE4("save", "4"),
		DOWNWARD_ONLY("down", "Place downward"),
		REQUIRES_TILLED_SOIL("seeds", "Requires tilled soil"),
		DROP_AS_ITEM("drop", "Drop as item"),
		IS_DOUBLE_BLOCK("double", "Double block"),
		USES_UP_AND_DOWN("updown", "Uses up/down for alignment"),
		MELON_AND_PUMPKIN_GROWTH("pumpkin", "Grows like melons/pumpkins");
		private final String flagName;
		private final String displayName;
		
		ItemFlags(String name, String desc)
		{
			this.flagName = name;
			this.displayName = desc;
		}
		
		public int getBitValue()
		{
			return 1 << this.ordinal();
		}
		
		public String getDisplayName()
		{
			return this.displayName;
		}
		
		public String getName()
		{
			return this.flagName;
		}
	}
	
	public enum TogglyfierOptions
	{
		MAX_CB_DISTANCE("maxcbdistance", "48", 1, 127, "Maximum distance a Change Block can be from a Toggle Block"),
		MOVE_FLINT_AND_STEEL("moveflintandsteel", "true", Boolean.TYPE, "Move Flint and Steel from Change Blocks to a nearby container?"),
		CENTER_ITEM_NAME("centeritemname", "togglyfiers:togglification_core", String.class, "Registry name of the center item used in Toggle Block recipes"),
		ALT_MEDIUM_IRON_RECIPE("altmedironrecipe", "false", Boolean.TYPE, "If true, move center item in recipe for medium Iron Toggle Block to bottom center"),
		IGNORE_EMPTY_CB("ignoreemptycb", "true", Boolean.TYPE, "Ignore empty Change Blocks when checking for errors?"),
		CONTAINER_CHECK_RADIUS("containercheckradius", "2", 0, 5, "Default radius to check for containers when searching for items"),
		CONTAINER_BLOCK_NAMES("containerblocks", "minecraft:chest", String.class, "Comma-delimited list of container block names to search for items"),
		STACK_TOGGLE_BLOCKS("stacktoggleblocks", "false", Boolean.TYPE, "If true, Toggle Blocks can be stacked up to 16");
		
		@Nonnull private final String optionName;
		private final String optionDefaultValue;
		private final int optionMinValue;
		private final int optionMaxValue;
		private final Serializable optionType;
		private final String optionDesc;
		
		TogglyfierOptions(@Nonnull String name, String defaultValue, String desc, Serializable type, int minimum, int maximum)
		{
			optionName = name;
			optionDefaultValue = defaultValue;
			optionDesc = desc;
			optionType = type;
			optionMinValue = minimum;
			optionMaxValue = maximum;
		}
		
		TogglyfierOptions(String name, String defaultValue, Serializable type, String desc)
		{
			this(name, defaultValue, desc, type, 0, 0);
		}
		
		TogglyfierOptions(String name, String defaultValue, int minValue, int maxValue, String desc)
		{
			this(name, defaultValue, desc, Integer.TYPE, minValue, maxValue);
		}
		
		@Nonnull
		public String getConfigName()
		{
			return this.optionName;
		}
		
		public String getDefaultValue()
		{
			return this.optionDefaultValue;
		}
		
		public String getDescription()
		{
			return this.optionDesc;
		}
		
		public int getMaxValue()
		{
			return this.optionMaxValue;
		}
		
		public int getMinValue()
		{
			return this.optionMinValue;
		}
		
		public boolean hasMinAndMax()
		{
			return (optionMinValue>0) || (optionMaxValue>0);
		}
		
		public boolean validate(Properties props)
		{
			final String value = props.getProperty(optionName);
			
			try
			{
				if(value==null)
				{
					return false;
				}
				else if(optionType==Integer.TYPE)
				{
					final int valueAsInt = Integer.parseInt(value);
					
					if(valueAsInt<optionMinValue || valueAsInt>optionMaxValue)
					{
						ModConfig.printError("Value for option " + optionName + " (" + valueAsInt + ") outside of range (" + optionMinValue + " to " + optionMaxValue + ")!");
						return false;
					}
				}
				else if(this.optionType==Boolean.TYPE && !("true".equalsIgnoreCase(value)))
				{
					final float var8 = Float.parseFloat(value);
					props.setProperty(optionName, String.valueOf(var8!=0.0F));
				}
			}
			catch(NumberFormatException ex)
			{
				ModConfig.printError("Invalid format for option " + optionName + '!');
			}
			return true;
		}
	}
	
	static
	{
		initConfig();
	}
}
