package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import dev.thecoolerzubumafu.createworkbench.AllBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WorkbenchBlockEntity extends BlockEntity {

    private final WorkbenchStorage storage = new WorkbenchStorage();

    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.WORKBENCH_BLOCKENTITY.get(), pos, state);
    }

    public WorkbenchStorage storage() {
        return storage;
    }

    public boolean insertToolbox(ItemStack item) {
        boolean inserted = storage.insert(item);
        if (inserted)
            setChanged();
        return inserted;
    }

    public void readContents(WorkbenchContents contents) {
        storage.setContents(contents);
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Storage", storage.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        storage.deserializeNBT(registries, tag.getCompound("Storage"));
    }

}
