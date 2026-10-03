package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class WorkbenchLockTest {

	@Test
	void unlockedWorkbenchGrantsAccessToAnyone() {
		WorkbenchLock lock = new WorkbenchLock();

		assertTrue(lock.grantsAccess(null));
		assertTrue(lock.grantsAccess(UUID.randomUUID()));
	}

	@Test
	void lockedWorkbenchGrantsAccessOnlyToAMatchingKey() {
		WorkbenchLock lock = new WorkbenchLock();
		UUID lockId = UUID.randomUUID();
		lock.lock(lockId, "Alice");

		assertTrue(lock.grantsAccess(lockId), "the matching key grants access");
		assertFalse(lock.grantsAccess(UUID.randomUUID()), "a different key is denied");
		assertFalse(lock.grantsAccess(null), "no key is denied");
	}

	@Test
	void unlockingGrantsAccessToAnyoneAgain() {
		WorkbenchLock lock = new WorkbenchLock();
		lock.lock(UUID.randomUUID(), "Alice");
		lock.unlock();

		assertTrue(lock.grantsAccess(null));
	}
}
