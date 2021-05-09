package com.sarahk.togglyfiers.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MutableItemStack
{
	private Item item = null;
	private int count = 0;
	private int damage = 0;
	private NBTTagCompound nbt = null;
	
	@NotNull private ItemStack wrappedStack = ItemStack.EMPTY;
	
	public MutableItemStack(final Block blockIn)
	{
		this(Item.getItemFromBlock(blockIn), 1);
	}
	
	public MutableItemStack(final Block blockIn, final int amount)
	{
		this(Item.getItemFromBlock(blockIn), amount);
	}
	
	public MutableItemStack(final Block blockIn, final int amount, final int meta)
	{
		this(Item.getItemFromBlock(blockIn), amount, meta);
	}
	
	public MutableItemStack(final Item itemIn)
	{
		this(itemIn, 1);
	}
	
	public MutableItemStack(final Item itemIn, final int amount)
	{
		this(itemIn, amount, 0);
	}
	
	public MutableItemStack(final Item itemIn, final int amount, final int meta)
	{
		this(itemIn, amount, meta, null);
	}
	
	public MutableItemStack(@NotNull final Item itemIn, final int amount, final int meta, @Nullable final NBTTagCompound capNBT)
	{
		this.item = itemIn;
		this.count = amount;
		this.damage = meta;
		this.nbt = capNBT;
	}
	
	public MutableItemStack(final NBTTagCompound compound)
	{
		this(new ItemStack(compound));
	}
	
	public MutableItemStack(@NotNull final ItemStack stack)
	{
		this(stack.getItem(), stack.getCount(), stack.getMetadata(), stack.getTagCompound());
	}
	
	@SuppressWarnings("CopyConstructorMissesField")	// The field this constructor fails to copy is "wrappedStack", which is meant to be generated on this side.
	public MutableItemStack(MutableItemStack stack)
	{
		this(stack.item, stack.count, stack.damage, stack.nbt);
	}
	
	@NotNull public ItemStack toItemStack()
	{
		return wrappedStack.copy();
	}
	
	public boolean isEmpty()
	{
		return wrappedStack.isEmpty();
	}
	
	public void setCount(final int count)
	{
		this.count = count;
		wrappedStack.setCount(count);
	}
	
	public void changeCount(final int count)
	{
		this.count += count;
		wrappedStack.setCount(this.count);
	}
	
	public void setAmount(final int count)
	{
		this.count = count;
		wrappedStack.setCount(count);
	}
	
	public void setItem(final Item item)
	{
		this.item = item;
		wrappedStack = new ItemStack(this.item, count, getMetadata(), nbt);
	}
	
	public void setDamage(final int damage)
	{
		this.damage = damage;
		wrappedStack.setItemDamage(damage);
	}
	
	public void setNbt(final NBTTagCompound nbt)
	{
		this.nbt = nbt;
		wrappedStack.setTagCompound(nbt);
	}
	
	public void setTagCompound(final NBTTagCompound nbt)
	{
		this.nbt = nbt;
		wrappedStack.setTagCompound(nbt);
	}
	
	public int getDamage()
	{
		return damage;
	}
	public int getItemDamage()
	{
		return wrappedStack.getItemDamage();
	}
	public int getMetadata()
	{
		return damage;
	}
	
	@Nullable
	public Item getItem()
	{
		return item;
	}
	
	public int getCount()
	{
		return count;
	}
	
	public NBTTagCompound getNbt()
	{
		return nbt;
	}
	public NBTTagCompound getTagCompound()
	{
		return nbt;
	}
	
	public void set(@NotNull ItemStack stack)
	{
		this.item = stack.getItem();
		this.damage = stack.getMetadata();
		this.count = stack.getCount();
		this.nbt = stack.getTagCompound();
		
		this.wrappedStack = stack.copy();
	}
	
	public void set(@NotNull MutableItemStack stack)
	{
		this.item = stack.getItem();
		this.damage = stack.getMetadata();
		this.count = stack.getCount();
		this.nbt = stack.getTagCompound();
		
		this.wrappedStack = new ItemStack(item, count, damage, nbt);
	}
	
	@NotNull
	public ItemStack splitStack(int amount)
	{
		ItemStack result = wrappedStack.splitStack(amount);
		set(wrappedStack);	// This preserves the side-effects associated with calling this function.
		return result;
	}
}
