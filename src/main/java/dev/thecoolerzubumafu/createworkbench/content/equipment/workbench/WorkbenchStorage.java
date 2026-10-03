package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

/**
 * Minecraft-facing storage: {@link WorkbenchSlots} of {@link StoredToolbox} plus the
 * item and NBT adapters. The slot rules live in the pure parent class.
 */
public class WorkbenchStorage extends WorkbenchSlots<StoredToolbox> {

	public boolean insert(ItemStack item) {
		StoredToolbox toolbox = ToolboxItems.snapshot(item);
		return toolbox != null && insert(toolbox);
	}

	public void setContents(WorkbenchContents contents) {
		setAll(contents.toolboxes());
	}

	public CompoundTag serializeNBT(HolderLookup.Provider registries) {
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
		CompoundTag tag = new CompoundTag();
		tag.put("Toolboxes", WorkbenchContents.CODEC
			.encodeStart(ops, new WorkbenchContents(contents()))
			.getOrThrow());
		return tag;
	}

	public void deserializeNBT(HolderLookup.Provider registries, CompoundTag tag) {
		clear();
		if (!tag.contains("Toolboxes"))
			return;
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
		WorkbenchContents contents = WorkbenchContents.CODEC
			.parse(ops, tag.get("Toolboxes"))
			.getOrThrow();
		setAll(contents.toolboxes());
	}
}
