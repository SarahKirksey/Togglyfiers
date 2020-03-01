package com.sarahk.togglyfiers.gui;

import com.sarahk.togglyfiers.ModConfig;
import com.sarahk.togglyfiers.blocks.ModBlocks;
import com.sarahk.togglyfiers.items.ModItems;
import com.sarahk.togglyfiers.util.MutableItemStack;
import com.sarahk.togglyfiers.util.Packet250CustomPayload;
import com.sarahk.togglyfiers.util.TogglyfierItemInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static net.minecraft.init.Blocks.STONE;

public class GuiTogglyfierConfig extends GuiScreen implements ITogglyfierGuiBase
{
	private static List<TogglyfierItemInfo> itemIDList = new ArrayList<>();
	private static List<Item> itemIDExcludeList = new ArrayList<>();
	public static final int EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON = 18;
	private List<GuiButton> itemFlagButtons = new ArrayList<>();
	private List<GuiButton> priorityButtons = new ArrayList<>();
	private GuiTogglyfierConfig.GuiItemPanel itemPanel;
	private GuiButton guiPrevButton;
	private GuiButton guiSaveButton;
	private GuiButton guiScannerButton;
	private List<Item> currentItemExpectedBlocks = new ArrayList<>();
	private int currentItemPriority;
	private boolean settingExpectedBlocks = false;
	private IInventory invPlayer;
	private ItemStack itemStringStack = new ItemStack(Items.AIR, 1, 0);
	private static final String labelEmptyBlocksEntry = "None";
	private static final String labelSetToHaveNoBlocks = "Set to None!";
	private static final String labelSave = "Save";
	private static final String labelSaved = "Saved!";
	private static final String labelGetScanner = "Get Scanner";
	private static final String labelRemoveScanner = "Remove Scanner";
	private static final String labelSetExpectedBlocks = "Add Blocks";
	private static final String labelClickWhenDone = "Click When Done";
	private static final int minimumButtonCount = 6;
	private static final RenderItem itemRenderer = new RenderItem(Minecraft.getMinecraft().renderEngine, new ModelManager(new TextureMap("togglyfiers")), new ItemColors());
	
	public GuiTogglyfierConfig(IInventory var1)
	{
		this.invPlayer = var1;
	}
	
	/**
	 * Adds the buttons (and other controls) to the screen in question.
	 */
	@Override
	public void initGui()
	{
		this.itemPanel = new GuiTogglyfierConfig.GuiItemPanel(
			this,
			2,
			25,
			(this.width - 4) / 3,
			this.height - 50,
			this.itemPanel != null
				? this.itemPanel.getCurrentItemInfo()
				: new TogglyfierItemInfo(STONE, -1));
		
		this.buttonList.clear();
		this.itemFlagButtons.clear();
		this.priorityButtons.clear();
		this.guiPrevButton = new GuiButton(0, 0, 0, this.width / 3, 20, "");
		this.buttonList.add(this.guiPrevButton);
		this.setPrevButtonText();
		this.buttonList.add(new GuiButton(1, 0, this.height - 20, this.width / 3, 20, "Next"));
		int var1 = this.width / 3 + 8;
		int var2 = this.width / 6;
		this.guiSaveButton = new GuiButton(2, var1, this.height - 30, var2 - 2, 20, "Save");
		this.buttonList.add(this.guiSaveButton);
		this.buttonList.add(new GuiButton(3, var1 + var2, this.height - 30, var2 - 2, 20, "Exit"));
		this.guiScannerButton = new GuiButton(4, this.width - 110, this.height - 30, 100, 20, "");
		this.buttonList.add(this.guiScannerButton);
		this.updateScannerButtonText();
		this.buttonList.add(new GuiButton(5, this.width - 110, 25, 100, 20, "Add Blocks"));
		ModConfig.ItemFlags[] var3 = ModConfig.ItemFlags.values();
		
		for(ModConfig.ItemFlags var6 : var3)
		{
			this.itemFlagButtons.add(
				var6.getName().equals("save")
					? new GuiCheckButton(this, var1 + 30 + var6.ordinal()*30, 55, var6.ordinal(), var6.getDisplayName())
					: new GuiCheckButton(this, var1, 7 + 16*var6.ordinal(), var6.ordinal(), var6.getDisplayName()));
		}
		
		for (int var7 = -1; var7 <= 3; ++var7)
		{
			this.priorityButtons.add(new GuiCheckButton(this, var1 + (var7 + 1)*30, this.height - 50, var7, String.valueOf(var7)));
		}
		
		this.loadNextItem();
	}
	
