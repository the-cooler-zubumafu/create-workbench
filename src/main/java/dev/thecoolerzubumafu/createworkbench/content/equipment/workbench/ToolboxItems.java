package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.UUID;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

public final class ToolboxItems {

	private ToolboxItems() {
	}

	public static StoredToolbox snapshot(ItemStack item) {
		DyeColor color = colorOf(item);
		if (color == null)
			return null;
		ToolboxInventory inventory = item.get(AllDataComponents.TOOLBOX_INVENTORY);
		if (inventory == null)
			inventory = new ToolboxInventory(null);
		UUID id = item.get(AllDataComponents.TOOLBOX_UUID);
		if (id == null)
			id = UUID.randomUUID();
		Component customName = item.get(DataComponents.CUSTOM_NAME);
		return new StoredToolbox(inventory, color, id, customName);
	}

	public static ItemStack restore(StoredToolbox toolbox) {
		ItemStack item = AllBlocks.TOOLBOXES.get(toolbox.color())
			.asStack();
		item.set(AllDataComponents.TOOLBOX_INVENTORY, toolbox.inventory());
		item.set(AllDataComponents.TOOLBOX_UUID, toolbox.uuid());
		if (toolbox.customName() != null)
			item.set(DataComponents.CUSTOM_NAME, toolbox.customName());
		return item;
	}

	public static DyeColor colorOf(ItemStack item) {
		for (DyeColor color : DyeColor.values())
			if (AllBlocks.TOOLBOXES.get(color)
				.get()
				.asItem() == item.getItem())
				return color;
		return null;
	}
}
