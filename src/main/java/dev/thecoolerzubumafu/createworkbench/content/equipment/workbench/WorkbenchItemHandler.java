package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class WorkbenchItemHandler implements IItemHandlerModifiable {

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
		if (slot < 0 || slot >= getSlots() || storage.get(slot) != null) {
			CreateWorkbench.LOGGER.debug("Workbench insert rejected: slot {} occupied or out of range", slot);
			return stack;
		}
		StoredToolbox toolbox = ToolboxItems.snapshot(stack);
		if (toolbox == null) {
			CreateWorkbench.LOGGER.debug("Workbench insert rejected: slot {} (not a toolbox)", slot);
			return stack;
		}
		if (!simulate) {
			storage.insertAt(slot, toolbox);
			CreateWorkbench.LOGGER.debug("Workbench inserted toolbox into slot {} (color={})", slot, toolbox.color());
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
		CreateWorkbench.LOGGER.debug("Workbench extracted toolbox from slot {}", slot);
		onChanged.run();
		return removed == null ? ItemStack.EMPTY : ToolboxItems.restore(removed);
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		if (slot < 0 || slot >= getSlots())
			return;
		if (stack.isEmpty()) {
			storage.remove(slot);
			CreateWorkbench.LOGGER.debug("Workbench cleared slot {}", slot);
		} else {
			StoredToolbox toolbox = ToolboxItems.snapshot(stack);
			if (toolbox == null) {
				CreateWorkbench.LOGGER.debug("Workbench setStackInSlot ignored: slot {} (not a toolbox)", slot);
				return;
			}
			storage.setAt(slot, toolbox);
			CreateWorkbench.LOGGER.debug("Workbench set slot {} (color={})", slot, toolbox.color());
		}
		onChanged.run();
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
