package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widget.IconButton;

import dev.thecoolerzubumafu.createworkbench.OpenWorkbenchMenuPacket;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Create's {@link ToolboxScreen} for a stored Toolbox, with its confirm ("check") button
 * re-wired to return to the Workbench menu instead of closing the container.
 */
public class WorkbenchContentsScreen extends ToolboxScreen {

	public WorkbenchContentsScreen(ToolboxMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Override
	protected void init() {
		super.init();
		int confirmX = leftPos + 30 + AllGuiTextures.TOOLBOX.getWidth() - 33;
		int confirmY = topPos + AllGuiTextures.TOOLBOX.getHeight() - 24;
		for (GuiEventListener child : children()) {
			if (child instanceof IconButton button && button.getX() == confirmX && button.getY() == confirmY)
				button.withCallback(() -> PacketDistributor.sendToServer(
					new OpenWorkbenchMenuPacket(menu.contentHolder.getBlockPos())));
		}
	}
}
