package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class WorkbenchSlotsTest {

	@Test
	void storesUpToEightAndRejectsTheNinth() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();

		for (int i = 0; i < WorkbenchSlots.CAPACITY; i++)
			assertTrue(slots.insert("toolbox" + i));

		assertEquals(WorkbenchSlots.CAPACITY, slots.size());
		assertFalse(slots.insert("overflow"));
		assertEquals(WorkbenchSlots.CAPACITY, slots.size());
	}

	@Test
	void fillsLowestFreeSlotAndRemovalLeavesAHole() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();
		slots.insert("a");
		slots.insert("b");
		slots.insert("c");

		assertEquals("a", slots.get(0));
		assertEquals("b", slots.get(1));
		assertEquals("c", slots.get(2));

		assertEquals("b", slots.remove(1));
		assertEquals(2, slots.size());
		assertNull(slots.get(1));
		assertEquals("a", slots.get(0));
		assertEquals("c", slots.get(2));

		slots.insert("d");
		assertEquals("d", slots.get(1));
	}

	@Test
	void insertAtRejectsOccupiedSlotsAndOutOfRange() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();

		assertFalse(slots.insertAt(-1, "x"));
		assertFalse(slots.insertAt(WorkbenchSlots.CAPACITY, "x"));
		assertTrue(slots.insertAt(3, "a"));
		assertFalse(slots.insertAt(3, "b"));
		assertEquals("a", slots.get(3));
	}

	@Test
	void setAtReplacesAndIgnoresOutOfRange() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();
		slots.setAt(3, "a");
		slots.setAt(3, "b");
		assertEquals("b", slots.get(3));
		assertEquals(1, slots.size());

		slots.setAt(-1, "ignored");
		slots.setAt(WorkbenchSlots.CAPACITY, "ignored");
		assertEquals(1, slots.size());
	}

	@Test
	void contentsIsAnUnmodifiableSnapshot() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();
		slots.insert("a");
		Map<Integer, String> contents = slots.contents();

		assertEquals(Map.of(0, "a"), contents);
		assertThrows(UnsupportedOperationException.class, () -> contents.put(1, "b"));

		slots.insert("b");
		assertEquals(1, contents.size(), "snapshot does not track later changes");
	}

	@Test
	void setAllReplacesEverything() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();
		slots.insert("old");

		slots.setAll(Map.of(2, "x", 5, "y"));

		assertEquals(2, slots.size());
		assertNull(slots.get(0));
		assertEquals("x", slots.get(2));
		assertEquals("y", slots.get(5));
	}

	@Test
	void clearRemovesEverything() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();
		slots.insert("a");
		slots.insert("b");

		slots.clear();

		assertEquals(0, slots.size());
		assertNull(slots.get(0));
	}

	@Test
	void getOnEmptySlotIsNull() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();
		assertNull(slots.get(0));
		assertNull(slots.remove(0));
	}

	@Test
	void insertAtPreservesReference() {
		WorkbenchSlots<String> slots = new WorkbenchSlots<>();
		String value = new String("a");
		slots.insert(value);
		assertSame(value, slots.get(0));
	}
}
