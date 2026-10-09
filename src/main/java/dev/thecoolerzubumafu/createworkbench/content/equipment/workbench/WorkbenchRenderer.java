package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Draws the moving parts of the Workbench. The static body is the block model; here
 * the two top lids slide apart on their local X axis and the two drawer halves (each
 * with its own chaser) slide out on their local -Z axis, all oriented by the block's
 * FACING. Driven by the BE's lerped values, so the motion eases in/out and is smooth
 * at any framerate.
 */
public class WorkbenchRenderer implements BlockEntityRenderer<WorkbenchBlockEntity> {

    private static final float LID_SLIDE = 6f / 16f;
    private static final float DRAWER_SLIDE = 4f / 16f;

    public WorkbenchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WorkbenchBlockEntity workbench, float partialTick, PoseStack ms, MultiBufferSource buffer,
                       int light, int overlay) {
        BlockState state = workbench.getBlockState();
        if (!(state.getBlock() instanceof WorkbenchBlock))
            return;

        Direction facing = state.getValue(WorkbenchBlock.FACING)
                .getOpposite();
        float lidOpen = workbench.lidOpen.getValue(partialTick);
        float drawerLeft = workbench.drawerLeftOpen.getValue(partialTick);
        float drawerRight = workbench.drawerRightOpen.getValue(partialTick);

        VertexConsumer builder = buffer.getBuffer(RenderType.cutoutMipped());

        float slide = LID_SLIDE * lidOpen;
        CachedBuffers.partial(WorkbenchPartialModels.LID_LEFT, state)
                .center()
                .rotateYDegrees(-facing.toYRot())
                .uncenter()
                .translate(-slide, 0, 0)
                .light(light)
                .renderInto(ms, builder);

        CachedBuffers.partial(WorkbenchPartialModels.LID_RIGHT, state)
                .center()
                .rotateYDegrees(-facing.toYRot())
                .uncenter()
                .translate(slide, 0, 0)
                .light(light)
                .renderInto(ms, builder);

        CachedBuffers.partial(WorkbenchPartialModels.DRAWER_LEFT, state)
                .center()
                .rotateYDegrees(-facing.toYRot())
                .uncenter()
                .translate(0, 0, -DRAWER_SLIDE * drawerLeft)
                .light(light)
                .renderInto(ms, builder);

        CachedBuffers.partial(WorkbenchPartialModels.DRAWER_RIGHT, state)
                .center()
                .rotateYDegrees(-facing.toYRot())
                .uncenter()
                .translate(0, 0, -DRAWER_SLIDE * drawerRight)
                .light(light)
                .renderInto(ms, builder);
    }
}
