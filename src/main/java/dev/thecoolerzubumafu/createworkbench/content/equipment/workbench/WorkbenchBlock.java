package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.mojang.serialization.MapCodec;

import dev.thecoolerzubumafu.createworkbench.AllDataComponents;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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

    public static final String LOCKED_KEY = "createworkbench.workbench.locked";
    public static final String UNLOCKED_KEY = "createworkbench.workbench.unlocked";

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
    public MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench))
            return null;
        return new SimpleMenuProvider(
                (id, playerInventory, player) -> new WorkbenchMenu(id, playerInventory, workbench,
                        ContainerLevelAccess.create(level, pos)),
                Component.translatable(getDescriptionId()));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        if (player.isCrouching() && stack.getItem() instanceof WorkbenchKeyItem) {
            if (level.isClientSide)
                return ItemInteractionResult.SUCCESS;
            boolean flipped = workbench.toggleLock(stack);
            if (flipped)
                player.displayClientMessage(
                        Component.translatable(workbench.isLocked() ? LOCKED_KEY : UNLOCKED_KEY), true);
            else
                player.displayClientMessage(Component.translatable(LOCKED_KEY), true);
            return ItemInteractionResult.SUCCESS;
        }

        if (!workbench.canAccess(player)) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable(LOCKED_KEY), true);
            return ItemInteractionResult.FAIL;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench))
            return InteractionResult.PASS;
        if (!workbench.canAccess(player)) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable(LOCKED_KEY), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer)
            workbench.openMenu(serverPlayer);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        WorkbenchContents contents = stack.get(AllDataComponents.WORKBENCH_CONTENTS.get());
        if (contents != null && level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench)
            workbench.readContents(contents);
        if (stack.has(AllDataComponents.WORKBENCH_LOCK.get())
                && level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench)
            workbench.restoreLock(stack.get(AllDataComponents.WORKBENCH_LOCK.get()));
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack item = new ItemStack(this);
        if (level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench) {
            item.set(AllDataComponents.WORKBENCH_CONTENTS.get(),
                    new WorkbenchContents(workbench.storage().contents()));
            if (workbench.isLocked())
                item.set(AllDataComponents.WORKBENCH_LOCK.get(), workbench.lock().lockId());
        }
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
        if (level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench && !workbench.canAccess(player)) {
            player.displayClientMessage(Component.translatable(LOCKED_KEY), true);
            return;
        }

        ItemStack picked = getCloneItemStack(level, pos, state);
        level.destroyBlock(pos, false);
        if (level.getBlockState(pos) != state)
            player.getInventory().placeItemBackInInventory(picked);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench && !workbench.canAccess(player))
            return 0.0F;
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof WorkbenchBlockEntity workbench && workbench.isLocked())
            return 3600000.0F;
        return super.getExplosionResistance(state, level, pos, explosion);
    }
}