	/**
	 * Draws the screen and all the components in it.
	 */
	@Override
	public void drawScreen(int var1, int var2, float var3)
	{
		this.drawDefaultBackground();
		super.drawScreen(var1, var2, var3);
		int var4 = this.width / 2 + 8;
		int var5 = this.width / 3 + 8;
		this.drawCenteredString(this.fontRenderer, "Toggle Blocks Configuration", var4, 10, 16777215);
		this.drawString(this.fontRenderer, "Selected: " + this.getItemString(this.itemPanel.getCurrentItemStack()), var5, 25, 16777215);
		this.drawCenteredString(this.fontRenderer, "Item Flags:", var4, 40, 16777215);
		this.drawString(this.fontRenderer, "Save:", var5, 55, 16777215);
		this.drawCenteredString(this.fontRenderer, "Expected Blocks", this.width - 60, 10, 16777215);
		this.drawCenteredString(this.fontRenderer, "Priority:", var4, this.height - 65, 16777215);
		int var6;
		
		for (var6 = 0; var6 < this.itemFlagButtons.size(); ++var6)
		{
			((GuiTogglyfierConfig.GuiCheckButton)this.itemFlagButtons.get(var6)).drawButton();
		}
		
		for (var6 = 0; var6 < this.priorityButtons.size(); ++var6)
		{
			((GuiTogglyfierConfig.GuiCheckButton)this.priorityButtons.get(var6)).drawButton();
		}
		
		this.itemPanel.drawPanel(var1, var2);
	}
	
	/**
	 * Called when the mouse is clicked.
	 */
	@Override
	protected void mouseClicked(int var1, int var2, int var3) throws IOException
	{
		super.mouseClicked(var1, var2, var3);
		
		if (var3 == 0)
		{
			int var4;
			GuiTogglyfierConfig.GuiCheckButton var5;
			
			for (var4 = 0; var4 < this.itemFlagButtons.size(); ++var4)
			{
				var5 = (GuiTogglyfierConfig.GuiCheckButton)this.itemFlagButtons.get(var4);
				
				if (var5.mousePressed(mc, var1, var2))
				{
					//var5.switchState();
					//this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
					this.resetSaveButtonText();
					return;
				}
			}
			
			for (var4 = 0; var4 < this.priorityButtons.size(); ++var4)
			{
				var5 = (GuiTogglyfierConfig.GuiCheckButton)this.priorityButtons.get(var4);
				
				if (var5.mousePressed(mc, var1, var2) && !var5.buttonState)
				{
					var5.buttonState = true;
					this.currentItemPriority = var5.id;
					//this.mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
					
					for (int var6 = 0; var6 < this.priorityButtons.size(); ++var6)
					{
						GuiTogglyfierConfig.GuiCheckButton var7 = (GuiTogglyfierConfig.GuiCheckButton)this.priorityButtons.get(var6);
						
						if (var7.id != var5.id)
						{
							var7.buttonState = false;
						}
					}
					
					this.resetSaveButtonText();
					return;
				}
			}
		}
		
		if (this.itemPanel.mouseClicked(var1, var2, !this.settingExpectedBlocks))
		{
			if (this.settingExpectedBlocks)
			{
				this.addExpectedBlock();
			}
			else
			{
				this.loadNextItem();
				this.resetSaveButtonText();
			}
		}
	}
	
