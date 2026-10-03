package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pure slot bookkeeping for a Workbench: eight fixed slots (0..7) that may hold a
 * value, with holes. No Minecraft types.
 */
public class WorkbenchSlots<T> {

	public static final int CAPACITY = 8;

	private final Map<Integer, T> slots = new LinkedHashMap<>();

	public int size() {
		return slots.size();
	}

	public boolean insert(T value) {
		int slot = firstFreeSlot();
		if (slot < 0)
			return false;
		slots.put(slot, value);
		return true;
	}

	public boolean insertAt(int slot, T value) {
		if (slot < 0 || slot >= CAPACITY || slots.containsKey(slot))
			return false;
		slots.put(slot, value);
		return true;
	}

	public void setAt(int slot, T value) {
		if (slot < 0 || slot >= CAPACITY)
			return;
		slots.put(slot, value);
	}

	public T get(int slot) {
		return slots.get(slot);
	}

	public T remove(int slot) {
		return slots.remove(slot);
	}

	public Map<Integer, T> contents() {
		return Collections.unmodifiableMap(new LinkedHashMap<>(slots));
	}

	public void clear() {
		slots.clear();
	}

	public void setAll(Map<Integer, T> values) {
		slots.clear();
		slots.putAll(values);
	}

	private int firstFreeSlot() {
		for (int slot = 0; slot < CAPACITY; slot++)
			if (!slots.containsKey(slot))
				return slot;
		return -1;
	}
}
