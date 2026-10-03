package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.simibubi.create.content.equipment.toolbox.ToolboxMenu;
import com.simibubi.create.content.equipment.toolbox.ToolboxScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widget.IconButton;

import dev.thecoolerzubumafu.createworkbench.OpenWorkbenchMenuPacket;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Create's {@link ToolboxScreen} for a stored Toolbox. Its confirm ("check") button is
 * re-wired to return to the Workbench menu, and its preview model is animated open (plus
 * the usual sounds) since the stored Toolbox's block entity is not ticked by any level.
 */
public class WorkbenchContentsScreen extends ToolboxScreen {

	private boolean previewOpened;

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
		if (!previewOpened) {
			previewOpened = true;
			playPreviewSound(SoundEvents.IRON_DOOR_OPEN, 0.25F, 1.2F);
			playPreviewSound(SoundEvents.CHEST_OPEN, 0.1F, 1.1F);
		}
	}

	@Override
	protected void containerTick() {
		super.containerTick();
		ToolboxBlockEntity holder = menu.contentHolder;
		holder.lid.chase(1, 0.2F, LerpedFloat.Chaser.LINEAR);
		holder.drawers.chase(1, 0.2F, LerpedFloat.Chaser.EXP);
		holder.lid.tickChaser();
		holder.drawers.tickChaser();
	}

	@Override
	public void removed() {
		super.removed();
		playPreviewSound(SoundEvents.CHEST_CLOSE, 0.1F, 1.1F);
		playPreviewSound(SoundEvents.IRON_DOOR_CLOSE, 0.25F, 1.2F);
	}

	private void playPreviewSound(SoundEvent sound, float volume, float basePitch) {
		if (minecraft == null || minecraft.level == null)
			return;
		Vec3 center = Vec3.atCenterOf(menu.contentHolder.getBlockPos());
		minecraft.level.playLocalSound(center.x, center.y, center.z, sound, SoundSource.BLOCKS, volume,
			minecraft.level.random.nextFloat() * 0.1F + basePitch, true);
	}
}
