package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

/**
 * Port that lets the pure slot rules work with any item representation without
 * depending on Minecraft types. {@code T} is the item type, {@code S} the stored form.
 */
public interface ItemAdapter<T, S> {

	T empty();

	boolean isEmpty(T item);

	boolean isToolbox(T item);

	S snapshot(T item);

	T restore(S stored);
}
