package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.Objects;
import java.util.UUID;

public final class WorkbenchLock {
	private UUID lockId;

	public boolean isLocked() {
		return lockId != null;
	}

	public UUID lockId() {
		return lockId;
	}

	public boolean grantsAccess(UUID carriedKeyId) {
		if (!isLocked())
			return true;
		return lockId.equals(carriedKeyId);
	}

	public void lock(UUID lockId) {
		this.lockId = Objects.requireNonNull(lockId);
	}

	public void unlock() {
		this.lockId = null;
	}
}
