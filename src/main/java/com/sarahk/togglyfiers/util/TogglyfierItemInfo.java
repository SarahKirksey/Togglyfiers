package com.sarahk.togglyfiers.util;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nonnull;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TogglyfierItemInfo
{
	private static final List<TogglyfierItemInfo> infoList = new ArrayList<>();
	private static int infoCount = 0;
	private Item item;
	private short itemDamage;
	private NBTTagCompound nbt;
	
	public TogglyfierItemInfo(Item var1, int var2)
	{
		this.item = var1;
		this.itemDamage = (short) var2;
	}
	
	public TogglyfierItemInfo(@Nonnull Block var1, int var2)
	{
		this(Item.getItemFromBlock(var1), var2);
	}
	
	public TogglyfierItemInfo(Item var1, int var2, NBTTagCompound nbt)
	{
		this(var1, var2);
		this.nbt = nbt;
	}
	
	public TogglyfierItemInfo(ItemStack var1)
	{
		this(var1.getItem(), (short) (var1.getItem().getHasSubtypes() ? var1.getItemDamage() : -1));
	}
	
	public TogglyfierItemInfo(String var1)
	{
		String[] var2 = var1.split("\\.");
		setItem(Item.getByNameOrId((var2[0])));
		this.setItemDamage(var2.length>1 ? Short.parseShort(var2[1]) : -1);
	}
	
	private static TogglyfierItemInfo checkPool()
	{
		if(infoCount >= infoList.size())
		{
			TogglyfierItemInfo var0 = new TogglyfierItemInfo(Items.AIR, 0);
			infoList.add(var0);
			return var0;
		}
		else
		{
			return infoList.get(infoCount++);
		}
	}
	
	public static void clearPool()
	{
		infoCount = 0;
	}
	
	public static synchronized TogglyfierItemInfo getFromPool(String var0)
	{
		return checkPool().setInfo(var0);
	}
	
	public static synchronized TogglyfierItemInfo getFromPool(Item var0, int var1)
	{
		return checkPool().setInfo(var0, var1);
	}
	
	public static synchronized TogglyfierItemInfo getFromPool(ItemStack var0)
	{
		return checkPool().setInfo(var0);
	}
	
	public static TogglyfierItemInfo readFromStream(DataInputStream var0)
	{
		try
		{
			return new TogglyfierItemInfo(Item.getByNameOrId(var0.readUTF()), var0.readShort());
		}
		catch(IOException var2)
		{
			var2.printStackTrace();
			return null;
		}
	}
	
	public Item getItem()
	{
		return item;
	}
	
	public void setItem(Item item)
	{
		this.item = item;
	}
	
	public short getItemDamage()
	{
		return itemDamage;
	}
	
	public void setItemDamage(short itemDamage)
	{
		this.itemDamage = itemDamage;
	}
	
	public TogglyfierItemInfo copy()
	{
		return new TogglyfierItemInfo(getItem(), this.getItemDamage());
	}
	
	public boolean hasSubtypes()
	{
		return getItem().getHasSubtypes();
	}
	
	@Override
	public int hashCode()
	{
		return getItem().hashCode() << 16 | this.getItemDamage();
	}
	
	@Override
	public boolean equals(Object var1)
	{
		if(!(var1 instanceof TogglyfierItemInfo))
		{
			return false;
		}
		else
		{
			TogglyfierItemInfo var2 = (TogglyfierItemInfo) var1;
			return var2.getItemDamage()==this.getItemDamage() && var2.getItem()==getItem();
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder var1 = new StringBuilder();
		var1.append(getItem());
		
		if(this.hasSubtypes() && this.getItemDamage() >= 0)
		{
			var1.append('.').append(this.getItemDamage());
		}
		
		return var1.toString();
	}
	
	public TogglyfierItemInfo setInfo(Item var1, int var2)
	{
		setItem(var1);
		this.setItemDamage((short) var2);
		return this;
	}
	
	public TogglyfierItemInfo setInfo(ItemStack var1)
	{
		setItem(var1.getItem());
		this.setItemDamage((short) (var1.getItem().getHasSubtypes() ? var1.getItemDamage() : -1));
		return this;
	}
	
	public TogglyfierItemInfo setInfo(String var1)
	{
		String[] var2 = var1.split("\\.");
		setItem(Item.getByNameOrId(var2[0]));
		this.setItemDamage(var2.length>1 ? Short.parseShort(var2[1]) : -1);
		return this;
	}
	
	public TogglyfierItemInfo stripDamage()
	{
		return getFromPool(getItem(), -1);
	}
	
	public TogglyfierItemInfo stripDamageAndCopy()
	{
		return new TogglyfierItemInfo(getItem(), -1);
	}
	
	public void writeToStream(DataOutput var1) throws IOException
	{
		var1.writeUTF(Objects.requireNonNull(getItem().getRegistryName()).toString());
		var1.writeShort(this.getItemDamage());
	}
}
