package com.sarahk.togglyfiers.gui;

import com.sarahk.togglyfiers.Main;
import com.sarahk.togglyfiers.blocks.BlockTogglyfier;
import com.sarahk.togglyfiers.network.TogglyfierNetworkHandler;
import com.sarahk.togglyfiers.network.TogglyfierPacket;
import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;

public class GuiTogglyfier extends GuiContainer implements ITogglyfierGuiBase
{
	private static final ResourceLocation GUI_BACKGROUND = new ResourceLocation(Main.MOD_ID, "textures/gui/togglyfier.png");
	public static final int SLOT_SIZE = 16;
	public static final float FULL_CIRCLE = 360.0F;
	public static final int MODE_SWITCH_BUTTON_INDEX = 11;
	@NotNull private final InventoryPlayer player;
	private final TileEntityTogglyfier tileEntity;
	
	private static final String labelHidden = "Yes";
	private static final String labelUnhidden = "No";
	public static final int P_DRAW_STRING_4_ = 0x404040;
	
	public GuiTogglyfier(Container inventorySlotsIn, @NotNull InventoryPlayer player, TileEntityTogglyfier tileEntity, boolean isReadyMode)
	{
		super(inventorySlotsIn);
		this.player = player;
		this.tileEntity = tileEntity;
		this.isReadyMode = isReadyMode;
		//this.guiBackgroundPNG = this.isReadyMode ? "/gui/toggleblock_ready.png" : "/gui/toggleblock.png";
	}
	
	public GuiTogglyfier(final EntityPlayer player, final TileEntityTogglyfier te, final boolean ready)
	{
		this(te.getContainer(), player.inventory, te, ready);
	}
	
