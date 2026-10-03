package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

public class WorkbenchItemHandler implements IItemHandler {

	private final WorkbenchStorage storage;
	private final Runnable onChanged;

	public WorkbenchItemHandler(WorkbenchStorage storage, Runnable onChanged) {
		this.storage = storage;
		this.onChanged = onChanged;
	}

	@Override
	public int getSlots() {
		return WorkbenchStorage.CAPACITY;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		StoredToolbox toolbox = storage.get(slot);
		return toolbox == null ? ItemStack.EMPTY : ToolboxItems.restore(toolbox);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		if (stack.isEmpty())
			return ItemStack.EMPTY;
		if (slot < 0 || slot >= getSlots() || storage.get(slot) != null)
			return stack;
		StoredToolbox toolbox = ToolboxItems.snapshot(stack);
		if (toolbox == null)
			return stack;
		if (!simulate) {
			storage.insertAt(slot, toolbox);
			onChanged.run();
		}
		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		StoredToolbox toolbox = storage.get(slot);
		if (toolbox == null || amount <= 0)
			return ItemStack.EMPTY;
		if (simulate)
			return ToolboxItems.restore(toolbox);
		StoredToolbox removed = storage.remove(slot);
		onChanged.run();
		return removed == null ? ItemStack.EMPTY : ToolboxItems.restore(removed);
	}

	@Override
	public int getSlotLimit(int slot) {
		return 1;
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return ToolboxItems.snapshot(stack) != null;
	}
}
