package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

public class WorkbenchStorage {

	public static final int CAPACITY = 8;

	private final Map<Integer, StoredToolbox> toolboxes = new LinkedHashMap<>();

	public int size() {
		return toolboxes.size();
	}

	public boolean insert(ItemStack item) {
		StoredToolbox toolbox = ToolboxItems.snapshot(item);
		return toolbox != null && insert(toolbox);
	}

	public boolean insert(StoredToolbox toolbox) {
		int slot = firstFreeSlot();
		if (slot < 0)
			return false;
		toolboxes.put(slot, toolbox);
		return true;
	}

	public boolean insertAt(int slot, StoredToolbox toolbox) {
		if (slot < 0 || slot >= CAPACITY || toolboxes.containsKey(slot))
			return false;
		toolboxes.put(slot, toolbox);
		return true;
	}

	public StoredToolbox get(int slot) {
		return toolboxes.get(slot);
	}

	public StoredToolbox remove(int slot) {
		return toolboxes.remove(slot);
	}

	public Map<Integer, StoredToolbox> contents() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(toolboxes));
	}

	public void clear() {
		toolboxes.clear();
	}

	public void setContents(WorkbenchContents contents) {
		toolboxes.clear();
		toolboxes.putAll(contents.toolboxes());
	}

	private int firstFreeSlot() {
		for (int slot = 0; slot < CAPACITY; slot++)
			if (!toolboxes.containsKey(slot))
				return slot;
		return -1;
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
		toolboxes.clear();
		if (!tag.contains("Toolboxes"))
			return;
		RegistryOps<Tag> ops = RegistryOps.create(NbtOps.INSTANCE, registries);
		WorkbenchContents contents = WorkbenchContents.CODEC
			.parse(ops, tag.get("Toolboxes"))
			.getOrThrow();
		toolboxes.putAll(contents.toolboxes());
	}
}
