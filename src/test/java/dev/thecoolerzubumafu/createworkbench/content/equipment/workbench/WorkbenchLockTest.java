package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
		lock.lock(lockId);

		assertTrue(lock.grantsAccess(lockId), "the matching key grants access");
		assertFalse(lock.grantsAccess(UUID.randomUUID()), "a different key is denied");
		assertFalse(lock.grantsAccess(null), "no key is denied");
	}

	@Test
	void unlockingGrantsAccessToAnyoneAgain() {
		WorkbenchLock lock = new WorkbenchLock();
		lock.lock(UUID.randomUUID());
		lock.unlock();

		assertTrue(lock.grantsAccess(null));
	}

	@Test
	void lockRetainsItsIdUntilUnlocked() {
		WorkbenchLock lock = new WorkbenchLock();
		assertFalse(lock.isLocked());
		assertNull(lock.lockId());

		UUID id = UUID.randomUUID();
		lock.lock(id);

		assertTrue(lock.isLocked());
		assertEquals(id, lock.lockId());

		lock.unlock();

		assertFalse(lock.isLocked());
		assertNull(lock.lockId());
	}

	@Test
	void lockingWithoutAnIdIsRejected() {
		WorkbenchLock lock = new WorkbenchLock();

		assertThrows(NullPointerException.class, () -> lock.lock(null));
		assertFalse(lock.isLocked(), "a rejected lock must not change the state");
	}

	@Test
	void relockingReplacesThePreviousId() {
		WorkbenchLock lock = new WorkbenchLock();
		lock.lock(UUID.randomUUID());
		UUID relinked = UUID.randomUUID();
		lock.lock(relinked);

		assertEquals(relinked, lock.lockId());
		assertFalse(lock.grantsAccess(null));
	}
}
