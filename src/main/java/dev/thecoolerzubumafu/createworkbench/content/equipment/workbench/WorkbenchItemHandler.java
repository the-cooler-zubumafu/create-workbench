package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import dev.thecoolerzubumafu.createworkbench.CreateWorkbench;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Minecraft/NeoForge adapter over the pure {@link WorkbenchSlotItems} rules.
 */
public class WorkbenchItemHandler implements IItemHandlerModifiable {

	private final WorkbenchSlotItems<ItemStack, StoredToolbox> rules;

	public WorkbenchItemHandler(WorkbenchStorage storage, Runnable onChanged) {
		this.rules = new WorkbenchSlotItems<>(storage, new ToolboxItemAdapter(), onChanged);
	}

	@Override
	public int getSlots() {
		return rules.getSlots();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return rules.getStackInSlot(slot);
	}

	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
		ItemStack remainder = rules.insertItem(slot, stack, simulate);
		if (!simulate && remainder.isEmpty() && !stack.isEmpty())
			CreateWorkbench.LOGGER.debug("Workbench inserted a toolbox into slot {}", slot);
		return remainder;
	}

	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		ItemStack result = rules.extractItem(slot, amount, simulate);
		if (!simulate && !result.isEmpty())
			CreateWorkbench.LOGGER.debug("Workbench extracted a toolbox from slot {}", slot);
		return result;
	}

	@Override
	public void setStackInSlot(int slot, ItemStack stack) {
		rules.setStackInSlot(slot, stack);
		CreateWorkbench.LOGGER.debug("Workbench set slot {}", slot);
	}

	@Override
	public int getSlotLimit(int slot) {
		return rules.getSlotLimit(slot);
	}

	@Override
	public boolean isItemValid(int slot, ItemStack stack) {
		return rules.isItemValid(slot, stack);
	}
}
