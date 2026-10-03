package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.Objects;
import java.util.UUID;

public final class WorkbenchLock {

	private UUID lockId;
	private String lockerName;

	public boolean isLocked() {
		return lockId != null;
	}

	public UUID lockId() {
		return lockId;
	}

	public String lockerName() {
		return lockerName;
	}

	public boolean grantsAccess(UUID carriedKeyId) {
		if (!isLocked())
			return true;
		return lockId.equals(carriedKeyId);
	}

	public void lock(UUID lockId, String lockerName) {
		this.lockId = Objects.requireNonNull(lockId);
		this.lockerName = lockerName;
	}

	public void unlock() {
		this.lockId = null;
		this.lockerName = null;
	}
}
