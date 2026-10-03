package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.Map;

import com.mojang.serialization.MapCodec;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorage;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import dev.thecoolerzubumafu.createworkbench.AllMountedStorageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

/**
 * Contraption-mounted view of a Workbench: all eight Stored Toolboxes flattened into
 * one handler (32 slots each, 256 total), preserving each Toolbox's own filters and
 * identity. The stored records share their {@link ToolboxInventory} instances with the
 * block entity, which is removed while the contraption moves.
 */
public class WorkbenchMountedStorage extends MountedItemStorage {

	public static final int SLOTS_PER_TOOLBOX = 8 * ToolboxInventory.STACKS_PER_COMPARTMENT;
	public static final int TOTAL_SLOTS = WorkbenchStorage.CAPACITY * SLOTS_PER_TOOLBOX;

	public static final MapCodec<WorkbenchMountedStorage> CODEC = WorkbenchContents.CODEC
		.xmap(WorkbenchMountedStorage::new, storage -> storage.contents)
		.fieldOf("value");

	private final WorkbenchContents contents;
	private final Map<Integer, StoredToolbox> bySlot;

	public WorkbenchMountedStorage(WorkbenchContents contents) {
		this(AllMountedStorageTypes.WORKBENCH.get(), contents);
	}

	protected WorkbenchMountedStorage(MountedItemStorageType<?> type, WorkbenchContents contents) {
		super(type);
		this.contents = contents;
		this.bySlot = contents.toolboxes();
	}

	private StoredToolbox storedFor(int slot) {
		if (slot < 0 || slot >= TOTAL_SLOTS)
			return null;
		return bySlot.get(slot / SLOTS_PER_TOOLBOX);
	}

	private int inner(int slot) {
		return slot % SLOTS_PER_TOOLBOX;
	}

	@Override
	public int getSlots() {
		return TOTAL_SLOTS;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		StoredToolbox stored = storedFor(slot);
		return stored == null ? ItemStack.EMPTY : stored.inventory()
			.getStackInSlot(inner(slot));
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		StoredToolbox stored = storedFor(slot);
		if (stored != null)
			stored.inventory()
				.setStackInSlot(inner(slot), stack);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		StoredToolbox stored = storedFor(slot);
		return stored == null ? stack : stored.inventory()
			.insertItem(inner(slot), stack, simulate);
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		StoredToolbox stored = storedFor(slot);
		return stored == null ? ItemStack.EMPTY : stored.inventory()
			.extractItem(inner(slot), amount, simulate);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 64;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		StoredToolbox stored = storedFor(slot);
		return stored != null && stored.inventory()
			.isItemValid(inner(slot), stack);
	}

	@Override
	public void unmount(Level level, BlockState state, BlockPos pos, BlockEntity be) {
		if (be instanceof WorkbenchBlockEntity workbench)
			workbench.readContents(contents);
	}

	@Override
	public boolean handleInteraction(ServerPlayer player, Contraption contraption, StructureBlockInfo info) {
		return false;
	}
}
