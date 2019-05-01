package com.sarahk.togglyfiers.gui;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.gui.container.ContainerTogglyfier;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiTogglyfier extends GuiContainer
{
    private static final ResourceLocation GUI = new ResourceLocation(Main.MODID, "textures/gui/togglyfier.png");
    private final InventoryPlayer player;
    private final TileEntityTogglyfier tileEntity;

    public GuiTogglyfier(InventoryPlayer player, TileEntityTogglyfier tileEntity)
    {
        super(new ContainerTogglyfier(player, tileEntity));
        this.player = player;
        this.tileEntity = tileEntity;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        String tileName = tileEntity
                .getDisplayName()
                .getUnformattedText();
        this.fontRenderer.drawString(tileName, (xSize / 2 - fontRenderer.getStringWidth(tileName) / 2), fontRenderer.FONT_HEIGHT/2, 4210752);
        this.fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.mc.getTextureManager().bindTexture(GUI);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

    }
}
