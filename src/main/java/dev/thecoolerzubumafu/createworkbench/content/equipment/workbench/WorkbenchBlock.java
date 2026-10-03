package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.mojang.serialization.MapCodec;

import dev.thecoolerzubumafu.createworkbench.AllDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

public class WorkbenchBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box(
            -8.0D,
            1.0D,
            0.0D,
            24.0D,
            16.0D,
            16.0D
    );
    public static final MapCodec<WorkbenchBlock> CODEC = simpleCodec(WorkbenchBlock::new);

    public WorkbenchBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new WorkbenchBlockEntity(blockPos, blockState);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        WorkbenchContents contents = stack.get(AllDataComponents.WORKBENCH_CONTENTS.get());
        if (contents != null && level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench)
            workbench.readContents(contents);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack item = new ItemStack(this);
        if (level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench)
            item.set(AllDataComponents.WORKBENCH_CONTENTS.get(),
                    new WorkbenchContents(workbench.storage().contents()));
        return item;
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (player instanceof FakePlayer)
            return;
        if (level.isClientSide)
            return;
        if (!(level instanceof ServerLevel))
            return;

        ItemStack picked = getCloneItemStack(level, pos, state);
        level.destroyBlock(pos, false);
        if (level.getBlockState(pos) != state)
            player.getInventory().placeItemBackInInventory(picked);
    }
}
