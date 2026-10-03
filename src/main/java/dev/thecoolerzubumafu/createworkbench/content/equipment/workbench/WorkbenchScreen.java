package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class WorkbenchScreen extends AbstractSimiContainerScreen<WorkbenchMenu> {

	private static final AllGuiTextures BG = AllGuiTextures.TOOLBOX;
	private static final AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;

	public WorkbenchScreen(WorkbenchMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Override
	protected void init() {
		setWindowSize(30 + BG.getWidth(), BG.getHeight() + PLAYER.getHeight() - 24);
		setWindowOffset(-11, 0);
		super.init();
	}

	@Override
	protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
		int x = leftPos + imageWidth - BG.getWidth();
		int y = topPos;

		BG.render(graphics, x, y);
		graphics.drawString(font, title, x + 15, y + 4, 0x592424, false);

		int inventoryX = leftPos;
		int inventoryY = topPos + imageHeight - PLAYER.getHeight();
		renderPlayerInventory(graphics, inventoryX, inventoryY);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		renderTooltip(graphics, mouseX, mouseY);
	}
}
