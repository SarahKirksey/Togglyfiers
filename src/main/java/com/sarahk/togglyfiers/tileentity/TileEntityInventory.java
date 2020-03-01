package com.sarahk.togglyfiers.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public abstract class TileEntityInventory extends TileEntity implements IInventory
{
	private final HashMap<Integer, Integer> fields = new HashMap<>();
	
	TileEntityInventory(final String defaultName)
	{
		super();
		this.defaultName = defaultName;
	}
	
	@Override public int getField(final int id)                   { return fields.get(id); }
	@Override public void setField(final int id, final int value) { fields.put(id, value); }
	@Override public int getFieldCount()                          { return fields.size(); }
	
	protected final String defaultName;
	
	/**
	 * Get the name of this object. For players this returns their username
	 */
	@Override
	public String getName()
	{
		return null;
	}
	
	/**
	 * Returns true if this thing is named
	 */
	@Override
	public boolean hasCustomName()
	{
		return false;
	}
	
	/**
	 * Returns the number of slots in the inventory.
	 */
	@Override
	public int getSizeInventory()
	{
		return 0;
	}
	
	@Override
	public boolean isEmpty()
	{
		return false;
	}
	
	/**
	 * Returns the stack in the given slot.
	 */
	@Override
	public ItemStack getStackInSlot(final int index)
	{
		return null;
	}
	
	/**
	 * Removes up to a specified number of items from an inventory slot and returns them in a new stack.
	 */
	@Override
	public ItemStack decrStackSize(final int index, final int count)
	{
		return null;
	}
	
	/**
	 * Removes a stack from the given slot and returns it.
	 */
	@Override
	public ItemStack removeStackFromSlot(final int index)
	{
		return null;
	}
	
	/**
	 * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
	 */
	@Override
	public void setInventorySlotContents(final int index, final ItemStack stack)
	{
	
	}
	
	/**
	 * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended.
	 */
	@Override
	public int getInventoryStackLimit()
	{
		return 0;
	}
	
	/**
	 * Don't rename this method to canInteractWith due to conflicts with Container
	 */
	@Override
	public boolean isUsableByPlayer(final EntityPlayer player)
	{
		return false;
	}
	
	@Override
	public void openInventory(final EntityPlayer player)
	{
	
	}
	
	@Override
	public void closeInventory(final EntityPlayer player)
	{
	
	}
	
	/**
	 * Returns true if automation is allowed to insert the given stack (ignoring stack size) into the given slot. For
	 * guis use Slot.isItemValid
	 */
	@Override
	public boolean isItemValidForSlot(final int index, final ItemStack stack)
	{
		return false;
	}
	
	@Override
	public void clear()
	{
	
	}
}
