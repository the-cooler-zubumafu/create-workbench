package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.menu.AbstractSimiContainerScreen;
import com.simibubi.create.foundation.gui.widget.IconButton;

import dev.engine_room.flywheel.lib.transform.TransformStack;
import dev.thecoolerzubumafu.createworkbench.AllBlocks;
import dev.thecoolerzubumafu.createworkbench.OpenWorkbenchToolboxPacket;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class WorkbenchScreen extends AbstractSimiContainerScreen<WorkbenchMenu> {

	private static final AllGuiTextures BG = AllGuiTextures.TOOLBOX;
	private static final AllGuiTextures PLAYER = AllGuiTextures.PLAYER_INVENTORY;

	private List<Rect2i> extraAreas = Collections.emptyList();

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

		extraAreas = ImmutableList.of(
			new Rect2i(leftPos + 30 + BG.getWidth(), topPos + BG.getHeight() - 15 - 34 - 6, 72, 68)
		);
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

		renderWorkbench(graphics, x + BG.getWidth() + 50, y + BG.getHeight() + 12);
	}

	/**
	 * Draws the Workbench model next to the screen and animates it exactly like the
	 * placed block, mirroring Create's Toolbox preview: the block entity renderer is
	 * invoked in the GUI with the Workbench's {@code lidOpen}/<code>drawerLeftOpen</code>/
	 * <code>drawerRightOpen</code> lerped values, so right-clicking plays the same open motion as the world block.
	 * Rendered in its default orientation for a consistent isometric view.
	 */
	private void renderWorkbench(GuiGraphics graphics, int x, int y) {
		WorkbenchBlockEntity workbench = workbench();
		if (workbench == null)
			return;

		PoseStack ms = graphics.pose();
		TransformStack.of(ms)
			.pushPose()
			.translate(x, y, 100)
			.scale(50)
			.rotateXDegrees(-22)
			.rotateYDegrees(-202);

		GuiGameElement.of(AllBlocks.WORKBENCH.get()
			.defaultBlockState(), workbench)
			.render(graphics);

		ms.popPose();
	}

	private WorkbenchBlockEntity workbench() {
		if (menu.workbench() != null)
			return menu.workbench();
		if (minecraft == null || minecraft.level == null)
			return null;
		return minecraft.level.getBlockEntity(menu.workbenchPos()) instanceof WorkbenchBlockEntity workbench ? workbench : null;
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
			playSelectSound();
			PacketDistributor.sendToServer(new OpenWorkbenchToolboxPacket(menu.workbenchPos(), hoveredSlot.index));
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public List<Rect2i> getExtraAreas() {
		return extraAreas;
	}

	private void playSelectSound() {
		if (minecraft == null || minecraft.level == null)
			return;
		Vec3 center = Vec3.atCenterOf(menu.workbenchPos());
		playAt(center, SoundEvents.IRON_DOOR_OPEN, 0.25F, 1.2F);
		playAt(center, SoundEvents.CHEST_OPEN, 0.1F, 1.1F);
	}

	private void playAt(Vec3 pos, SoundEvent sound, float volume, float basePitch) {
		minecraft.level.playLocalSound(pos.x, pos.y, pos.z, sound, SoundSource.BLOCKS, volume,
			minecraft.level.random.nextFloat() * 0.1F + basePitch, true);
	}
}
