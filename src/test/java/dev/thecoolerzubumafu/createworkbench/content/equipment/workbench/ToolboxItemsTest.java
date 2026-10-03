package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

class ToolboxItemsTest {

	@Test
	void snapshotCapturesColourContentsAndIdentity() {
		ItemStack item = AllBlocks.TOOLBOXES.get(DyeColor.RED)
			.asStack();
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));
		UUID id = UUID.randomUUID();
		item.set(AllDataComponents.TOOLBOX_INVENTORY, inventory);
		item.set(AllDataComponents.TOOLBOX_UUID, id);

		StoredToolbox stored = ToolboxItems.snapshot(item);

		assertEquals(DyeColor.RED, stored.color());
		assertEquals(id, stored.uuid());
		assertEquals(5, stored.inventory()
			.getStackInSlot(0)
			.getCount());
		assertTrue(stored.inventory()
			.getStackInSlot(0)
			.is(Items.DIAMOND));
	}

	@Test
	void restoreRebuildsAToolboxItemWithItsColourContentsAndIdentity() {
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 7));
		UUID id = UUID.randomUUID();
		StoredToolbox stored = new StoredToolbox(inventory, DyeColor.BLUE, id);

		ItemStack restored = ToolboxItems.restore(stored);

		assertTrue(restored.is(AllBlocks.TOOLBOXES.get(DyeColor.BLUE)
			.get()
			.asItem()));
		assertEquals(id, restored.get(AllDataComponents.TOOLBOX_UUID));
		assertEquals(7, restored.get(AllDataComponents.TOOLBOX_INVENTORY)
			.getStackInSlot(0)
			.getCount());
	}

	@Test
	void snapshotAndRestorePreserveACustomName() {
		ItemStack item = AllBlocks.TOOLBOXES.get(DyeColor.RED)
			.asStack();
		Component name = Component.literal("My Tools");
		item.set(DataComponents.CUSTOM_NAME, name);

		StoredToolbox stored = ToolboxItems.snapshot(item);

		assertEquals(name, stored.customName());

		ItemStack restored = ToolboxItems.restore(stored);

		assertEquals(name, restored.get(DataComponents.CUSTOM_NAME));
	}
}
