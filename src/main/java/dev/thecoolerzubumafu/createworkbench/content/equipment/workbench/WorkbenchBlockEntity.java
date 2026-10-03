package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import dev.thecoolerzubumafu.createworkbench.AllBlockEntities;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WorkbenchBlockEntity extends BlockEntity {

    private final WorkbenchStorage storage = new WorkbenchStorage();
    private final WorkbenchItemHandler itemHandler = new WorkbenchItemHandler(storage, this::setChanged);

    public WorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntities.WORKBENCH_BLOCKENTITY.get(), pos, state);
    }

    public WorkbenchStorage storage() {
        return storage;
    }

    public WorkbenchItemHandler itemHandler() {
        return itemHandler;
    }

    public boolean insertToolbox(ItemStack item) {
        boolean inserted = storage.insert(item);
        if (inserted) {
            setChanged();
            CreateWorkbench.LOGGER.debug("Workbench at {} stored a toolbox", worldPosition);
        }
        return inserted;
    }

    public void readContents(WorkbenchContents contents) {
        storage.setContents(contents);
        setChanged();
        CreateWorkbench.LOGGER.debug("Workbench at {} restored {} toolboxes", worldPosition,
                contents.toolboxes().size());
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
