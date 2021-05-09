package com.sarahk.togglyfiers.gui;

import com.sarahk.togglyfiers.tileentity.TileEntityTogglyfier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public interface ITogglyfierGuiBase
{
	void initGui();
	TileEntityTogglyfier getTogglyfier();
	void onGuiClosed();
	void drawScreen(int winX, int winY, float winZ);
}

class GuiTogglyfierShortButton extends GuiButton
{
	private int halfWidth = 100;
	private int halfHeight = 10;
	
	public GuiTogglyfierShortButton(int var1, int var2, int var3, String var4)
	{
		super(var1, var2, var3, var4);
		this.halfWidth = this.width / 2;
		this.halfHeight = this.height / 2;
	}
	
	public GuiTogglyfierShortButton(int var1, int var2, int var3, int var4, int var5, String var6)
	{
		super(var1, var2, var3, var4, var5, var6);
		this.halfWidth = this.width / 2;
		this.halfHeight = this.height / 2;
	}
	
	/**
	 * Draws this button to the screen.
	 */
	@Override
	public void drawButton(@NotNull Minecraft mc, int mouseX, int mouseY, float partialTicks)
	{
		super.drawButton(mc, mouseX, mouseY, partialTicks);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, mc.renderEngine.getTexture(new ResourceLocation("/gui/gui.png")).getGlTextureId());
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		boolean var4 = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		int var5 = this.getHoverState(var4);
		this.drawTexturedModalRect(x, y, 0, 46 + var5 * 20, this.halfWidth, this.halfHeight);
		this.drawTexturedModalRect(x + this.halfWidth, y, 200 - this.halfWidth, 46 + var5 * 20, this.halfWidth, this.halfHeight);
		this.drawTexturedModalRect(x, y + this.halfHeight, 0, 66 + var5 * 20 - this.halfHeight, this.halfWidth, this.halfHeight);
		this.drawTexturedModalRect(x + this.halfWidth, y + this.halfHeight, 200 - this.halfWidth, 66 + var5 * 20 - this.halfHeight, this.halfWidth, this.halfHeight);
		int var6 = 14737632;
		
		if (!this.enabled)
		{
			var6 = -6250336;
		}
		else if (var4)
		{
			var6 = 16777120;
		}
		
		assert mc.fontRenderer!=null;
		assert this.displayString!=null;
		this.drawCenteredString(mc.fontRenderer, this.displayString, x + this.halfWidth, y + (this.height - 8) / 2, var6);
	}
}