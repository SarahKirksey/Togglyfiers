package com.sarahk.togglyfiers.tileentity;

import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.listeners.ListenerRegister;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class TileEntityTogglyfier extends TileEntity implements IInventory, ITickable
{
    protected ItemStack changeBlock = new ItemStack(ModBlocks.changeBlock, 10);
    private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>withSize(3, ItemStack.EMPTY);
    private String customName;
    public boolean powered = false;
    private static int TickCount=0;

    public TileEntityTogglyfier(World worldin)
    {
        inventory.set(0, changeBlock);
        
    }

    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty()
    {
        for(ItemStack stack : this.inventory)
            if(!stack.isEmpty())return false;
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {return (ItemStack)this.inventory.get(index);}

    @Override
    public ItemStack decrStackSize(int index, int count) {return ItemStackHelper.getAndSplit(this.inventory, index, count);}

    @Override
    public ItemStack removeStackFromSlot(int index) {return ItemStackHelper.getAndRemove(this.inventory, index);}

    @Override
    public void setInventorySlotContents(int index, ItemStack stack)
    {
        ItemStack oldStack = (ItemStack)inventory.get(index);
        boolean isStackable = !stack.isEmpty() && stack.isItemEqual(oldStack) && ItemStack.areItemStackTagsEqual(stack, oldStack);
        this.inventory.set(index, stack);

        if(stack.getCount() > this.getInventoryStackLimit())
            stack.setCount(this.getInventoryStackLimit());
    }

    @Override
    public int getInventoryStackLimit() {return 64;}

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {return this.world.getTileEntity(pos) != this ? false : player.getDistanceSq(pos) <= 64;}

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack)
    {
        if(index == 0) return stack.isItemEqual(changeBlock);
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {inventory.clear();}

    //NBT
    

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        ItemStackHelper.saveAllItems(compound, inventory);
        if(hasCustomName()) compound.setString("CustomName", customName);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);

        inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, inventory);
        if(compound.hasKey("CustomName, 8")) this.setCustomName(compound.getString("CustomName"));
    }

    //Custom Name
    @Override
    public ITextComponent getDisplayName() {return this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName());}
    @Override
    public String getName() {return this.hasCustomName() ? customName : "container.togglyfier";}
    @Override
    public boolean hasCustomName() {return (customName != null && !customName.isEmpty());}
    public void setCustomName(String customName) {this.customName = customName;}
    
    @Override
    public void update() {
    	TickCount=((TickCount+1)%20);
    	if(TickCount==0) {
    		ListenerRegister.GetListeners(world, ModBlocks.togglifyer).OnPlaced(pos);
    	}
    }
}
