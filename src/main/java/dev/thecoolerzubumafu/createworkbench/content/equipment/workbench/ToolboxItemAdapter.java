package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import net.minecraft.world.item.ItemStack;

/**
 * Minecraft-facing {@link ItemAdapter}: maps {@link ItemStack} Toolboxes to
 * {@link StoredToolbox} snapshots via {@link ToolboxItems}.
 */
public class ToolboxItemAdapter implements ItemAdapter<ItemStack, StoredToolbox> {

	@Override
	public ItemStack empty() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isEmpty(ItemStack item) {
		return item.isEmpty();
	}

	@Override
	public boolean isToolbox(ItemStack item) {
		return ToolboxItems.colorOf(item) != null;
	}

	@Override
	public StoredToolbox snapshot(ItemStack item) {
		return ToolboxItems.snapshot(item);
	}

	@Override
	public ItemStack restore(StoredToolbox stored) {
		return ToolboxItems.restore(stored);
	}
}