	/**
	 * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
	 */
	@Override
	protected void actionPerformed(GuiButton var1)
	{
		if (var1.id == 0)
		{
			this.itemPanel.decrementPage();
			this.setPrevButtonText();
		}
		else if (var1.id == 1)
		{
			this.itemPanel.incrementPage();
			this.setPrevButtonText();
		}
		else if (var1.id == 2)
		{
			if (var1.displayString.equals("Save"))
			{
				this.saveCurrentItem();
				((GuiButton)this.buttonList.get(5)).displayString = "Add Blocks";
				this.settingExpectedBlocks = false;
			}
		}
		else if (var1.id == 3)
		{
			this.mc.displayGuiScreen((GuiScreen)null);
		}
		else if (var1.id == 4)
		{
			ByteArrayOutputStream var2 = new ByteArrayOutputStream();
			DataOutputStream var3 = new DataOutputStream(var2);
			
			try
			{
				var3.writeByte(2);
				var3.writeByte(1);
				this.mc.getConnection().sendPacket(new Packet250CustomPayload("Togglyfiers", var2.toByteArray()));
				var3.close();
			}
			catch (IOException var5)
			{
				var5.printStackTrace();
			}
		}
		else if (var1.id == 5)
		{
			this.settingExpectedBlocks = var1.displayString.equals("Add Blocks");
			var1.displayString = this.settingExpectedBlocks ? "Click When Done" : "Add Blocks";
		}
		else
		{
			if (var1.id == 6)
			{
				if (var1.displayString.equals("None"))
				{
					var1.displayString = "Set to None!";
					this.currentItemExpectedBlocks.clear();
					this.currentItemExpectedBlocks.add(Items.AIR);
					return;
				}
				
				if (var1.displayString.equals("Set to None!"))
				{
					var1.displayString = "None";
					this.currentItemExpectedBlocks.clear();
					return;
				}
			}
			
			this.currentItemExpectedBlocks.remove(var1.id - 6);
			this.buttonList.remove(var1);
			
			for (int var6 = var1.id; var6 < this.buttonList.size(); ++var6)
			{
				GuiButton var7 = (GuiButton)this.buttonList.get(var6);
				--var7.id;
				var7.y -= 25;
			}
			
			if (this.buttonList.size() == 6)
			{
				this.buttonList.add(new GuiButton(6, this.width - 110, 50, 100, 20, "None"));
			}
		}
	}
	
	private void resetSaveButtonText()
	{
		this.guiSaveButton.displayString = "Save";
	}
	
	public void setSaveButtonTextToSaved()
	{
		this.guiSaveButton.displayString = "Saved!";
	}
	
	private void addExpectedBlock()
	{
		Item var1 = this.itemPanel.getLastClickedID();
		
		if (this.currentItemExpectedBlocks.size() < 5 && var1 instanceof ItemBlock)
		{
			String var2 = this.getItemString(var1);
			
			if (this.currentItemExpectedBlocks.size() == 1 && (this.currentItemExpectedBlocks.get(0)) == Items.AIR)
			{
				this.currentItemExpectedBlocks.clear();
			}
			
			this.currentItemExpectedBlocks.add(var1);
			
			if (this.currentItemExpectedBlocks.size() == 1)
			{
				GuiButton var3 = (GuiButton)this.buttonList.get(6);
				var3.displayString = var2;
			}
			else
			{
				int var4 = this.currentItemExpectedBlocks.size() - 1;
				this.buttonList.add(new GuiButton(var4 + 6, this.width - 110, 50 + var4 * 25, 100, 20, var2));
			}
		}
		else
		{
			//this.mc.getSoundHandler().playSound("random.glass");
		}
	}
	
