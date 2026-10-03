package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

/**
 * Pure bridging rules between an item form and {@link WorkbenchSlots}: only Toolboxes
 * are accepted, one per slot, occupied slots reject, empty clears. No Minecraft types.
 */
public class WorkbenchSlotItems<T, S> {

	private final WorkbenchSlots<S> slots;
	private final ItemAdapter<T, S> items;
	private final Runnable onChanged;

	public WorkbenchSlotItems(WorkbenchSlots<S> slots, ItemAdapter<T, S> items, Runnable onChanged) {
		this.slots = slots;
		this.items = items;
		this.onChanged = onChanged;
	}

	public int getSlots() {
		return WorkbenchSlots.CAPACITY;
	}

	public T getStackInSlot(int slot) {
		S stored = slots.get(slot);
		return stored == null ? items.empty() : items.restore(stored);
	}

	public T insertItem(int slot, T stack, boolean simulate) {
		if (items.isEmpty(stack))
			return items.empty();
		if (slot < 0 || slot >= getSlots() || slots.get(slot) != null)
			return stack;
		if (!items.isToolbox(stack))
			return stack;
		if (!simulate) {
			slots.insertAt(slot, items.snapshot(stack));
			onChanged.run();
		}
		return items.empty();
	}

	public T extractItem(int slot, int amount, boolean simulate) {
		S stored = slots.get(slot);
		if (stored == null || amount <= 0)
			return items.empty();
		if (simulate)
			return items.restore(stored);
		S removed = slots.remove(slot);
		onChanged.run();
		return removed == null ? items.empty() : items.restore(removed);
	}

	public void setStackInSlot(int slot, T stack) {
		if (slot < 0 || slot >= getSlots())
			return;
		if (items.isEmpty(stack)) {
			slots.remove(slot);
		} else {
			if (!items.isToolbox(stack))
				return;
			slots.setAt(slot, items.snapshot(stack));
		}
		onChanged.run();
	}

	public int getSlotLimit(int slot) {
		return 1;
	}

	public boolean isItemValid(int slot, T stack) {
		return items.isToolbox(stack);
	}
}
