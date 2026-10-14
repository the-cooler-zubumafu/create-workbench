package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

public interface ItemAdapter<T, S> {
	T empty();

	boolean isEmpty(T item);

	boolean isToolbox(T item);

	S snapshot(T item);

	T restore(S stored);
}