	private void addExpectedBlockButtons()
	{
		//noinspection MethodCallInLoopCondition
		while (this.buttonList.size() > 6)
		{
			this.buttonList.remove(6);
		}
		
		if (this.currentItemExpectedBlocks.isEmpty())
		{
			this.buttonList.add(new GuiButton(6, this.width - 110, 50, 100, 20, "None"));
		}
		else if (this.currentItemExpectedBlocks.size() == 1 && this.currentItemExpectedBlocks.get(0)==Items.AIR)
		{
			this.buttonList.add(new GuiButton(6, this.width - 110, 50, 100, 20, "Set to None!"));
		}
		else
		{
			for (int var1 = 0; var1 < this.currentItemExpectedBlocks.size(); ++var1)
			{
				Item var2 = this.currentItemExpectedBlocks.get(var1);
				this.buttonList.add(new GuiButton(var1 + 6, this.width - 110, 50 + var1 * 25, 100, 20, this.getItemString(var2)));
			}
		}
	}
	
	private String getItemString(Item var1)
	{
		ItemStack var2 = new ItemStack(var1, 0, 0);
		String var3 = var2.getItem().getUnlocalizedName(var2);
		var3 = var3.isEmpty() ? var1 + " Unnamed" : var1 + " " + var3;
		return var3;
	}
	
	private String getItemString(ItemStack var1)
	{
		this.itemStringStack = new ItemStack(var1.getItem(), var1.getCount(), var1.getItemDamage());
		this.itemStringStack.setItemDamage(var1.getItemDamage());
		StringBuilder var2 = new StringBuilder();
		var2.append(this.itemStringStack.getItem());
		
		if (this.itemStringStack.getItem().getHasSubtypes())
		{
			var2.append('-');
			
			if (this.itemStringStack.getItemDamage() >= 0)
			{
				var2.append(this.itemStringStack.getItemDamage());
			}
			else
			{
				var2.append("All");
				this.itemStringStack.setItemDamage(0);
			}
		}
		
		var2.append(' ');
		String var3 = this.itemStringStack.getItem().getUnlocalizedName(this.itemStringStack);
		
		if (var3.length() > 0)
		{
			var2.append(var3);
		}
		else
		{
			var2.append("Unnamed");
		}
		
		return var2.toString();
	}
	
	private void saveCurrentItem()
	{
		int var1 = 0;
		TogglyfierItemInfo var2 = this.itemPanel.getCurrentItemInfo();
		
		for (int var3 = 0; var3 < this.itemFlagButtons.size(); ++var3)
		{
			GuiTogglyfierConfig.GuiCheckButton var4 = (GuiTogglyfierConfig.GuiCheckButton)this.itemFlagButtons.get(var3);
			var1 |= var4.buttonState ? 1 << var4.id : 0;
		}
		
		ByteArrayOutputStream var7 = new ByteArrayOutputStream();
		DataOutputStream var8 = new DataOutputStream(var7);
		
		try
		{
			var8.writeByte(3);
			var2.writeToStream(var8);
			var8.writeByte(this.currentItemExpectedBlocks.size());
			
			for (int var5 = 0; var5 < this.currentItemExpectedBlocks.size(); ++var5)
			{
				var8.writeBytes((this.currentItemExpectedBlocks.get(var5).getRegistryName().toString()));
			}
			
			var8.writeInt(var1);
			var8.writeByte(this.currentItemPriority);
			this.mc.getConnection().sendPacket(new Packet250CustomPayload("Togglyfiers", var7.toByteArray()));
			var8.close();
		}
		catch (IOException var6)
		{
			var6.printStackTrace();
		}
	}
	
	private void loadNextItem()
	{
		ByteArrayOutputStream var1 = new ByteArrayOutputStream();
		DataOutputStream var2 = new DataOutputStream(var1);
		
		try
		{
			var2.writeByte(4);
			this.itemPanel.getCurrentItemInfo().writeToStream(var2);
			this.mc.getConnection().sendPacket(new Packet250CustomPayload("Togglyfiers", var1.toByteArray()));
			var2.close();
		}
		catch (IOException var4)
		{
			var4.printStackTrace();
		}
	}
	
