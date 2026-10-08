package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class WorkbenchRangeTest {

	@Test
	void doublesTheBaseRange() {
		assertEquals(40.0, WorkbenchRange.apply(20.0));
	}

	@Test
	void zeroStaysZero() {
		assertEquals(0.0, WorkbenchRange.apply(0.0));
	}
}
