package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.toolbox.ToolboxInventory;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

class WorkbenchStorageTest {

	@Test
	void storesUpToEightToolboxesAndRejectsTheNinth() {
		WorkbenchStorage storage = new WorkbenchStorage();

		for (int i = 0; i < WorkbenchStorage.CAPACITY; i++)
			assertTrue(storage.insert(storedToolbox()), "insert " + i + " should succeed");

		assertEquals(WorkbenchStorage.CAPACITY, storage.size());
		assertFalse(storage.insert(storedToolbox()), "the ninth insert should be rejected");
		assertEquals(WorkbenchStorage.CAPACITY, storage.size());
	}

	@Test
	void rejectsItemsThatAreNotToolboxes() {
		WorkbenchStorage storage = new WorkbenchStorage();

		assertFalse(storage.insert(new ItemStack(Items.STONE)), "a non-toolbox must be rejected");
		assertEquals(0, storage.size());
	}

	@Test
	void acceptsAToolboxItemAndGrowsTheStoredCount() {
		WorkbenchStorage storage = new WorkbenchStorage();

		assertTrue(storage.insert(AllBlocks.TOOLBOXES.get(DyeColor.GREEN)
			.asStack()));
		assertEquals(1, storage.size());
	}

	@Test
	void fillsLowestFreeSlotAndRemovalLeavesAHole() {
		WorkbenchStorage storage = new WorkbenchStorage();
		StoredToolbox a = storedToolbox();
		StoredToolbox b = storedToolbox();
		StoredToolbox c = storedToolbox();
		storage.insert(a);
		storage.insert(b);
		storage.insert(c);

		assertSame(a, storage.get(0));
		assertSame(b, storage.get(1));
		assertSame(c, storage.get(2));

		assertSame(b, storage.remove(1));
		assertEquals(2, storage.size());
		assertNull(storage.get(1), "a removed slot is a hole");
		assertSame(a, storage.get(0), "other slots stay put");
		assertSame(c, storage.get(2), "other slots stay put");

		StoredToolbox d = storedToolbox();
		storage.insert(d);
		assertSame(d, storage.get(1), "the lowest free slot is refilled");
	}

	private static StoredToolbox storedToolbox() {
		return new StoredToolbox(new ToolboxInventory(null), DyeColor.BROWN, UUID.randomUUID());
	}
}