	public void setItemProperties(Item[] var1, int var2, int var3)
	{
		this.currentItemExpectedBlocks.clear();
		int var4;
		
		for (var4 = 0; var4 < var1.length; ++var4)
		{
			this.currentItemExpectedBlocks.add(var1[var4]);
		}
		
		this.currentItemPriority = var3;
		this.addExpectedBlockButtons();
		GuiTogglyfierConfig.GuiCheckButton var5;
		
		for (var4 = 0; var4 < this.itemFlagButtons.size(); ++var4)
		{
			var5 = (GuiCheckButton)this.itemFlagButtons.get(var4);
			var5.buttonState = (var2 & 1 << var5.id) > 0;
		}
		
		for (var4 = 0; var4 < this.priorityButtons.size(); ++var4)
		{
			var5 = (GuiCheckButton)this.priorityButtons.get(var4);
			var5.buttonState = var5.id == this.currentItemPriority;
		}
	}
	
	private void setPrevButtonText()
	{
		this.guiPrevButton.displayString = "Prev (Page " + (this.itemPanel.getCurrentPage() + 1) + "/" + this.itemPanel.getNumberOfPages() + ")";
	}
	
	public void updateScannerButtonText()
	{
		//this.guiScannerButton.displayString = ItemTogglyfierAssistant.inventoryHasAssistant(this.invPlayer, 1) ? "Remove Scanner" : "Get Scanner";
	}
	
	/**
	 * Returns true if this GUI should pause the game when it is displayed in single-player
	 */
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
	
	public static void populateItemList()
	{
		itemIDList.clear();
		Collection<String> var1 = new HashSet<>();
		MutableItemStack var2 = new MutableItemStack(Items.AIR, 0, 0);
		
		for (Item var3 : ForgeRegistries.ITEMS)
		{
			if (var3 != null && !itemIDExcludeList.contains(var3))
			{
				if (var3.getHasSubtypes())
				{
					var1.clear();
					var2.setItem(var3);
					var2.setCount(var3.getItemStackLimit());
					
					for (int var4 = 0; var4 <= 15; ++var4)
					{
						var2.setDamage(var4);
						
						try
						{
							String var6 = var2.toItemStack().getItem().getRegistryName().toString();
							
							if (!var1.contains(var6))
							{
								var1.add(var6);
								itemIDList.add(new TogglyfierItemInfo(var3, var4));
							}
						}
						catch (NullPointerException | IndexOutOfBoundsException var7)
						{
							System.out.println(var7.getMessage());
							var7.printStackTrace();
						}
					}
				}
				else
				{
					itemIDList.add(new TogglyfierItemInfo(var3, 0));
				}
			}
		}
	}
	
	private static void addExcludedItem(Item item)
	{
		itemIDExcludeList.add(item);
	}
	
	static List access$000()
	{
		return itemIDList;
	}
	
	static RenderItem access$100()
	{
		return itemRenderer;
	}
	
	static String access$200(GuiTogglyfierConfig var0, ItemStack var1)
	{
		return var0.getItemString(var1);
	}
	
	static
	{
		addExcludedItem(Item.getItemFromBlock(ModBlocks.togglyfier));
		addExcludedItem(Item.getItemFromBlock(ModBlocks.changeBlock));
		addExcludedItem(ModItems.togglyfierAssistant);
	}
	
	class GuiItemPanel extends Gui
	{
		private int xPos;
		private int yPos;
		private int numberOfItemCols;
		private int numberOfItemRows;
		private int numberOfItemsPerPage;
		private int numberOfPages;
		private TogglyfierItemInfo lastClickedInfo;
		private int currentPage;
		private TogglyfierItemInfo currentItemInfo;
		private TogglyfierItemInfo tempZeroDamageII;
		private ItemStack currentItemInfoAsStack;
		private ItemStack drawItemStack;
		
		final GuiTogglyfierConfig this$0;
		
