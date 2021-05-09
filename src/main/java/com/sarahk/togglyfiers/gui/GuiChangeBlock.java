package com.sarahk.togglyfiers.gui;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.items.ItemTogglyfierAssistant;
import com.sarahk.togglyfiers.tileentity.TileEntityChangeBlock;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiChangeBlock extends GuiContainer implements ITogglyfierGuiBase
{
	private static final String labelDefault = "Use";
	private static final String labelOverride = "Drop";
	private static final String labelGetAligner = "Get Aligner";
	private static final String labelRemoveAligner = "Remove Aligner";
	private IInventory invPlayer;
	private TileEntityChangeBlock tileentCB;
	private GuiButton guiAlignerButton = null;
	RenderItem itemRenderer = new RenderItem(Minecraft.getMinecraft().renderEngine, null, new ItemColors());
	
	public GuiChangeBlock(IInventory var1, TileEntityChangeBlock var2)
	{
		super(new TileEntityChangeBlock.ContainerChangeBlock(var1, var2));
		this.buttonList = new ArrayList<>();
		this.invPlayer = var1;
		this.tileentCB = var2;
		itemRenderer = new RenderItem(mc.renderEngine, null, new ItemColors());
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		String[] var1 = new String[] {"", ""};
		
		for (int var2 = 0; var2 < 2; ++var2)
		{
			var1[var2] = this.tileentCB.getOverride(var2) ? "Drop" : "Use";
		}
		
		this.buttonList.add(new GuiTogglyfierShortButton(0, this.guiLeft + 64, this.guiTop + 20, 60, 16, var1[0]));
		this.buttonList.add(new GuiTogglyfierShortButton(1, this.guiLeft + 64, this.guiTop + 51, 60, 16, var1[1]));
		
		if (Main.isPlayerOp(this.mc.player))
		{
			this.buttonList.add(new GuiTogglyfierShortButton(10, this.guiLeft + this.xSize - 16, this.guiTop - 16, 16, 16, "+"));
		}
		
		if (this.mc.playerController.isInCreativeMode())
		{
			this.guiAlignerButton = new GuiTogglyfierShortButton(11, this.guiLeft, this.guiTop - 16, 80, 16, "");
			this.updateAlignerButtonText();
			this.buttonList.add(this.guiAlignerButton);
		}
	}
	
	@Override
	public TileEntityTogglyfier getTogglyfier()
	{
		return tileentCB.getTogglyfier();
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int var1, int var2, float var3)
	{
		super.drawScreen(var1, var2, var3);
		GL11.glPushMatrix();
		GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glTranslatef((float)this.guiLeft, (float)this.guiTop, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		
		for (int var4 = 0; var4 < 2; ++var4)
		{
			Slot var5 = this.inventorySlots.inventorySlots.get(var4);
			
			if (var5.getStack().isEmpty())
			{
				ItemStack var6 = this.tileentCB.getReplenish(var4);
				
				if (!var6.isEmpty())
				{
					int var7 = var5.xPos;
					int var8 = var5.yPos;
					itemRenderer.renderItemIntoGUI(var6, var7, var8);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					this.fontRenderer.drawStringWithShadow("0", var7 + 17 - this.fontRenderer.getStringWidth("0"), var8 + 9, 16777215);
					int var9 = var1 - this.guiLeft;
					int var10 = var2 - this.guiTop;
					
					if (this.mc.player.inventory.getItemStack().isEmpty() && var9 >= var7 - 1 && var9 <= var7 + 16 && var10 >= var8 - 1 && var10 <= var8 + 16)
					{
						List<String> var11 = var6.getTooltip(
							mc.player,
							mc.gameSettings.advancedItemTooltips
								? ITooltipFlag.TooltipFlags.ADVANCED
								: ITooltipFlag.TooltipFlags.NORMAL);
						
						if (!var11.isEmpty())
						{
							int var12 = 0;
							int var13;
							int var14;
							
							for (var13 = 0; var13 < var11.size(); ++var13)
							{
								var14 = this.fontRenderer.getStringWidth(var11.get(var13));
								
								if (var14 > var12)
								{
									var12 = var14;
								}
							}
							
							var13 = var1 - this.guiLeft + 12;
							var14 = var2 - this.guiTop - 12;
							int var16 = 8;
							
							if (var11.size() > 1)
							{
								var16 += 2 + (var11.size() - 1) * 10;
							}
							
							this.zLevel = 300.0F;
							itemRenderer.zLevel = 300.0F;
							int var17 = -267386864;
							this.drawGradientRect(var13 - 3, var14 - 4, var13 + var12 + 3, var14 - 3, var17, var17);
							this.drawGradientRect(var13 - 3, var14 + var16 + 3, var13 + var12 + 3, var14 + var16 + 4, var17, var17);
							this.drawGradientRect(var13 - 3, var14 - 3, var13 + var12 + 3, var14 + var16 + 3, var17, var17);
							this.drawGradientRect(var13 - 4, var14 - 3, var13 - 3, var14 + var16 + 3, var17, var17);
							this.drawGradientRect(var13 + var12 + 3, var14 - 3, var13 + var12 + 4, var14 + var16 + 3, var17, var17);
							int var18 = 1347420415;
							int var19 = (var18 & 16711422) >> 1 | var18 & -16777216;
							this.drawGradientRect(var13 - 3, var14 - 3 + 1, var13 - 3 + 1, var14 + var16 + 3 - 1, var18, var19);
							this.drawGradientRect(var13 + var12 + 2, var14 - 3 + 1, var13 + var12 + 3, var14 + var16 + 3 - 1, var18, var19);
							this.drawGradientRect(var13 - 3, var14 - 3, var13 + var12 + 3, var14 - 3 + 1, var18, var18);
							this.drawGradientRect(var13 - 3, var14 + var16 + 2, var13 + var12 + 3, var14 + var16 + 3, var19, var19);
							
							final int size = var11.size();
							for (int var20 = 0; var20 < size; ++var20)
							{
								String var21 = var11.get(var20);
								var21 = var20==0? "\u00a7" + Integer.toHexString(var6.getRarity().color.getColorIndex()) + var21 : "\u00a77" + var21;
								this.fontRenderer.drawStringWithShadow(var21, var13, var14, -1);
								var14 += var20==0? 12 : 10;
							}
							
							this.zLevel = 0.0F;
							itemRenderer.zLevel = 0.0F;
						}
					}
					
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
		}
		
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glPopMatrix();
	}
	
	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	protected void drawGuiContainerForegroundLayer()
	{
		this.fontRenderer.drawString("Off", 8, 26, 4210752);
		this.fontRenderer.drawString("On", 8, 56, 4210752);
		this.fontRenderer.drawString(StatCollector.translateToLocal(this.invPlayer.getInvName()), 8, this.ySize - 96 + 2, 4210752);
		
		if (this.tileentCB.errorState != TileEntityChangeBlock$ChangeBlockError.NO_ERROR && (this.tileentCB.errorState != TileEntityChangeBlock$ChangeBlockError.CHANGE_BLOCK_EMPTY || !BlockTBChange.ignoreEmpty))
		{
			this.fontRenderer.drawString(this.tileentCB.errorState.getErrorMessage(), 8, 8, 4210752);
		}
	}
	 */
	
	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float var1, int var2, int var3)
	{
		ResourceLocation var4 = new ResourceLocation("/gui/changeblock.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(var4);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton var1)
	{
		if (var1.enabled)
		{
			if (var1.id >= 0 && var1.id < 4)
			{
				if (var1.displayString.equals("Use"))
				{
					this.tileentCB.setOverride(var1.id, true);
					var1.displayString = "Drop";
				}
				else
				{
					this.tileentCB.setOverride(var1.id, false);
					var1.displayString = "Use";
				}
			}
			else if (var1.id == 10)
			{
				this.mc.displayGuiScreen(new GuiTogglyfierConfig(this.invPlayer));
			}
			else if (var1.id == 11)
			{
				ByteArrayOutputStream var2 = new ByteArrayOutputStream();
				DataOutputStream var3 = new DataOutputStream(var2);
				
				try
				{
					var3.writeByte(2);
					var3.writeByte(2);
					this.mc.getConnection().sendPacket(new CPacketCustomPayload("Togglyfiers",  new PacketBuffer(Unpooled.buffer().writeBytes(var2.toByteArray()))));
					var3.close();
				}
				catch (IOException var5)
				{
					var5.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		
		if (this.mc.world.isRemote)
		{
			ByteArrayOutputStream var1 = new ByteArrayOutputStream();
			DataOutputStream var2 = new DataOutputStream(var1);
			
			try
			{
				var2.writeByte(1);
				var2.writeInt(this.tileentCB.getPos().getX());
				var2.writeShort(this.tileentCB.getPos().getY());
				var2.writeInt(this.tileentCB.getPos().getZ());
				var2.writeByte((this.tileentCB.getOverride(0) ? 1 : 0) | (this.tileentCB.getOverride(1) ? 2 : 0));
				this.mc.getConnection().sendPacket(new CPacketCustomPayload("Togglyfiers",  new PacketBuffer(Unpooled.buffer().writeBytes(var1.toByteArray()))));
				var2.close();
			}
			catch (IOException var4)
			{
				var4.printStackTrace();
			}
		}
	}
	
	public void updateAlignerButtonText()
	{
		if (this.guiAlignerButton != null)
		{
			//TODO
			this.guiAlignerButton.displayString = ItemTogglyfierAssistant.doesInventoryHaveAssistant(this.invPlayer, 2) ? "Remove Aligner" : "Get Aligner";
		}
	}
}
