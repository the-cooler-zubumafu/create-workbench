package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.simibubi.create.AllDataComponents;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

class WorkbenchItemHandlerTest {

	@Test
	void placingAToolboxIntoASlotStoresItAndReadingRestoresItsContents() {
		WorkbenchStorage storage = new WorkbenchStorage();
		WorkbenchItemHandler handler = new WorkbenchItemHandler(storage, () -> {});

		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 4));
		ItemStack toolbox = ToolboxItems.restore(
			new StoredToolbox(inventory, DyeColor.ORANGE, UUID.randomUUID()));

		ItemStack remainder = handler.insertItem(3, toolbox, false);

		assertTrue(remainder.isEmpty(), "the toolbox should be accepted");
		assertEquals(1, storage.size());
		ItemStack read = handler.getStackInSlot(3);
		assertFalse(read.isEmpty(), "the slot should now hold the toolbox");
		ToolboxInventory readInventory = read.get(AllDataComponents.TOOLBOX_INVENTORY);
		assertEquals(4, readInventory.getStackInSlot(0)
			.getCount());
		assertTrue(readInventory.getStackInSlot(0)
			.is(Items.DIAMOND));
		assertEquals(DyeColor.ORANGE, ToolboxItems.colorOf(read));
	}

	@Test
	void rejectsNonToolboxItemsAndOccupiedSlots() {
		WorkbenchStorage storage = new WorkbenchStorage();
		WorkbenchItemHandler handler = new WorkbenchItemHandler(storage, () -> {});

		ItemStack remainingStone = handler.insertItem(0, new ItemStack(Items.STONE), false);
		assertFalse(remainingStone.isEmpty(), "a non-toolbox must be rejected");
		assertEquals(0, storage.size());

		handler.insertItem(0, ToolboxItems.restore(
			new StoredToolbox(new ToolboxInventory(null), DyeColor.LIME, UUID.randomUUID())), false);
		ItemStack remainingSecond = handler.insertItem(0, ToolboxItems.restore(
			new StoredToolbox(new ToolboxInventory(null), DyeColor.PINK, UUID.randomUUID())), false);
		assertFalse(remainingSecond.isEmpty(), "an occupied slot must be rejected");
		assertEquals(1, storage.size());
	}

	@Test
	void extractingReturnsTheToolboxWithContentsAndFreesTheSlot() {
		WorkbenchStorage storage = new WorkbenchStorage();
		WorkbenchItemHandler handler = new WorkbenchItemHandler(storage, () -> {});
		ToolboxInventory inventory = new ToolboxInventory(null);
		inventory.setStackInSlot(0, new ItemStack(Items.GOLD_INGOT, 2));
		handler.insertItem(1, ToolboxItems.restore(
			new StoredToolbox(inventory, DyeColor.CYAN, UUID.randomUUID())), false);

		ItemStack extracted = handler.extractItem(1, 1, false);

		assertFalse(extracted.isEmpty());
		assertEquals(2, extracted.get(AllDataComponents.TOOLBOX_INVENTORY)
			.getStackInSlot(0)
			.getCount());
		assertNull(storage.get(1), "the slot is freed");
		assertEquals(0, storage.size());
	}
}