		public GuiItemPanel(GuiTogglyfierConfig var1, int var2, int var3, int var4, int var5, TogglyfierItemInfo var6)
		{
			this.this$0 = var1;
			this.currentPage = 0;
			this.currentItemInfo = new TogglyfierItemInfo(STONE, -1);
			this.tempZeroDamageII = new TogglyfierItemInfo(STONE, 0);
			this.currentItemInfoAsStack = new ItemStack(STONE, 1, -1);
			this.drawItemStack = new ItemStack(Items.AIR, 0, 0);
			this.xPos = var2;
			this.yPos = var3;
			this.numberOfItemCols = var4/EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON;
			this.numberOfItemRows = var5/EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON;
			this.numberOfItemsPerPage = this.numberOfItemCols * this.numberOfItemRows;
			this.numberOfPages = (int)Math.ceil(GuiTogglyfierConfig.access$000().size()/ (double)this.numberOfItemsPerPage);
			this.currentItemInfo = var6.copy();
			this.lastClickedInfo = var6.copy();
		}
		
		void drawPanel(int var1, int var2)
		{
			GL11.glPushMatrix();
			GL11.glRotatef(120.0F, 1.0F, 0.0F, 0.0F);
			RenderHelper.enableStandardItemLighting();
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			int var3 = this.currentPage * this.numberOfItemsPerPage;
			int var4 = 0;
			int var5;
			int var6;
			TogglyfierItemInfo var7;
			int var9;
			
			while (var4 < this.numberOfItemCols)
			{
				var5 = 0;
				
				while (true)
				{
					if (var5 < this.numberOfItemRows)
					{
						var6 = var3 + var5 * this.numberOfItemCols + var4;
						
						if (var6 < GuiTogglyfierConfig.access$000().size())
						{
							var7 = (TogglyfierItemInfo) GuiTogglyfierConfig.access$000().get(var6);
							this.drawItemStack = new ItemStack(var7.getItem(), 1, var7.getItemDamage());
							int var8 = this.xPos + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* var4;
							var9 = this.yPos + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* var5;
							GuiTogglyfierConfig.access$100().renderItemIntoGUI(this.drawItemStack, var8, var9);
							++var5;
							continue;
						}
					}
					
					++var4;
					break;
				}
			}
			
			var4 = GuiTogglyfierConfig.access$000().indexOf(
				this.currentItemInfo.getItemDamage() >= 0
					? this.currentItemInfo
					: this.tempZeroDamageII.setInfo(this.currentItemInfo.getItem(), 0));
			var5 = var4;
			
			if (this.currentItemInfo.hasSubtypes() && this.currentItemInfo.getItemDamage() < 0)
			{
				while (var5 < GuiTogglyfierConfig.access$000().size() - 1 && ((TogglyfierItemInfo) GuiTogglyfierConfig.access$000().get(var5 + 1)).getItem() == this.currentItemInfo.getItem())
				{
					++var5;
				}
			}
			
			for (var6 = var4; var6 <= var5; ++var6)
			{
				if (var6 >= var3 && var6 < var3 + this.numberOfItemsPerPage)
				{
					this.drawRectAtIndex(var6 % this.numberOfItemsPerPage, -1593835521);
				}
			}
			
			var6 = this.getItemArrayIndexFromMousePos(var1, var2);
			
			if (var6 >= 0 && var6 < GuiTogglyfierConfig.access$000().size())
			{
				var7 = (TogglyfierItemInfo) GuiTogglyfierConfig.access$000().get(var6);
				this.drawItemStack = new ItemStack(var7.getItem(), 1, var7.getItemDamage());
				this.drawRectAtIndex(var6 % this.numberOfItemsPerPage, -2130706433);
				String var11 = GuiTogglyfierConfig.access$200(this.this$0, this.drawItemStack);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				var9 = var1 + 12;
				int var10 = var2 - 12;
				this.drawGradientRect(var9 - 3, var10 - 3, var9 + this.this$0.fontRenderer.getStringWidth(var11) + 3, var10 + 11, -1073741824, -1073741824);
				this.this$0.fontRenderer.drawStringWithShadow(var11, var9, var10, -1);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glPopMatrix();
		}
		
		private int getItemArrayIndexFromMousePos(int var1, int var2)
		{
			if (var1 >= this.xPos - 1 && var1 <= this.xPos + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* this.numberOfItemCols && var2 >= this.yPos - 1 && var2 <= this.yPos + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* this.numberOfItemRows)
			{
				int var3 = (var1 - this.xPos - 1) /EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON;
				int var4 = (var2 - this.yPos - 1) /EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON;
				return this.currentPage * this.numberOfItemsPerPage + var3 + var4 * this.numberOfItemCols;
			}
			else
			{
				return -1;
			}
		}
		
		boolean mouseClicked(int var1, int var2, boolean var3)
		{
			if (var1 >= this.xPos && var1 <= this.xPos + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* this.numberOfItemCols && var2 >= this.yPos && var2 <= this.yPos + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* this.numberOfItemRows)
			{
				int var4 = this.getItemArrayIndexFromMousePos(var1, var2);
				
				if (var4 >= 0 && var4 < GuiTogglyfierConfig.access$000().size())
				{
					if (var3)
					{
						TogglyfierItemInfo var5 = (TogglyfierItemInfo) GuiTogglyfierConfig.access$000().get(var4);
						
						if (this.currentItemInfo.getItem() == var5.getItem() && this.currentItemInfo.getItemDamage() != var5.getItemDamage())
						{
							this.currentItemInfo = var5;
						}
						else
						{
							this.currentItemInfo = var5.stripDamageAndCopy();
						}
					}
					else
					{
						this.lastClickedInfo = ((TogglyfierItemInfo) GuiTogglyfierConfig.access$000().get(var4));
					}
				}
				
				return true;
			}
			else
			{
				return false;
			}
		}
		
		private void drawRectAtIndex(int var1, int var2)
		{
			int var3 = this.xPos - 1 + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* (var1 % this.numberOfItemCols);
			int var4 = this.yPos - 1 + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON* (var1 / this.numberOfItemCols);
			drawRect(var3, var4, var3 + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON, var4 + EIGHTEEN_WHICH_IS_IMPORTANT_FOR_SOME_REASON, var2);
		}
		
		TogglyfierItemInfo getCurrentItemInfo()
		{
			return this.currentItemInfo;
		}
		
		ItemStack getCurrentItemStack()
		{
			this.currentItemInfoAsStack = new ItemStack(this.currentItemInfo.getItem(), 1, this.currentItemInfo.getItemDamage());
			return this.currentItemInfoAsStack;
		}
		
		Item getLastClickedID()
		{
			return this.lastClickedInfo.getItem();
		}
		
		void incrementPage()
		{
			this.currentPage = (this.currentPage + 1) % this.numberOfPages;
		}
		
		void decrementPage()
		{
			this.currentPage = (this.currentPage == 0 ? this.numberOfPages : this.currentPage) - 1;
		}
		
		int getCurrentPage()
		{
			return this.currentPage;
		}
		
		int getNumberOfPages()
		{
			return this.numberOfPages;
		}
	}
	
	static class GuiCheckButton extends GuiButton
	{
		private int buttonWidth;
		
		public boolean buttonState;
		
		final GuiTogglyfierConfig this$0;
		
		GuiCheckButton(GuiTogglyfierConfig var1, int var2, int var3, int var4, String var5)
		{
			super(var4, var2, var3, var5);
			this.this$0 = var1;
			this.buttonWidth = 14 + var1.mc.fontRenderer.getStringWidth(var5);
		}
		
		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
		{
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.this$0.mc.renderEngine.getTexture(new ResourceLocation("/gui/toggleblock.png")).getGlTextureId());
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.x, this.y, this.buttonState ? 190 : 180, 0, 10, 10);
			this.drawString(this.this$0.fontRenderer, this.displayString, this.x + 14, this.y + 1, 14737632);
		}
		
		public void drawButton()
		{
			drawButton(Minecraft.getMinecraft(),0,0, 0);
		}
		
		@Override
		public boolean mousePressed(Minecraft mc, int var1, int var2)
		{
			return var1 >= this.x && var2 >= this.y && var1 <= this.x + this.buttonWidth && var2 <= this.y + 10;
		}
	}
	
}
