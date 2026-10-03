package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.toolbox.ToolboxBlockEntity;

import dev.thecoolerzubumafu.createworkbench.AllBlockEntities;
import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
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

    public WorkbenchContentsMenu createContentsMenu(int id, Inventory inventory, int slot) {
        StoredToolbox stored = storage.get(slot);
        if (stored == null)
            return null;
        return new WorkbenchContentsMenu(id, inventory, buildHolder(stored), this, slot, stored);
    }

    public void openStoredToolbox(ServerPlayer player, int slot) {
        StoredToolbox stored = storage.get(slot);
        if (stored == null)
            return;
        ToolboxBlockEntity holder = buildHolder(stored);
        CompoundTag tag = holder.saveWithoutMetadata(player.level()
                .registryAccess());
        player.openMenu(new SimpleMenuProvider(
                        (id, inventory, p) -> new WorkbenchContentsMenu(id, inventory, holder, this, slot, stored),
                        Component.translatable("block.createworkbench.workbench")),
                buffer -> {
                    buffer.writeBlockPos(worldPosition);
                    buffer.writeNbt(tag);
                    buffer.writeVarInt(stored.color()
                            .getId());
                });
    }

    private ToolboxBlockEntity buildHolder(StoredToolbox stored) {
        ToolboxBlockEntity holder = new ToolboxBlockEntity(AllBlockEntityTypes.TOOLBOX.get(), worldPosition,
                AllBlocks.TOOLBOXES.get(stored.color())
                        .get()
                        .defaultBlockState());
        holder.setLevel(level);
        holder.readInventory(stored.inventory());
        return holder;
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
