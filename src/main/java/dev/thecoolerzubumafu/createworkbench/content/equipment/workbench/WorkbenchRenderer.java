package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Draws the Workbench's Stored Toolboxes on top of the block, in a 4x2 grid. Each is
 * rendered with its own Create toolbox block model, so colour/identity show through.
 */
public class WorkbenchRenderer implements BlockEntityRenderer<WorkbenchBlockEntity> {

	private static final int COLUMNS = 4;

	public WorkbenchRenderer(BlockEntityRendererProvider.Context context) {
	}

	@Override
	public void render(WorkbenchBlockEntity workbench, float partialTick, PoseStack ms, MultiBufferSource buffer,
	                   int light, int overlay) {
		List<StoredToolbox> stored = new ArrayList<>(workbench.storage()
			.contents()
			.values());
		if (stored.isEmpty())
			return;

		ms.pushPose();
		ms.translate(0.5, 1.02, 0.5);
		for (int i = 0; i < stored.size(); i++) {
			int column = i % COLUMNS;
			int row = i / COLUMNS;
			ms.pushPose();
			ms.translate((column - (COLUMNS - 1) / 2f) * 0.3, 0, (row - 0.5f) * 0.3);
			ms.scale(0.28f, 0.28f, 0.28f);
			ItemStack stack = ToolboxItems.restore(stored.get(i));
			Minecraft.getInstance()
				.getItemRenderer()
				.renderStatic(stack, ItemDisplayContext.FIXED, light, OverlayTexture.NO_OVERLAY, ms, buffer,
					workbench.getLevel(), 0);
			ms.popPose();
		}
		ms.popPose();
	}
}