	/**
	 * Draw the background layer for the GuiContainer (everything behind the items)
	 */
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
	{
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GUI_BACKGROUND);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
	}
	
	/**
	 * Draw the foreground layer for the GuiContainer (everything in front of the items)
	 */
	@SuppressWarnings("FeatureEnvy")
	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		String tileName = tileEntity.getDisplayName().getUnformattedText();
		this.fontRenderer.drawString(tileName, (xSize/2 - fontRenderer.getStringWidth(tileName)/2), fontRenderer.FONT_HEIGHT/2, P_DRAW_STRING_4_);
		this.fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, P_DRAW_STRING_4_);
		
		if(this.isReadyMode)
		{
			String var1 = tileEntity.capacityUsage();
			this.fontRenderer.drawString(var1, 8, 6, P_DRAW_STRING_4_);
			this.fontRenderer.drawString("Container Checking", 8, 18, P_DRAW_STRING_4_);
			String s = "Radius: " + this.tileEntity.getContainerCheckRadius();
			this.fontRenderer.drawString(s, 8, 32, P_DRAW_STRING_4_);
			this.fontRenderer.drawString("Hidden:", 8, 50, P_DRAW_STRING_4_);
			this.fontRenderer.drawString("Replenish", 119, 6, P_DRAW_STRING_4_);
		}
		else
		{
			this.fontRenderer.drawString("Change Blocks", 10, 16, P_DRAW_STRING_4_);
			this.fontRenderer.drawString("Quick-place", 100, 16, P_DRAW_STRING_4_);
			this.fontRenderer.drawString("Off", 108, 40, P_DRAW_STRING_4_);
			this.fontRenderer.drawString("On", 108, 65, P_DRAW_STRING_4_);
		}
		
		this.fontRenderer.drawString(player.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, P_DRAW_STRING_4_);
	}
	
	
	private boolean isReadyMode = false;
	private List<ItemStack> replenishList;
	private boolean switchModeButtonPressed = false;
	private final GuiButton guiHiddenButton = new GuiTogglyfierShortButton(2, this.guiLeft + 45, this.guiTop + 46, 30, 16, "");
	
	
	private final GuiButton modeSwitchButton = new GuiTogglyfierShortButton(11, this.guiLeft, this.guiTop - 16, 70, 16, "Switch mode");
	private final GuiButton opOnlyButton = new GuiTogglyfierShortButton(10, this.guiLeft + this.xSize - 16, this.guiTop - 16, 16, 16, "+");
	
	private final GuiButton decrContainerRadiusButton = new GuiTogglyfierShortButton(0, this.guiLeft + 58, this.guiTop + 26, 16, 16, "<");
	private final GuiButton incrContainerRadiusButton = new GuiTogglyfierShortButton(1, this.guiLeft + 78, this.guiTop + 26, 16, 16, ">");
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui()
	{
		super.initGui();
		this.buttonList.clear();
		
		if(Main.isPlayerOp(mc.player))
		{
			this.buttonList.add(opOnlyButton);
		}
		
		if(this.mc.playerController.isInCreativeMode())
		{
			this.buttonList.add(new GuiTogglyfierShortButton(MODE_SWITCH_BUTTON_INDEX, this.guiLeft, this.guiTop - 16, 70, 16, "Switch mode"));
		}
		
		if(this.isReadyMode)
		{
			this.updateHiddenButtonText();
			this.buttonList.add(decrContainerRadiusButton);
			this.buttonList.add(incrContainerRadiusButton);
			this.buttonList.add(guiHiddenButton);
		}
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int winX, int winY, float winZ)
	{
		super.drawScreen(winX, winY, winZ);
		
		if(this.replenishList == null || !this.isReadyMode)
		{
			return;
		}
		
		Iterator<ItemStack> var4 = this.replenishList.iterator();
		GL11.glPushMatrix();
		GL11.glRotatef(FULL_CIRCLE/3.0F, 1.0F, 0.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GL11.glPopMatrix();
		GL11.glPushMatrix();
		GL11.glTranslatef((float) this.guiLeft, (float) this.guiTop, 0.0F);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		
		for(int var5 = 0; var5<9; ++var5)
		{
			Slot slot = this.inventorySlots.inventorySlots.get(var5);
			
			if(!slot.getStack().isEmpty() || !var4.hasNext())
			{
				continue;
			}
			
			ItemStack var7 = var4.next();
			int slotX = slot.xPos;
			int slotY = slot.yPos;
			drawRect(slotX, slotY, slotX + SLOT_SIZE, slotY + SLOT_SIZE, -1073741824);
			itemRender.renderItemIntoGUI(var7, slotX, slotY);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			String var10 = String.valueOf(var7.getCount());
			this.fontRenderer.drawStringWithShadow(var10, slotX + 17 - this.fontRenderer.getStringWidth(var10), slotY + 9, 16711680);
			int var11 = winX - this.guiLeft;
			int var12 = winY - this.guiTop;
			
			if(this.mc.player.inventory.getItemStack().isEmpty() && var11 >= slotX - 1 && var11<=slotX + SLOT_SIZE && var12 >= slotY - 1 && var12<=slotY + SLOT_SIZE)
			{
				List<String> tooltipLines = var7.getTooltip(
					mc.player,
					mc.gameSettings.advancedItemTooltips
						? ITooltipFlag.TooltipFlags.ADVANCED
						: ITooltipFlag.TooltipFlags.NORMAL);
				
				if(!tooltipLines.isEmpty())
				{
					int maxStringWidth = 0;
					
					for(String s : tooltipLines)
					{
						int stringWidth = this.fontRenderer.getStringWidth(s);
						
						if(stringWidth>maxStringWidth)
						{
							maxStringWidth = stringWidth;
						}
					}
					
					int i = winX - this.guiLeft + 12;
					int var16 = winY - this.guiTop - 12;
					int var18 = 8;
					
					if(tooltipLines.size()>1)
					{
						var18 += 2 + (tooltipLines.size() - 1)*10;
					}
					
					this.zLevel = 300.0F;
					itemRender.zLevel = 300.0F;
					int var19 = -267386864;
					this.drawGradientRect(i - 3, var16 - 4, i + maxStringWidth + 3, var16 - 3, var19, var19);
					this.drawGradientRect(i - 3, var16 + var18 + 3, i + maxStringWidth + 3, var16 + var18 + 4, var19, var19);
					this.drawGradientRect(i - 3, var16 - 3, i + maxStringWidth + 3, var16 + var18 + 3, var19, var19);
					this.drawGradientRect(i - 4, var16 - 3, i - 3, var16 + var18 + 3, var19, var19);
					this.drawGradientRect(i + maxStringWidth + 3, var16 - 3, i + maxStringWidth + 4, var16 + var18 + 3, var19, var19);
					int var20 = 1347420415;
					int var21 = (var20 & 16711422) >> 1 | var20 & -16777216;
					this.drawGradientRect(i - 3, var16 - 3 + 1, i - 3 + 1, var16 + var18 + 3 - 1, var20, var21);
					this.drawGradientRect(i + maxStringWidth + 2, var16 - 3 + 1, i + maxStringWidth + 3, var16 + var18 + 3 - 1, var20, var21);
					this.drawGradientRect(i - 3, var16 - 3, i + maxStringWidth + 3, var16 - 3 + 1, var20, var20);
					this.drawGradientRect(i - 3, var16 + var18 + 2, i + maxStringWidth + 3, var16 + var18 + 3, var21, var21);
					
					for(String var23 : tooltipLines)
					{
						int var22 = tooltipLines.indexOf(var23);
						
						var23 = var22==0 ? "\u00a7" + Integer.toHexString(var7.getRarity().color.getColorIndex()) + var23 : "\u00a77" + var23;	//keyword
						
						this.fontRenderer.drawStringWithShadow(var23, i, var16, -1);
						
						if(var22==0)
						{
							var16 += 2;
						}
						
						var16 += 10;
					}
					
					this.zLevel = 0.0F;
					itemRender.zLevel = 0.0F;
				}
			}
			
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		RenderHelper.disableStandardItemLighting();
		GL11.glPopMatrix();
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		if(!guiButton.enabled)
		{
			return;
		}
		
		TogglyfierPacket packet = null;
		
		switch(guiButton.id)
		{
		case 0:
		case 1:
			if(this.tileEntity.getContainerCheckRadius()>0)
			{
				tileEntity.setContainerCheckRadius((byte) ((tileEntity.getContainerCheckRadius() + 2*guiButton.id-1) % 5));
				
				if(!this.mc.world.isRemote)
				{
					this.tileEntity.updateContainerRegistry();
				}
			}
			break;
		case 2:
			OutputStream var3 = new DataOutputStream(new ByteArrayOutputStream());
			
			try
			{
				var3.write(new byte[] {2, 0});
				packet = new TogglyfierPacket.TogglyfierSwitchModePacket("Togglyfiers", new byte[] {2, 0});
				var3.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
			break;
		case 10:
			this.mc.displayGuiScreen(new GuiTogglyfierConfig(this.player));
			break;
		case MODE_SWITCH_BUTTON_INDEX:
			if(guiButton==modeSwitchButton)
			{
				if(tileEntity.isReady())
				{
					packet = new TogglyfierPacket.TogglyfierSwitchModePacket("Togglyfiers", BlockTogglyfier.EnumTogglyfierMode.EDIT);
					guiButton.displayString = I18n.format("gui.switchToReady");
				}
				else
				{
					packet = new TogglyfierPacket.TogglyfierSwitchModePacket("Togglyfiers", BlockTogglyfier.EnumTogglyfierMode.MODE_READY);
					guiButton.displayString = I18n.format("gui.switchToEdit");
				}
				tileEntity.switchMode();
			}
			this.switchModeButtonPressed = true;
			this.mc.displayGuiScreen(null);
			break;
		}
		
		if(packet != null)
		{
			TogglyfierNetworkHandler.sendToServer(packet);
		}
	}
	
	/**
	 * Called when the screen is unloaded. Used to disable keyboard repeat events
	 */
	@Override
	public void onGuiClosed()
	{
		super.onGuiClosed();
		
		if(this.mc.world.isRemote)
		{
			ByteArrayOutputStream var1 = new ByteArrayOutputStream();
			DataOutputStream var2 = new DataOutputStream(var1);
			
			if(this.isReadyMode)
			{
				try
				{
					var2.writeByte(0);
					var2.writeInt(this.tileEntity.getPos().getX());
					var2.writeShort(this.tileEntity.getPos().getY());
					var2.writeInt(this.tileEntity.getPos().getZ());
					var2.writeByte(this.tileEntity.getContainerCheckRadius());
					
					TogglyfierNetworkHandler.sendToServer((new TogglyfierPacket.TogglyfierGuiClosed("Togglyfiers", var1.toByteArray())));
					var2.close();
				}
				catch(IOException var5)
				{
					var5.printStackTrace();
				}
			}
			
			if(this.switchModeButtonPressed)
			{
				try
				{
					var1 = new ByteArrayOutputStream();
					var2 = new DataOutputStream(var1);
					var2.writeByte(5);
					var2.writeInt(this.tileEntity.getPos().getX());
					var2.writeShort(this.tileEntity.getPos().getY());
					var2.writeInt(this.tileEntity.getPos().getZ());
					TogglyfierNetworkHandler.sendToServer((new TogglyfierPacket.TogglyfierGuiClosed("Togglyfiers", var1.toByteArray())));
					var2.close();
				}
				catch(IOException var4)
				{
					var4.printStackTrace();
				}
			}
		}
	}
	
	public void updateReplenishList(List<ItemStack> var1) { replenishList = var1; }
	
	private void updateHiddenButtonText()
	{
		this.guiHiddenButton.displayString = tileEntity.getHiddenBlock()!=null ? labelHidden : labelUnhidden;
	}
	
	@Override
	public TileEntityTogglyfier getTogglyfier()
	{
		return this.tileEntity;
	}
}
