package com.sarahk.togglyfiers.gui.container;

import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

public class ContainerTogglyfier extends Container
{
    protected final TileEntityTogglyfier tileentity;
    public ContainerTogglyfier(InventoryPlayer player, TileEntityTogglyfier tileEntity)
    {
        this.tileentity = tileEntity;

        this.addSlotToContainer(new Slot(tileEntity, 0, 43, 34));

        for(int y = 0; y < 3; y++)
            for(int x = 0; x < 9; x++)
                this.addSlotToContainer(new Slot(player, x + y*9 + 9, x*18 + 8, 84 + y*18));

        for(int x = 0; x < 9; x++)
            this.addSlotToContainer(new Slot(player, x, 8 + x*18, 142));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tileentity.isUsableByPlayer(playerIn);
    }
}
