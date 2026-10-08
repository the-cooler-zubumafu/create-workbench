package dev.thecoolerzubumafu.createworkbench.content.equipment.workbench;

public final class WorkbenchRange {

	public static final double MULTIPLIER = 2.0;

	private WorkbenchRange() {}

	public static double apply(double base) {
		return base * MULTIPLIER;
	}
}
