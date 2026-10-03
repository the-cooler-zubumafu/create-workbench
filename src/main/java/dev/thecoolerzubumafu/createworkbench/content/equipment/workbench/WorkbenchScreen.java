package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;

import dev.thecoolerzubumafu.createworkbench.OpenWorkbenchToolboxPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

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

		IconButton confirmButton = new IconButton(leftPos + 30 + BG.getWidth() - 33,
			topPos + BG.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(() -> minecraft.player.closeContainer());
		addRenderableWidget(confirmButton);
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

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0 && hasControlDown() && hoveredSlot != null
				&& hoveredSlot.index < WorkbenchStorage.CAPACITY && hoveredSlot.hasItem()) {
			PacketDistributor.sendToServer(new OpenWorkbenchToolboxPacket(menu.workbenchPos(), hoveredSlot.index));
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}
}
