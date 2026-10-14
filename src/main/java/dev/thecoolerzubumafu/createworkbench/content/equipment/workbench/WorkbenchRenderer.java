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
        float drawer = workbench.drawerOpen.getValue(partialTick);

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

        CachedBuffers.partial(WorkbenchPartialModels.DRAWER, state)
                .center()
                .rotateYDegrees(-facing.toYRot())
                .uncenter()
                .translate(0, 0, -DRAWER_SLIDE * drawer)
                .light(light)
                .renderInto(ms, builder);
    }
}
