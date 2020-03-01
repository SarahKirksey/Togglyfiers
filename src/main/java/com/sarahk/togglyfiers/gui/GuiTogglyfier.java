package com.sarahk.togglyfiers.gui;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import com.sarahk.togglyfiers.util.Packet250CustomPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class GuiTogglyfier extends GuiContainer implements ITogglyfierGuiBase
{
	private static final ResourceLocation GUI = new ResourceLocation(Main.MOD_ID, "textures/gui/togglyfier.png");
	@Nonnull private final InventoryPlayer player;
	private final TileEntityTogglyfier tileEntity;
	private final RenderItem itemRenderer = new RenderItem(Minecraft.getMinecraft().renderEngine, new ModelManager(new TextureMap("togglyfiers")), new ItemColors());
	
	public GuiTogglyfier(@NotNull InventoryPlayer player, TileEntityTogglyfier tileEntity, boolean isReadyMode)
	{
		super(new TileEntityTogglyfier.ContainerTogglyfier(player, tileEntity, isReadyMode));
		this.player = player;
		this.tileEntity = tileEntity;
		this.isReadyMode = isReadyMode;
		//this.guiBackgroundPNG = this.isReadyMode ? "/gui/toggleblock_ready.png" : "/gui/toggleblock.png";
	}
	
	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GUI);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
	}
	
	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		String tileName = tileEntity.getDisplayName().getUnformattedText();
		this.fontRenderer.drawString(tileName, (xSize/2 - fontRenderer.getStringWidth(tileName)/2), fontRenderer.FONT_HEIGHT/2, 4210752);
		this.fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
		
		if (this.isReadyMode)
		{
			String var1 = tileEntity.capacityUsage();
			this.fontRenderer.drawString(var1, 8, 6, 4210752);
			this.fontRenderer.drawString("Container Checking", 8, 18, 4210752);
			String s = "Radius: " + this.tileEntity.getContainerCheckRadius();
			this.fontRenderer.drawString(s, 8, 32, 4210752);
			this.fontRenderer.drawString("Hidden:", 8, 50, 4210752);
			this.fontRenderer.drawString("Replenish", 119, 6, 4210752);
		}
		else
		{
			this.fontRenderer.drawString("Change Blocks", 10, 16, 4210752);
			this.fontRenderer.drawString("Quick-place", 100, 16, 4210752);
			this.fontRenderer.drawString("Off", 108, 40, 4210752);
			this.fontRenderer.drawString("On", 108, 65, 4210752);
		}
		
		this.fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
	}
	
	
	
	
	
	
	
	
	
	
	
	private boolean isReadyMode = false;
	private List<ItemStack> replenishList;
	private boolean switchModeButtonPressed = false;
	private GuiButton guiHiddenButton = null;
	private static final String labelHidden = "Yes";
	private static final String labelUnhidden = "No";
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		
		if (Main.isPlayerOp(mc.player))
		{
			this.buttonList.add(new GuiTogglyfierShortButton(10, this.guiLeft + this.xSize - 16, this.guiTop - 16, 16, 16, "+"));
		}
		
		if (this.mc.playerController.isInCreativeMode())
		{
			this.buttonList.add(new GuiTogglyfierShortButton(11, this.guiLeft, this.guiTop - 16, 70, 16, "Switch mode"));
		}
		
		if (this.isReadyMode)
		{
			this.buttonList.add(new GuiTogglyfierShortButton(0, this.guiLeft + 58, this.guiTop + 26, 16, 16, "<"));
			this.buttonList.add(new GuiTogglyfierShortButton(1, this.guiLeft + 78, this.guiTop + 26, 16, 16, ">"));
			this.guiHiddenButton = new GuiTogglyfierShortButton(2, this.guiLeft + 45, this.guiTop + 46, 30, 16, "");
			this.updateHiddenButtonText();
			this.buttonList.add(this.guiHiddenButton);
		}
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int var1, int var2, float var3)
	{
		super.drawScreen(var1, var2, var3);
		if (this.isReadyMode && this.replenishList != null)
		{
			Iterator<ItemStack> var4 = this.replenishList.iterator();
			GL11.glPushMatrix();
			GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glTranslatef((float)this.guiLeft, (float)this.guiTop, 0.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			
			for (int var5 = 0; var5 < 9; ++var5)
			{
				Slot var6 = (Slot)this.inventorySlots.inventorySlots.get(var5);
				
				if (var6.getStack().isEmpty() && var4.hasNext())
				{
					ItemStack var7 = var4.next();
					int var8 = var6.xPos;
					int var9 = var6.yPos;
					drawRect(var8, var9, var8 + 16, var9 + 16, -1073741824);
					itemRenderer.renderItemIntoGUI(var7, var8, var9);
					GL11.glDisable(GL11.GL_LIGHTING);
					GL11.glDisable(GL11.GL_DEPTH_TEST);
					String var10 = String.valueOf(var7.getCount());
					this.fontRenderer.drawStringWithShadow(var10, var8 + 17 - this.fontRenderer.getStringWidth(var10), var9 + 9, 16711680);
					int var11 = var1 - this.guiLeft;
					int var12 = var2 - this.guiTop;
					
					if (this.mc.player.inventory.getItemStack().isEmpty() && var11 >= var8 - 1 && var11 <= var8 + 16 && var12 >= var9 - 1 && var12 <= var9 + 16)
					{
						List<String> var13 = var7.getTooltip(
							mc.player,
							mc.gameSettings.advancedItemTooltips
								? ITooltipFlag.TooltipFlags.ADVANCED
								: ITooltipFlag.TooltipFlags.NORMAL);
						
						if (var13.size() > 0)
						{
							int var14 = 0;
							int var15;
							int var16;
							
							for (var15 = 0; var15 < var13.size(); ++var15)
							{
								var16 = this.fontRenderer.getStringWidth(var13.get(var15));
								
								if (var16 > var14)
								{
									var14 = var16;
								}
							}
							
							var15 = var1 - this.guiLeft + 12;
							var16 = var2 - this.guiTop - 12;
							int var18 = 8;
							
							if (var13.size() > 1)
							{
								var18 += 2 + (var13.size() - 1) * 10;
							}
							
							this.zLevel = 300.0F;
							itemRenderer.zLevel = 300.0F;
							int var19 = -267386864;
							this.drawGradientRect(var15 - 3, var16 - 4, var15 + var14 + 3, var16 - 3, var19, var19);
							this.drawGradientRect(var15 - 3, var16 + var18 + 3, var15 + var14 + 3, var16 + var18 + 4, var19, var19);
							this.drawGradientRect(var15 - 3, var16 - 3, var15 + var14 + 3, var16 + var18 + 3, var19, var19);
							this.drawGradientRect(var15 - 4, var16 - 3, var15 - 3, var16 + var18 + 3, var19, var19);
							this.drawGradientRect(var15 + var14 + 3, var16 - 3, var15 + var14 + 4, var16 + var18 + 3, var19, var19);
							int var20 = 1347420415;
							int var21 = (var20 & 16711422) >> 1 | var20 & -16777216;
							this.drawGradientRect(var15 - 3, var16 - 3 + 1, var15 - 3 + 1, var16 + var18 + 3 - 1, var20, var21);
							this.drawGradientRect(var15 + var14 + 2, var16 - 3 + 1, var15 + var14 + 3, var16 + var18 + 3 - 1, var20, var21);
							this.drawGradientRect(var15 - 3, var16 - 3, var15 + var14 + 3, var16 - 3 + 1, var20, var20);
							this.drawGradientRect(var15 - 3, var16 + var18 + 2, var15 + var14 + 3, var16 + var18 + 3, var21, var21);
							
							for (int var22 = 0; var22 < var13.size(); ++var22)
							{
								String var23 = (String)var13.get(var22);
								
								if (var22 == 0)
								{
									var23 = "\u00a7" + Integer.toHexString(var7.getRarity().rarityColor.getColorIndex()) + var23;
								}
								else
								{
									var23 = "\u00a77" + var23;
								}
								
								this.fontRenderer.drawStringWithShadow(var23, var15, var16, -1);
								
								if (var22 == 0)
								{
									var16 += 2;
								}
								
								var16 += 10;
							}
							
							this.zLevel = 0.0F;
							itemRenderer.zLevel = 0.0F;
						}
					}
					
					GL11.glEnable(GL11.GL_LIGHTING);
					GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
			
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glPopMatrix();
		}
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton var1)
	{
		if (var1.enabled)
		{
			if (var1.id == 0 && this.tileEntity.getContainerCheckRadius() > 0)
			{
				tileEntity.setContainerCheckRadius((byte) (tileEntity.getContainerCheckRadius()-1));
				
				if (!this.mc.world.isRemote)
				{
					this.tileEntity.updateContainerRegistry();
				}
			}
			else if (var1.id == 1 && this.tileEntity.getContainerCheckRadius() < 5)
			{
				tileEntity.setContainerCheckRadius((byte) (tileEntity.getContainerCheckRadius()+1));
				
				if (!this.mc.world.isRemote)
				{
					this.tileEntity.updateContainerRegistry();
				}
			}
			else if (var1.id == 2)
			{
				ByteArrayOutputStream var2 = new ByteArrayOutputStream();
				DataOutputStream var3 = new DataOutputStream(var2);
				
				try
				{
					var3.writeByte(2);
					var3.writeByte(0);
					this.mc.getConnection().getNetworkManager().sendPacket(new Packet250CustomPayload("Togglyfiers", var2.toByteArray()));
					var3.close();
				}
				catch (IOException var5)
				{
					var5.printStackTrace();
				}
			}
			else if (var1.id == 10)
			{
				this.mc.displayGuiScreen(new GuiTogglyfierConfig(this.player));
			}
			else if (var1.id == 11)
			{
				this.switchModeButtonPressed = true;
				this.mc.displayGuiScreen(null);
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
			
			if (this.isReadyMode)
			{
				try
				{
					var2.writeByte(0);
					var2.writeInt(this.tileEntity.getPos().getX());
					var2.writeShort(this.tileEntity.getPos().getY());
					var2.writeInt(this.tileEntity.getPos().getZ());
					var2.writeByte(this.tileEntity.getContainerCheckRadius());
					this.mc.getConnection().getNetworkManager().sendPacket((new Packet250CustomPayload("Togglyfiers", var1.toByteArray())));
					var2.close();
				}
				catch (IOException var5)
				{
					var5.printStackTrace();
				}
			}
			
			if (this.switchModeButtonPressed)
			{
				try
				{
					var1 = new ByteArrayOutputStream();
					var2 = new DataOutputStream(var1);
					var2.writeByte(5);
					var2.writeInt(this.tileEntity.getPos().getX());
					var2.writeShort(this.tileEntity.getPos().getY());
					var2.writeInt(this.tileEntity.getPos().getZ());
					this.mc.getConnection().getNetworkManager().sendPacket((new Packet250CustomPayload("Togglyfiers", var1.toByteArray())));
					var2.close();
				}
				catch (IOException var4)
				{
					var4.printStackTrace();
				}
			}
		}
	}
	
	public void updateReplenishList(List var1)
	{
		this.replenishList = var1;
	}
	
	public void updateHiddenButtonText()
	{
		if (this.guiHiddenButton != null)
		{
			this.guiHiddenButton.displayString = this.tileEntity.getHiddenBlock() != null ? "Yes" : "No";
		}
	}
	
	public TileEntityTogglyfier getTogglyfier()
	{
		return this.tileEntity;
	}
}
