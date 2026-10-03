package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WorkbenchSlotItemsTest {

	private static final class ToolboxStrings implements ItemAdapter<String, String> {

		@Override
		public String empty() {
			return "";
		}

		@Override
		public boolean isEmpty(String item) {
			return item.isEmpty();
		}

		@Override
		public boolean isToolbox(String item) {
			return item.startsWith("toolbox:");
		}

		@Override
		public String snapshot(String item) {
			return isToolbox(item) ? item : null;
		}

		@Override
		public String restore(String stored) {
			return stored;
		}
	}

	private final WorkbenchSlots<String> slots = new WorkbenchSlots<>();
	private int changes;
	private final WorkbenchSlotItems<String, String> rules =
		new WorkbenchSlotItems<>(slots, new ToolboxStrings(), () -> changes++);

	@Test
	void exposesEightSlotsWithUnitLimit() {
		assertEquals(WorkbenchSlots.CAPACITY, rules.getSlots());
		assertEquals(1, rules.getSlotLimit(0));
	}

	@Test
	void acceptsAToolboxIntoAnEmptySlot() {
		String remainder = rules.insertItem(3, "toolbox:red", false);

		assertEquals("", remainder, "fully accepted");
		assertEquals("toolbox:red", rules.getStackInSlot(3));
		assertEquals(1, slots.size());
		assertEquals(1, changes);
	}

	@Test
	void rejectsANonToolboxAndLeavesTheSlotEmpty() {
		String remainder = rules.insertItem(0, "stone", false);

		assertEquals("stone", remainder);
		assertEquals("", rules.getStackInSlot(0));
		assertEquals(0, slots.size());
		assertEquals(0, changes);
	}

	@Test
	void rejectsAnOccupiedSlot() {
		rules.insertItem(0, "toolbox:red", false);
		String remainder = rules.insertItem(0, "toolbox:blue", false);

		assertEquals("toolbox:blue", remainder);
		assertEquals("toolbox:red", rules.getStackInSlot(0));
		assertEquals(1, slots.size());
	}

	@Test
	void simulateInsertDoesNotStoreOrNotify() {
		String remainder = rules.insertItem(0, "toolbox:red", true);

		assertEquals("", remainder);
		assertEquals("", rules.getStackInSlot(0));
		assertEquals(0, slots.size());
		assertEquals(0, changes);
	}

	@Test
	void extractingReturnsTheToolboxAndFreesTheSlot() {
		rules.insertItem(1, "toolbox:green", false);
		changes = 0;

		String extracted = rules.extractItem(1, 1, false);

		assertEquals("toolbox:green", extracted);
		assertEquals("", rules.getStackInSlot(1));
		assertEquals(0, slots.size());
		assertEquals(1, changes);
	}

	@Test
	void extractingEmptyOrNonPositiveAmountReturnsEmpty() {
		assertEquals("", rules.extractItem(0, 1, false));
		rules.insertItem(0, "toolbox:red", false);
		assertEquals("", rules.extractItem(0, 0, false));
		assertEquals("toolbox:red", rules.getStackInSlot(0));
	}

	@Test
	void simulateExtractKeepsTheSlot() {
		rules.insertItem(0, "toolbox:red", false);

		String simulated = rules.extractItem(0, 1, true);

		assertEquals("toolbox:red", simulated);
		assertEquals("toolbox:red", rules.getStackInSlot(0));
		assertEquals(1, slots.size());
	}

	@Test
	void setStackStoresReplacesAndClears() {
		rules.setStackInSlot(2, "toolbox:red");
		assertEquals("toolbox:red", rules.getStackInSlot(2));
		assertEquals(1, changes);

		rules.setStackInSlot(2, "toolbox:blue");
		assertEquals("toolbox:blue", rules.getStackInSlot(2));
		assertEquals(1, slots.size());

		rules.setStackInSlot(2, "");
		assertEquals("", rules.getStackInSlot(2));
		assertEquals(0, slots.size());
		assertEquals(3, changes);
	}

	@Test
	void setStackIgnoresNonToolboxesAndOutOfRange() {
		rules.setStackInSlot(0, "stone");
		assertEquals(0, slots.size());
		assertEquals(0, changes);

		rules.setStackInSlot(-1, "toolbox:red");
		rules.setStackInSlot(WorkbenchSlots.CAPACITY, "toolbox:red");
		assertEquals(0, slots.size());
		assertEquals(0, changes);
	}

	@Test
	void isItemValidAcceptsOnlyToolboxes() {
		assertTrue(rules.isItemValid(0, "toolbox:red"));
		assertFalse(rules.isItemValid(0, "stone"));
		assertFalse(rules.isItemValid(0, ""));
	}
}
